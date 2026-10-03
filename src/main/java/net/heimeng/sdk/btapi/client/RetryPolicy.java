package net.heimeng.sdk.btapi.client;

import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.config.BtSdkConfig;

/**
 * 客户端重试策略。
 *
 * <ul>
 *   <li>按 {@link BtSdkConfig.RetryMode} 只重试幂等或显式允许的请求；
 *   <li>证书校验失败、域名无法解析等确定性错误不重试；
 *   <li>等待时间采用带随机抖动的指数退避（equal jitter），避免多个客户端同时重试；
 *   <li>服务端返回 {@code Retry-After} 时至少等待该时长，但不超过 {@code maxRetryInterval}。
 * </ul>
 */
final class RetryPolicy {

  private final BtSdkConfig.RetryMode retryMode;
  private final int[] retryableStatusCodes;
  private final Duration baseInterval;
  private final Duration maxInterval;
  private final DoubleSupplier random;

  RetryPolicy(BtSdkConfig config) {
    this(config, () -> ThreadLocalRandom.current().nextDouble());
  }

  RetryPolicy(BtSdkConfig config, DoubleSupplier random) {
    Objects.requireNonNull(config, "config cannot be null");
    this.retryMode = config.getRetryMode();
    this.retryableStatusCodes = config.getRetryableStatusCodes();
    this.baseInterval = config.getRetryInterval();
    this.maxInterval = config.getMaxRetryInterval();
    this.random = Objects.requireNonNull(random, "random cannot be null");
  }

  /**
   * 计算第 {@code attempt} 次失败后（从 0 开始）的等待时间。
   *
   * @param attempt 已失败的尝试序号，从 0 开始
   * @param retryAfter 服务端通过 {@code Retry-After} 要求的最短等待时间
   * @return 本次重试前应等待的时长
   */
  Duration backoffDelay(int attempt, Optional<Duration> retryAfter) {
    long baseMillis = baseInterval.toMillis();
    long capMillis = maxInterval.toMillis();
    long exponentialMillis = capMillis;
    if (attempt < 31) {
      exponentialMillis = Math.min(capMillis, baseMillis * (1L << Math.max(attempt, 0)));
    }
    long half = exponentialMillis / 2;
    long delayMillis = half + (long) (random.getAsDouble() * (exponentialMillis - half));

    if (retryAfter.isPresent()) {
      long requestedMillis = Math.min(capMillis, Math.max(0, retryAfter.get().toMillis()));
      delayMillis = Math.max(delayMillis, requestedMillis);
    }
    return Duration.ofMillis(delayMillis);
  }

  /**
   * 解析 {@code Retry-After} 头，支持秒数与 HTTP-date 两种格式。
   *
   * @param headerValue 头的原始值
   * @param now 当前时间
   * @return 可解析时返回等待时长
   */
  static Optional<Duration> parseRetryAfter(String headerValue, Instant now) {
    if (headerValue == null || headerValue.isBlank()) {
      return Optional.empty();
    }
    String value = headerValue.trim();
    try {
      long seconds = Long.parseLong(value);
      return seconds < 0 ? Optional.empty() : Optional.of(Duration.ofSeconds(seconds));
    } catch (NumberFormatException ignored) {
      // fall through to the HTTP-date form
    }
    try {
      Instant retryAt =
          ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
      Duration delay = Duration.between(now, retryAt);
      return Optional.of(delay.isNegative() ? Duration.ZERO : delay);
    } catch (DateTimeParseException ignored) {
      return Optional.empty();
    }
  }

  boolean shouldRetryStatus(BtApi.HttpMethod method, int statusCode, boolean forceRetry) {
    if (forceRetry) {
      return true;
    }
    return canRetryByMode(method) && isRetryableStatusCode(statusCode);
  }

  boolean shouldRetryException(BtApi.HttpMethod method, Throwable exception, boolean forceRetry) {
    if (isPermanentFailure(exception)) {
      return false;
    }
    return forceRetry || canRetryByMode(method);
  }

  /** 判断异常链中是否包含证书校验失败。 */
  static boolean isCertificateFailure(Throwable exception) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      if (cause instanceof CertificateException) {
        return true;
      }
      if (cause.getCause() == cause) {
        break;
      }
    }
    return false;
  }

  /** 证书校验失败、域名无法解析等确定性错误重试也不会成功，应直接失败。 */
  static boolean isPermanentFailure(Throwable exception) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      if (cause instanceof CertificateException
          || cause instanceof UnknownHostException
          || cause instanceof UnresolvedAddressException) {
        return true;
      }
      if (cause.getCause() == cause) {
        break;
      }
    }
    return false;
  }

  private boolean canRetryByMode(BtApi.HttpMethod method) {
    switch (retryMode) {
      case NONE:
        return false;
      case SAFE_REQUESTS_ONLY:
        return method == BtApi.HttpMethod.GET;
      case ALL_REQUESTS:
        return true;
      default:
        return false;
    }
  }

  private boolean isRetryableStatusCode(int code) {
    return Arrays.stream(retryableStatusCodes).anyMatch(status -> status == code);
  }
}
