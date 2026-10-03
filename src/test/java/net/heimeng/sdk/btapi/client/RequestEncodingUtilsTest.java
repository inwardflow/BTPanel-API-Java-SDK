package net.heimeng.sdk.btapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;

@DisplayName("RequestEncodingUtils tests")
class RequestEncodingUtilsTest {

  @Test
  @DisplayName("buildUrl should normalize slash between base URL and endpoint")
  void buildUrlNormalizesSlash() {
    assertEquals(
        "http://localhost:8888/system?action=GetNetWork",
        RequestEncodingUtils.buildUrl("http://localhost:8888", "/system?action=GetNetWork"));
    assertEquals(
        "http://localhost:8888/system?action=GetNetWork",
        RequestEncodingUtils.buildUrl("http://localhost:8888/", "system?action=GetNetWork"));
  }

  @Test
  @DisplayName("appendQueryParameters should keep every POST parameter out of the URL")
  void appendQueryParametersKeepsPostParamsInBody() {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("siteName", "example.com");
    params.put("ftp_password", "p1");
    params.put("request_token", "token-value");
    params.put("request_time", "1234567890");

    String builtUrl =
        RequestEncodingUtils.appendQueryParameters(
            "http://localhost:8888/site?action=SetSSL", BtApi.HttpMethod.POST, params);

    assertEquals("http://localhost:8888/site?action=SetSSL", builtUrl);
  }

  @Test
  @DisplayName("appendQueryParameters should include values in GET URL and encode spaces")
  void appendQueryParametersIncludesGetValues() {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("name", "hello world");

    String builtUrl =
        RequestEncodingUtils.appendQueryParameters(
            "http://localhost:8888/site?action=Get", BtApi.HttpMethod.GET, params);

    assertTrue(builtUrl.contains("name=hello+world"));
  }

  @Test
  @DisplayName("shouldSendFormContentType should depend on method and params")
  void shouldSendFormContentTypeWorksAsExpected() {
    Map<String, Object> params = Map.of("name", "demo");

    assertTrue(RequestEncodingUtils.shouldSendFormContentType(BtApi.HttpMethod.POST, params));
    assertTrue(RequestEncodingUtils.shouldSendFormContentType(BtApi.HttpMethod.PUT, params));
    assertTrue(RequestEncodingUtils.shouldSendFormContentType(BtApi.HttpMethod.PATCH, params));
    assertFalse(RequestEncodingUtils.shouldSendFormContentType(BtApi.HttpMethod.GET, params));
    assertFalse(RequestEncodingUtils.shouldSendFormContentType(BtApi.HttpMethod.POST, Map.of()));
  }
}
