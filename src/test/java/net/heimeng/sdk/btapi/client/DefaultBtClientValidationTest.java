package net.heimeng.sdk.btapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.api.BtApi.HttpMethod;
import net.heimeng.sdk.btapi.api.website.GetWebsitePhpVersionApi;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.facade.WebsiteOperations;
import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("DefaultBtClient parameter validation tests")
class DefaultBtClientValidationTest {

  private HttpServer server;
  private final AtomicInteger requestCount = new AtomicInteger();
  private DefaultBtClient client;

  @BeforeEach
  void setUp() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/",
        exchange -> {
          requestCount.incrementAndGet();
          writeJsonResponse(exchange, "{\"status\":true,\"msg\":\"ok\",\"data\":true}");
        });
    server.start();

    String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    client =
        new DefaultBtClient(BtSdkConfig.builder().baseUrl(baseUrl).apiKey("test-api-key").build());
  }

  @AfterEach
  void tearDown() {
    if (client != null) {
      client.close();
    }
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  @DisplayName("execute sends the request when parameters are valid")
  void executeSendsValidRequest() {
    BtResult<Boolean> result = client.execute(new RequiresNameApi().setName("demo"));

    assertTrue(result.isSuccess());
    assertEquals(1, requestCount.get());
  }

  @Test
  @DisplayName("execute throws INVALID_PARAMETERS without sending an HTTP request")
  void executeRejectsInvalidRequestWithoutHttpCall() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> client.execute(new RequiresNameApi()));

    assertEquals(BtApiException.INVALID_PARAMETERS, exception.getErrorCode());
    assertTrue(exception.getMessage().contains("RequiresNameApi"), exception.getMessage());
    assertTrue(exception.getMessage().contains("demo?action=Validate"), exception.getMessage());
    assertEquals(0, requestCount.get());
  }

  @Test
  @DisplayName("validation error lists parameter names but never their values")
  void validationMessageOmitsParameterValues() {
    RequiresNameApi api = new RequiresNameApi();
    // 运行时生成的哨兵值，避免在仓库里出现形似密码的字面量。
    String sentinel = "sentinel-" + UUID.randomUUID();
    api.addParam("password", sentinel);

    BtApiException exception = assertThrows(BtApiException.class, () -> client.execute(api));

    assertTrue(exception.getMessage().contains("[password]"), exception.getMessage());
    assertFalse(exception.getMessage().contains(sentinel), exception.getMessage());
  }

  @Test
  @DisplayName("interceptors do not run for a request that fails validation")
  void interceptorsDoNotRunForInvalidRequest() {
    AtomicInteger interceptorCalls = new AtomicInteger();
    client.addInterceptor(
        (context, chain) -> {
          interceptorCalls.incrementAndGet();
          chain.proceed();
        });

    assertThrows(BtApiException.class, () -> client.execute(new RequiresNameApi()));

    assertEquals(0, interceptorCalls.get());
    assertEquals(0, requestCount.get());
  }

  @Test
  @DisplayName("executeAsync completes normally when parameters are valid")
  void executeAsyncSendsValidRequest() throws Exception {
    BtResult<Boolean> result =
        client.executeAsync(new RequiresNameApi().setName("demo")).get(10, TimeUnit.SECONDS);

    assertTrue(result.isSuccess());
    assertEquals(1, requestCount.get());
  }

  @Test
  @DisplayName("executeAsync throws synchronously without sending an HTTP request")
  void executeAsyncRejectsInvalidRequestSynchronously() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> client.executeAsync(new RequiresNameApi()));

    assertEquals(BtApiException.INVALID_PARAMETERS, exception.getErrorCode());
    assertEquals(0, requestCount.get());
  }

  @Test
  @DisplayName("BtApi implementations without BaseBtApi are not validated")
  void plainBtApiUsesNoOpValidation() {
    BtApi<BtResult<Boolean>> api =
        new BtApi<>() {
          @Override
          public String getEndpoint() {
            return "demo?action=Plain";
          }

          @Override
          public HttpMethod getMethod() {
            return HttpMethod.POST;
          }

          @Override
          public Map<String, Object> getParams() {
            return Map.of();
          }

          @Override
          public BtResult<Boolean> parseResponse(String response) {
            return successResult();
          }
        };

    assertTrue(client.execute(api).isSuccess());
    assertEquals(1, requestCount.get());
  }

  @Test
  @DisplayName("ID-only PHP version lookup is rejected before reaching the panel")
  @SuppressWarnings("removal")
  void deprecatedIdOnlyPhpVersionLookupFailsFast() {
    BtApiException apiException =
        assertThrows(
            BtApiException.class, () -> client.execute(new GetWebsitePhpVersionApi().setId(1)));
    assertTrue(
        apiException.getMessage().contains("site?action=GetSitePHPVersion"),
        apiException.getMessage());

    WebsiteOperations operations = new WebsiteOperations(client);
    assertThrows(BtApiException.class, () -> operations.getPhpVersion(1));
    assertThrows(BtApiException.class, () -> operations.updatePhpVersion(1, "81"));

    assertEquals(0, requestCount.get());
  }

  private static void writeJsonResponse(HttpExchange exchange, String responseBody)
      throws IOException {
    byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
    exchange.sendResponseHeaders(200, responseBytes.length);
    try (OutputStream responseStream = exchange.getResponseBody()) {
      responseStream.write(responseBytes);
    }
  }

  private static BtResult<Boolean> successResult() {
    BtResult<Boolean> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg("ok");
    result.setData(true);
    return result;
  }

  private static final class RequiresNameApi extends BaseBtApi<BtResult<Boolean>> {

    private RequiresNameApi() {
      super("demo?action=Validate", HttpMethod.POST);
    }

    private RequiresNameApi setName(String name) {
      addParam("name", name);
      return this;
    }

    @Override
    protected boolean validateParams() {
      return params.get("name") instanceof String name && !name.isBlank();
    }

    @Override
    public BtResult<Boolean> parseResponse(String response) {
      return successResult();
    }
  }
}
