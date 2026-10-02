package net.heimeng.sdk.btapi.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.config.BtSdkConfig;

@DisplayName("RetryPolicy tests")
class RetryPolicyTest {

  @Test
  @DisplayName("NONE mode should not retry unless forced")
  void noneModeShouldOnlyRetryWhenForced() {
    RetryPolicy policy = new RetryPolicy(configWithMode(BtSdkConfig.RetryMode.NONE));

    assertFalse(policy.shouldRetryStatus(BtApi.HttpMethod.GET, 503, false));
    assertFalse(policy.shouldRetryException(BtApi.HttpMethod.GET, false));
    assertTrue(policy.shouldRetryStatus(BtApi.HttpMethod.POST, 503, true));
    assertTrue(policy.shouldRetryException(BtApi.HttpMethod.POST, true));
  }

  @Test
  @DisplayName("SAFE_REQUESTS_ONLY mode should retry GET but not POST")
  void safeModeShouldRetryGetOnly() {
    RetryPolicy policy = new RetryPolicy(configWithMode(BtSdkConfig.RetryMode.SAFE_REQUESTS_ONLY));

    assertTrue(policy.shouldRetryStatus(BtApi.HttpMethod.GET, 503, false));
    assertTrue(policy.shouldRetryException(BtApi.HttpMethod.GET, false));
    assertFalse(policy.shouldRetryStatus(BtApi.HttpMethod.POST, 503, false));
    assertFalse(policy.shouldRetryException(BtApi.HttpMethod.POST, false));
  }

  @Test
  @DisplayName("ALL_REQUESTS mode should retry POST")
  void allModeShouldRetryPost() {
    RetryPolicy policy = new RetryPolicy(configWithMode(BtSdkConfig.RetryMode.ALL_REQUESTS));

    assertTrue(policy.shouldRetryStatus(BtApi.HttpMethod.POST, 503, false));
    assertTrue(policy.shouldRetryException(BtApi.HttpMethod.POST, false));
  }

  private BtSdkConfig configWithMode(BtSdkConfig.RetryMode retryMode) {
    return BtSdkConfig.builder()
        .baseUrl("http://localhost:8888")
        .apiKey("test-api-key")
        .retryMode(retryMode)
        .retryableStatusCodes(503)
        .build();
  }
}
