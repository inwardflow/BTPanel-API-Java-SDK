package net.heimeng.sdk.btapi.api.website;

import java.util.Map;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * Website 模块中返回文本内容的查询型 API 公共基类。
 *
 * <p>兼容两类常见返回：
 *
 * <ul>
 *   <li>标准包装 JSON：{@code status/msg/data}
 *   <li>直接返回原始文本或非包装 JSON 文本
 * </ul>
 */
abstract class AbstractWebsiteTextQueryApi extends BaseBtApi<BtResult<String>> {

  private final String successMessage;
  private final String failureMessage;

  protected AbstractWebsiteTextQueryApi(
      String endpoint, String successMessage, String failureMessage) {
    super(endpoint, HttpMethod.POST);
    this.successMessage = successMessage;
    this.failureMessage = failureMessage;
  }

  @Override
  public BtResult<String> parseResponse(String response) {
    if (response == null || response.isBlank()) {
      throw new BtApiException("Empty response received");
    }

    if (!WebsiteApiResponseSupport.isJsonPayload(response)) {
      return WebsiteApiResponseSupport.successStringResult(response, successMessage);
    }

    JSON json = WebsiteApiResponseSupport.parseJsonResponse(response, endpoint);
    if (!(json instanceof JSONObject jsonObject) || !jsonObject.containsKey("status")) {
      return WebsiteApiResponseSupport.successStringResult(response, successMessage);
    }

    boolean status = jsonObject.getBool("status", false);
    if (!status) {
      return WebsiteApiResponseSupport.failureStringResult(
          jsonObject.getStr("msg", failureMessage));
    }

    Object rawData = jsonObject.get("data");
    if (rawData != null) {
      return WebsiteApiResponseSupport.successStringResult(
          WebsiteApiResponseSupport.stringifyJsonValue(rawData),
          jsonObject.getStr("msg", successMessage));
    }

    Map<String, Object> payload =
        WebsiteApiResponseSupport.removeStatusAndMsg(WebsiteApiResponseSupport.toMap(jsonObject));
    if (!payload.isEmpty()) {
      return WebsiteApiResponseSupport.successStringResult(
          WebsiteApiResponseSupport.stringifyJsonValue(payload),
          jsonObject.getStr("msg", successMessage));
    }

    throw new BtApiException("Website text response is missing required data field");
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
