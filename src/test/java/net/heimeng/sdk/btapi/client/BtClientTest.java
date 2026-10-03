package net.heimeng.sdk.btapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.config.BtSdkConfig;

@DisplayName("BtClient factory and configuration tests")
class BtClientTest {

  @Test
  @DisplayName("Factory creates a client from a validated config")
  void createClientFromConfig() {
    BtSdkConfig config =
        BtSdkConfig.builder()
            .baseUrl("http://localhost:8888/")
            .apiKey("test-api-key")
            .readTimeout(Duration.ofSeconds(20))
            .build();

    BtClient client = BtClientFactory.createClient(config);

    assertNotNull(client);
    assertFalse(client.isClosed());
    assertEquals("http://localhost:8888", client.getConfig().getBaseUrl());

    client.close();
    assertTrue(client.isClosed());
  }

  @Test
  @DisplayName("Factory creates a manager for high-level operations")
  void createApiManager() {
    BtApiManager apiManager =
        BtClientFactory.createApiManager("http://localhost:8888", "test-api-key");

    assertNotNull(apiManager);
    assertNotNull(apiManager.system());
    assertNotNull(apiManager.website());

    apiManager.close();
  }

  @Test
  @DisplayName("Invalid base URL is rejected eagerly")
  void rejectInvalidBaseUrl() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> BtClientFactory.createClient("invalid-url", "test-api-key"));

    assertTrue(exception.getMessage().contains("baseUrl"));
  }

  @Test
  @DisplayName("Blank API key is rejected eagerly")
  void rejectBlankApiKey() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> BtClientFactory.createClient("http://localhost:8888", " "));

    assertTrue(exception.getMessage().contains("apiKey"));
  }

  @Test
  @DisplayName("Null config is rejected eagerly")
  void rejectNullConfig() {
    NullPointerException exception =
        assertThrows(
            NullPointerException.class, () -> BtClientFactory.createClient((BtSdkConfig) null));

    assertTrue(exception.getMessage().contains("config"));
  }

  @Test
  @DisplayName("Builder supports duration-based timeout configuration")
  void durationBasedTimeoutConfiguration() {
    BtSdkConfig config =
        BtClientFactory.configBuilder()
            .baseUrl("https://panel.example.com:8888")
            .apiKey("test-api-key")
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(15))
            .retryCount(2)
            .build();

    assertEquals(5000, config.getConnectTimeout());
    assertEquals(15000, config.getReadTimeout());
    assertEquals(2, config.getRetryCount());
  }

  @Test
  @DisplayName("Invalid duration configuration surfaces as IllegalArgumentException")
  void invalidDurationConfiguration() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                BtSdkConfig.builder()
                    .baseUrl("http://localhost:8888")
                    .apiKey("test-api-key")
                    .retryInterval(Duration.ZERO)
                    .build());

    assertInstanceOf(IllegalArgumentException.class, exception);
  }
}
