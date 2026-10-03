package net.heimeng.sdk.btapi.api.website;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * Website 模块响应解析辅助类。
 *
 * <p>集中处理 JSON 解析、结果对象构建以及 Map/List 的递归转换，避免查询型 API 在细节上各自维护一套逻辑。
 */
final class WebsiteApiResponseSupport {

  private WebsiteApiResponseSupport() {}

  static boolean isJsonPayload(String response) {
    return response != null && !response.isBlank() && JSONUtil.isTypeJSON(response.trim());
  }

  static JSON parseJsonResponse(String response, String responseName) {
    if (response == null || response.isBlank()) {
      throw new BtApiException("Empty response received");
    }

    String normalizedResponse = response.trim();
    try {
      if (!JSONUtil.isTypeJSON(normalizedResponse)) {
        throw new BtApiException("Invalid JSON response: " + normalizedResponse);
      }
      return JSONUtil.parse(normalizedResponse);
    } catch (JSONException exception) {
      throw new BtApiException(
          "Invalid JSON response for " + responseName + ": " + normalizedResponse, exception);
    }
  }

  static Map<String, Object> toMap(JSONObject jsonObject) {
    Map<String, Object> result = new LinkedHashMap<>();
    for (String key : jsonObject.keySet()) {
      result.put(key, normalizeJsonValue(jsonObject.get(key)));
    }
    return result;
  }

  static List<Map<String, Object>> toMapList(JSONArray jsonArray) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject jsonObject = jsonArray.getJSONObject(i);
      if (jsonObject != null) {
        result.add(toMap(jsonObject));
      }
    }
    return List.copyOf(result);
  }

  static <T> BtResult<List<T>> successListResult(List<T> data, String message) {
    BtResult<List<T>> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg(message == null || message.isBlank() ? "Success" : message);
    result.setData(data);
    return result;
  }

  static <T> BtResult<List<T>> failureListResult(String message) {
    BtResult<List<T>> result = new BtResult<>();
    result.setStatus(false);
    result.setMsg(message);
    result.setData(List.of());
    return result;
  }

  static BtResult<Map<String, Object>> successMapResult(Map<String, Object> data, String message) {
    BtResult<Map<String, Object>> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg(message == null || message.isBlank() ? "Success" : message);
    result.setData(data);
    return result;
  }

  static BtResult<Map<String, Object>> failureMapResult(String message) {
    BtResult<Map<String, Object>> result = new BtResult<>();
    result.setStatus(false);
    result.setMsg(message);
    result.setData(Map.of());
    return result;
  }

  static BtResult<String> successStringResult(String data, String message) {
    BtResult<String> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg(message == null || message.isBlank() ? "Success" : message);
    result.setData(data);
    return result;
  }

  static BtResult<String> failureStringResult(String message) {
    BtResult<String> result = new BtResult<>();
    result.setStatus(false);
    result.setMsg(message);
    result.setData("");
    return result;
  }

  static String stringifyJsonValue(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof JSONObject || value instanceof JSONArray) {
      return JSONUtil.toJsonStr(value);
    }
    return String.valueOf(value);
  }

  static Map<String, Object> removeStatusAndMsg(Map<String, Object> payload) {
    Map<String, Object> copy = new LinkedHashMap<>(payload);
    copy.remove("status");
    copy.remove("msg");
    return Map.copyOf(copy);
  }

  private static Object normalizeJsonValue(Object value) {
    if (value instanceof JSONObject jsonObject) {
      return toMap(jsonObject);
    }
    if (value instanceof JSONArray jsonArray) {
      return toList(jsonArray);
    }
    return value;
  }

  private static List<Object> toList(JSONArray jsonArray) {
    List<Object> result = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      result.add(normalizeJsonValue(jsonArray.get(i)));
    }
    return List.copyOf(result);
  }
}
