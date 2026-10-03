package net.heimeng.sdk.btapi.api.website;

import java.util.List;
import java.util.Map;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * Website 模块中返回 {@code List<Map<String, Object>>} 的查询型 API 公共基类。
 *
 * <p>兼容面板直接返回数组，以及包装在指定字段中的列表响应。
 */
abstract class AbstractWebsiteMapListQueryApi
    extends BaseBtApi<BtResult<List<Map<String, Object>>>> {

  private final String responseName;
  private final String successMessage;
  private final String failureMessage;
  private final String dataFieldName;
  private final boolean allowDirectArrayResponse;

  protected AbstractWebsiteMapListQueryApi(
      String endpoint,
      String responseName,
      String successMessage,
      String failureMessage,
      String dataFieldName,
      boolean allowDirectArrayResponse) {
    super(endpoint, HttpMethod.POST);
    this.responseName = responseName;
    this.successMessage = successMessage;
    this.failureMessage = failureMessage;
    this.dataFieldName = dataFieldName;
    this.allowDirectArrayResponse = allowDirectArrayResponse;
  }

  @Override
  public BtResult<List<Map<String, Object>>> parseResponse(String response) {
    JSON json = WebsiteApiResponseSupport.parseJsonResponse(response, responseName);
    if (json instanceof JSONArray jsonArray) {
      if (!allowDirectArrayResponse) {
        throw new BtApiException(responseName + " response must be a JSON object");
      }
      return WebsiteApiResponseSupport.successListResult(
          WebsiteApiResponseSupport.toMapList(jsonArray), successMessage);
    }
    if (!(json instanceof JSONObject jsonObject)) {
      throw new BtApiException(responseName + " response must be a JSON object or array");
    }
    if (jsonObject.containsKey("status") && !jsonObject.getBool("status", false)) {
      return WebsiteApiResponseSupport.failureListResult(jsonObject.getStr("msg", failureMessage));
    }

    JSONArray dataArray = jsonObject.getJSONArray(dataFieldName);
    if (dataArray == null) {
      throw new BtApiException(
          responseName + " response is missing required " + dataFieldName + " array");
    }

    return WebsiteApiResponseSupport.successListResult(
        WebsiteApiResponseSupport.toMapList(dataArray), jsonObject.getStr("msg", successMessage));
  }

  protected final boolean hasPositiveIntegerParam(String paramName) {
    Object value = params.get(paramName);
    return value instanceof Integer integerValue && integerValue > 0;
  }

  protected final boolean hasOptionalPositiveIntegerParam(String paramName) {
    return !params.containsKey(paramName) || hasPositiveIntegerParam(paramName);
  }

  protected final boolean hasOptionalStringParam(String paramName) {
    return !params.containsKey(paramName) || params.get(paramName) instanceof String;
  }
}
