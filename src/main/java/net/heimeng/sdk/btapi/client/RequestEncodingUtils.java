package net.heimeng.sdk.btapi.client;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import net.heimeng.sdk.btapi.api.BtApi;

/** 请求参数编码与 URL 拼接工具。 */
final class RequestEncodingUtils {

  private RequestEncodingUtils() {}

  static String buildUrl(String baseUrl, String endpoint) {
    if (endpoint == null || endpoint.isEmpty()) {
      return baseUrl;
    }

    String normalizedEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
    String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    return normalizedBaseUrl + normalizedEndpoint;
  }

  static String appendQueryParameters(
      String fullUrl, BtApi.HttpMethod method, Map<String, Object> params) {
    if (params.isEmpty()) {
      return fullUrl;
    }

    StringBuilder urlBuilder = new StringBuilder(fullUrl);
    boolean hasExistingParams = fullUrl.contains("?");

    for (Map.Entry<String, Object> entry : params.entrySet()) {
      if (!shouldAppendParamToUrl(method, entry.getKey(), entry.getValue())) {
        continue;
      }

      urlBuilder.append(hasExistingParams ? '&' : '?');
      urlBuilder.append(entry.getKey());
      urlBuilder.append('=');
      urlBuilder.append(encodeValue(String.valueOf(entry.getValue())));
      hasExistingParams = true;
    }

    return urlBuilder.toString();
  }

  static HttpRequest.BodyPublisher buildFormBodyPublisher(Map<String, Object> params) {
    if (params.isEmpty()) {
      return HttpRequest.BodyPublishers.noBody();
    }

    String formData =
        params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + encodeValue(String.valueOf(entry.getValue())))
            .collect(Collectors.joining("&"));
    return HttpRequest.BodyPublishers.ofString(formData);
  }

  static boolean shouldSendFormContentType(BtApi.HttpMethod method, Map<String, Object> params) {
    if (params.isEmpty()) {
      return false;
    }
    return method == BtApi.HttpMethod.POST
        || method == BtApi.HttpMethod.PUT
        || method == BtApi.HttpMethod.PATCH;
  }

  private static boolean shouldAppendParamToUrl(BtApi.HttpMethod method, String key, Object value) {
    if (method == BtApi.HttpMethod.GET || method == BtApi.HttpMethod.DELETE) {
      return true;
    }
    if ("request_token".equals(key) || "request_time".equals(key)) {
      return true;
    }
    if (!(value instanceof String stringValue)) {
      return true;
    }
    return stringValue.length() <= 512
        && stringValue.indexOf('\n') < 0
        && stringValue.indexOf('\r') < 0;
  }

  static String encodeValue(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8);
    } catch (Exception exception) {
      return value;
    }
  }
}
