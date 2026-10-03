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
    if (params.isEmpty() || !shouldAppendParamToUrl(method)) {
      return fullUrl;
    }

    StringBuilder urlBuilder = new StringBuilder(fullUrl);
    boolean hasExistingParams = fullUrl.contains("?");

    for (Map.Entry<String, Object> entry : params.entrySet()) {
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

  /**
   * 只有不带请求体的方法才把参数放进 URL。
   *
   * <p>已对真实面板验证：面板会读取表单请求体中的业务参数和签名参数。
   *
   * <p>因此 POST/PUT/PATCH 的参数只放请求体，避免密码和签名出现在 URL 与访问日志中。
   */
  private static boolean shouldAppendParamToUrl(BtApi.HttpMethod method) {
    return method == BtApi.HttpMethod.GET || method == BtApi.HttpMethod.DELETE;
  }

  static String encodeValue(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8);
    } catch (Exception exception) {
      return value;
    }
  }
}
