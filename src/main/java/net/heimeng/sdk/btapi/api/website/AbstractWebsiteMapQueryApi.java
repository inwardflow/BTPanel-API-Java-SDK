package net.heimeng.sdk.btapi.api.website;

import java.util.Map;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * Website 模块中返回 {@code Map<String, Object>} 的查询型 API 公共基类。
 *
 * <p>兼容两类常见返回：
 *
 * <ul>
 *   <li>标准包装 JSON：{@code status/msg/data}
 *   <li>直接返回对象载荷
 * </ul>
 */
abstract class AbstractWebsiteMapQueryApi extends BaseBtApi<BtResult<Map<String, Object>>> {

  private final String responseName;
  private final String successMessage;
  private final String failureMessage;
  private final boolean requireNonEmptyPayload;

  protected AbstractWebsiteMapQueryApi(
      String endpoint,
      String responseName,
      String successMessage,
      String failureMessage,
      boolean requireNonEmptyPayload) {
    super(endpoint, HttpMethod.POST);
    this.responseName = responseName;
    this.successMessage = successMessage;
    this.failureMessage = failureMessage;
    this.requireNonEmptyPayload = requireNonEmptyPayload;
  }

  @Override
  public BtResult<Map<String, Object>> parseResponse(String response) {
    JSON json = WebsiteApiResponseSupport.parseJsonResponse(response, responseName);
    if (!(json instanceof JSONObject jsonObject)) {
      throw new BtApiException(responseName + " response must be a JSON object");
    }
    if (jsonObject.containsKey("status") && !jsonObject.getBool("status", false)) {
      return WebsiteApiResponseSupport.failureMapResult(jsonObject.getStr("msg", failureMessage));
    }

    Map<String, Object> payload = extractPayload(jsonObject);
    if (requireNonEmptyPayload && payload.isEmpty()) {
      throw new BtApiException(responseName + " response is missing detail payload");
    }

    return WebsiteApiResponseSupport.successMapResult(
        payload, jsonObject.getStr("msg", successMessage));
  }

  protected final Map<String, Object> extractPayload(JSONObject jsonObject) {
    Object rawData = jsonObject.get("data");
    if (rawData instanceof JSONObject dataObject) {
      return Map.copyOf(WebsiteApiResponseSupport.toMap(dataObject));
    }
    if (rawData != null) {
      throw new BtApiException(responseName + " response data must be a JSON object");
    }

    return WebsiteApiResponseSupport.removeStatusAndMsg(
        WebsiteApiResponseSupport.toMap(jsonObject));
  }

  protected final boolean hasPositiveIntegerParam(String paramName) {
    Object value = params.get(paramName);
    return value instanceof Integer integerValue && integerValue > 0;
  }

  protected final boolean hasNonBlankStringParam(String paramName) {
    Object value = params.get(paramName);
    return value instanceof String stringValue && !stringValue.isBlank();
  }
}
