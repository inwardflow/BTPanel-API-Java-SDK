package net.heimeng.sdk.btapi.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLHandshakeException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.config.BtSdkConfig;

@DisplayName("RetryPolicy tests")
class RetryPolicyTest {

  private static final Exception TRANSIENT = new IOException("connection reset");

  @Test
  @DisplayName("NONE mode should not retry unless forced")
  void noneModeShouldOnlyRetryWhenForced() {
    RetryPolicy policy = new RetryPolicy(configWithMode(BtSdkConfig.RetryMode.NONE));

    assertFalse(policy.shouldRetryStatus(BtApi.HttpMethod.GET, 503, false));
    assertFalse(policy.shouldRetryException(BtApi.HttpMethod.GET, TRANSIENT, false));
    assertTrue(policy.shouldRetryStatus(BtApi.HttpMethod.POST, 503, true));
    assertTrue(policy.shouldRetryException(BtApi.HttpMethod.POST, TRANSIENT, true));
  }

  @Test
  @DisplayName("SAFE_REQUESTS_ONLY mode should retry GET but not POST")
  void safeModeShouldRetryGetOnly() {
    RetryPolicy policy = new RetryPolicy(configWithMode(BtSdkConfig.RetryMode.SAFE_REQUESTS_ONLY));

    assertTrue(policy.shouldRetryStatus(BtApi.HttpMethod.GET, 503, false));
    assertTrue(policy.shouldRetryException(BtApi.HttpMethod.GET, TRANSIENT, false));
    assertFalse(policy.shouldRetryStatus(BtApi.HttpMethod.POST, 503, false));
    assertFalse(policy.shouldRetryException(BtApi.HttpMethod.POST, TRANSIENT, false));
  }

  @Test
  @DisplayName("ALL_REQUESTS mode should retry POST")
  void allModeShouldRetryPost() {
    RetryPolicy policy = new RetryPolicy(configWithMode(BtSdkConfig.RetryMode.ALL_REQUESTS));

    assertTrue(policy.shouldRetryStatus(BtApi.HttpMethod.POST, 503, false));
    assertTrue(policy.shouldRetryException(BtApi.HttpMethod.POST, TRANSIENT, false));
  }

  @Test
  @DisplayName("certificate and DNS failures should never be retried, even when forced")
  void permanentFailuresShouldNotRetry() {
    RetryPolicy policy = new RetryPolicy(configWithMode(BtSdkConfig.RetryMode.ALL_REQUESTS));

    // SSLHandshakeException(String, Throwable) only exists on JDK 19+, so attach the cause
    // manually.
    Exception pkix = new SSLHandshakeException("PKIX path building failed");
    pkix.initCause(new CertificateException("PKIX"));
    Exception unknownHost = new ConnectException("dns");
    unknownHost.initCause(new UnknownHostException("invalid.invalid"));
    Exception unresolved = new ConnectException();
    unresolved.initCause(new UnresolvedAddressException());

    assertFalse(policy.shouldRetryException(BtApi.HttpMethod.GET, pkix, true));
    assertFalse(policy.shouldRetryException(BtApi.HttpMethod.GET, unknownHost, true));
    assertFalse(policy.shouldRetryException(BtApi.HttpMethod.GET, unresolved, true));
    assertTrue(
        policy.shouldRetryException(
            BtApi.HttpMethod.GET, new SSLHandshakeException("Remote host closed"), false));
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
