package net.heimeng.sdk.btapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.api.BtApi.HttpMethod;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("DefaultBtClient request behavior tests")
class DefaultBtClientTest {

  private HttpServer server;

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  @DisplayName("POST form requests include content type and keep multiline payloads out of the URL")
  void postFormRequestsIncludeContentTypeAndKeepMultilinePayloadsOutOfUrl() throws Exception {
    AtomicReference<String> methodRef = new AtomicReference<>();
    AtomicReference<String> contentTypeRef = new AtomicReference<>();
    AtomicReference<String> queryRef = new AtomicReference<>();
    AtomicReference<String> bodyRef = new AtomicReference<>();

    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/site",
        exchange -> {
          methodRef.set(exchange.getRequestMethod());
          contentTypeRef.set(exchange.getRequestHeaders().getFirst("Content-Type"));
          queryRef.set(exchange.getRequestURI().getRawQuery());
          bodyRef.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          writeJsonResponse(exchange, "{\"status\":true,\"msg\":\"ok\",\"data\":true}");
        });
    server.start();

    String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    try (DefaultBtClient client =
        new DefaultBtClient(
            BtSdkConfig.builder().baseUrl(baseUrl).apiKey("test-api-key").build())) {
      BtResult<Boolean> result = client.execute(new FakeSslInstallApi());

      assertTrue(result.isSuccess());
    }

