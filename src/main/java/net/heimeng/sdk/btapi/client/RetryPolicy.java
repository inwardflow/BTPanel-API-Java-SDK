package net.heimeng.sdk.btapi.client;

import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Objects;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.config.BtSdkConfig;

/** 客户端重试判定策略，统一封装 retry mode 与状态码匹配逻辑。 */
final class RetryPolicy {

  private final BtSdkConfig.RetryMode retryMode;
  private final int[] retryableStatusCodes;

  RetryPolicy(BtSdkConfig config) {
    Objects.requireNonNull(config, "config cannot be null");
    this.retryMode = config.getRetryMode();
    this.retryableStatusCodes = config.getRetryableStatusCodes();
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
