package net.heimeng.sdk.btapi.client;

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

  boolean shouldRetryException(BtApi.HttpMethod method, boolean forceRetry) {
    return forceRetry || canRetryByMode(method);
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
