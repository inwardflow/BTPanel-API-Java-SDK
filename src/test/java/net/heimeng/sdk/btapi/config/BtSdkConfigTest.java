package net.heimeng.sdk.btapi.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BtSdkConfig tests")
class BtSdkConfigTest {

  @Test
  @DisplayName("Builder normalizes URL and protects immutable state")
  void buildValidatedConfig() {
    BtSdkConfig config =
        BtSdkConfig.builder()
            .baseUrl("https://panel.example.com:8888/")
            .apiKey("test-api-key")
            .connectTimeout(5)
            .connectTimeoutUnit(TimeUnit.SECONDS)
            .retryableStatusCodes(408, 429, 500)
            .extraHeaders(Map.of("X-Test", "true"))
            .build();

    assertEquals("https://panel.example.com:8888", config.getBaseUrl());
    assertEquals("test-api-key", config.getApiKey());
    assertArrayEquals(new int[] {408, 429, 500}, config.getRetryableStatusCodes());
    assertEquals("true", config.getExtraHeaders().get("X-Test"));
    assertTrue(config.isValid());
  }

  @Test
  @DisplayName("Duration helpers convert to millisecond storage")
  void durationHelpersConvertToMillis() {
    BtSdkConfig config =
        BtSdkConfig.builder()
            .baseUrl("http://localhost:8888")
            .apiKey("test-api-key")
            .connectTimeout(Duration.ofSeconds(3))
            .readTimeout(Duration.ofSeconds(7))
            .build();

    assertEquals(3000, config.getConnectTimeout());
    assertEquals(TimeUnit.MILLISECONDS, config.getConnectTimeoutUnit());
    assertEquals(7000, config.getReadTimeout());
    assertEquals(TimeUnit.MILLISECONDS, config.getReadTimeoutUnit());
  }

  @Test
  @DisplayName("Blank base URL is rejected")
  void rejectBlankBaseUrl() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> BtSdkConfig.builder().baseUrl(" ").apiKey("test-api-key").build());

    assertTrue(exception.getMessage().contains("baseUrl"));
  }

  @Test
  @DisplayName("Invalid retry interval is rejected")
  void rejectInvalidRetryInterval() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                BtSdkConfig.builder()
                    .baseUrl("http://localhost:8888")
                    .apiKey("test-api-key")
                    .retryInterval(Duration.ZERO)
                    .build());

    assertNotNull(exception);
  }

  @Test
  @DisplayName("Trust store builder should switch SSL mode to CUSTOM_TRUST_STORE")
  void trustStoreBuilderSwitchesSslMode() {
    BtSdkConfig config =
        BtSdkConfig.builder()
            .baseUrl("https://panel.example.com:8888")
            .apiKey("test-api-key")
            .trustStore("certs/test.p12", "changeit")
            .build();

    assertEquals(BtSdkConfig.SslMode.CUSTOM_TRUST_STORE, config.getSslMode());
    assertEquals("certs/test.p12", config.getTrustStorePath());
    assertEquals("changeit", config.getTrustStorePassword());
    assertEquals("PKCS12", config.getTrustStoreType());
  }

  @Test
  @DisplayName("Duration helper should reject null connect timeout")
  void durationHelperRejectsNullConnectTimeout() {
    assertThrows(
        NullPointerException.class,
        () ->
            BtSdkConfig.builder()
                .baseUrl("http://localhost:8888")
                .apiKey("test-api-key")
                .connectTimeout((Duration) null)
                .build());
  }

  @Test
  @DisplayName("Duration helper should reject zero read timeout")
  void durationHelperRejectsZeroReadTimeout() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            BtSdkConfig.builder()
                .baseUrl("http://localhost:8888")
                .apiKey("test-api-key")
                .readTimeout(Duration.ZERO)
                .build());
  }
}