    assertEquals("POST", methodRef.get());
    assertNotNull(contentTypeRef.get());
    assertTrue(
        contentTypeRef.get().startsWith("application/x-www-form-urlencoded"),
        "Expected form content type for POST body requests");
    assertNotNull(queryRef.get());
    assertTrue(queryRef.get().contains("siteName=example.com"));
    assertTrue(queryRef.get().contains("request_time="));
    assertTrue(queryRef.get().contains("request_token="));
    assertFalse(queryRef.get().contains("key="));
    assertFalse(queryRef.get().contains("csr="));
    assertNotNull(bodyRef.get());
    assertTrue(bodyRef.get().contains("siteName=example.com"));
    assertTrue(bodyRef.get().contains("key=line1%0Aline2"));
    assertTrue(bodyRef.get().contains("csr=cert-line1%0Acert-line2"));
  }

  @Test
  @DisplayName("SAFE_REQUESTS_ONLY retries GET when status is retryable")
  void safeRetryModeRetriesGet() throws Exception {
    AtomicInteger requestCount = new AtomicInteger(0);

    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/retry-get",
        exchange -> {
          int currentAttempt = requestCount.incrementAndGet();
          if (currentAttempt == 1) {
            writeJsonResponse(exchange, 503, "{\"status\":false,\"msg\":\"retry\"}");
            return;
          }
          writeJsonResponse(exchange, "{\"status\":true,\"msg\":\"ok\",\"data\":true}");
        });
    server.start();

    String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    BtSdkConfig config =
        BtSdkConfig.builder()
            .baseUrl(baseUrl)
            .apiKey("test-api-key")
            .retryMode(BtSdkConfig.RetryMode.SAFE_REQUESTS_ONLY)
            .retryCount(1)
            .retryInterval(Duration.ofMillis(10))
            .retryableStatusCodes(503)
            .build();

    try (DefaultBtClient client = new DefaultBtClient(config)) {
      BtResult<Boolean> result = client.execute(new FakeBooleanApi("retry-get", HttpMethod.GET));

      assertTrue(result.isSuccess());
      assertEquals(2, requestCount.get());
    }
  }

  @Test
  @DisplayName("SAFE_REQUESTS_ONLY should not retry POST requests")
  void safeRetryModeDoesNotRetryPost() throws Exception {
    AtomicInteger requestCount = new AtomicInteger(0);

    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/retry-post",
        exchange -> {
          requestCount.incrementAndGet();
          writeJsonResponse(exchange, 503, "{\"status\":false,\"msg\":\"retry\"}");
        });
    server.start();

    String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    BtSdkConfig config =
        BtSdkConfig.builder()
            .baseUrl(baseUrl)
            .apiKey("test-api-key")
            .retryMode(BtSdkConfig.RetryMode.SAFE_REQUESTS_ONLY)
            .retryCount(2)
            .retryInterval(Duration.ofMillis(10))
            .retryableStatusCodes(503)
            .build();

    try (DefaultBtClient client = new DefaultBtClient(config)) {
      assertThrows(
          BtApiException.class,
          () -> client.execute(new FakeBooleanApi("retry-post", HttpMethod.POST)));
      assertEquals(1, requestCount.get());
    }
  }

  @Test
  @DisplayName("ALL_REQUESTS retries POST requests when status is retryable")
  void allRequestsModeRetriesPost() throws Exception {
    AtomicInteger requestCount = new AtomicInteger(0);

    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/retry-post-all",
        exchange -> {
          int currentAttempt = requestCount.incrementAndGet();
          if (currentAttempt == 1) {
            writeJsonResponse(exchange, 503, "{\"status\":false,\"msg\":\"retry\"}");
            return;
          }
          writeJsonResponse(exchange, "{\"status\":true,\"msg\":\"ok\",\"data\":true}");
        });
    server.start();

    String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    BtSdkConfig config =
        BtSdkConfig.builder()
            .baseUrl(baseUrl)
            .apiKey("test-api-key")
            .retryMode(BtSdkConfig.RetryMode.ALL_REQUESTS)
            .retryCount(1)
            .retryInterval(Duration.ofMillis(10))
            .retryableStatusCodes(503)
            .build();

    try (DefaultBtClient client = new DefaultBtClient(config)) {
      BtResult<Boolean> result =
          client.execute(new FakeBooleanApi("retry-post-all", HttpMethod.POST));

      assertTrue(result.isSuccess());
      assertEquals(2, requestCount.get());
    }
  }

  @Test
  @DisplayName("CUSTOM_TRUST_STORE mode should fail fast when file is missing")
  void customTrustStoreModeFailsFastWhenFileMissing() {
    BtSdkConfig config =
        BtSdkConfig.builder()
            .baseUrl("https://127.0.0.1:8443")
            .apiKey("test-api-key")
            .trustStore("target/missing-" + System.nanoTime() + ".p12", "changeit")
            .build();

    assertThrows(IllegalStateException.class, () -> new DefaultBtClient(config));
  }

  private void writeJsonResponse(HttpExchange exchange, String responseBody) throws IOException {
    writeJsonResponse(exchange, 200, responseBody);
  }

  private void writeJsonResponse(HttpExchange exchange, int statusCode, String responseBody)
      throws IOException {
    byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
    exchange.sendResponseHeaders(statusCode, responseBytes.length);
    try (OutputStream responseStream = exchange.getResponseBody()) {
      responseStream.write(responseBytes);
    }
  }

  private static final class FakeSslInstallApi extends BaseBtApi<BtResult<Boolean>> {

    private FakeSslInstallApi() {
      super("site?action=SetSSL", HttpMethod.POST);
      addParam("siteName", "example.com");
      addParam("key", "line1\nline2");
      addParam("csr", "cert-line1\ncert-line2");
    }

    @Override
    public BtResult<Boolean> parseResponse(String response) {
      BtResult<Boolean> result = new BtResult<>();
      result.setStatus(true);
      result.setMsg("ok");
      result.setData(true);
      return result;
    }
  }

  private static final class FakeBooleanApi extends BaseBtApi<BtResult<Boolean>> {

    private FakeBooleanApi(String endpoint, HttpMethod method) {
      super(endpoint, method);
      addParam("name", "retry-api");
    }

    @Override
    public BtResult<Boolean> parseResponse(String response) {
      BtResult<Boolean> result = new BtResult<>();
      boolean success = response != null && response.contains("\"status\":true");
      result.setStatus(success);
      result.setMsg(success ? "ok" : "failed");
      result.setData(success);
      return result;
    }
  }
}
