package net.heimeng.sdk.btapi.api.ssl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
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

final class SslApiResponseSupport {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  private SslApiResponseSupport() {}

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

  static <T> BtResult<T> successResult(T data, String message) {
    BtResult<T> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg(message == null || message.isBlank() ? "Success" : message);
    result.setData(data);
    return result;
  }

  static <T> BtResult<T> failureResult(T data, String message) {
    BtResult<T> result = new BtResult<>();
    result.setStatus(false);
    result.setMsg(message == null || message.isBlank() ? "Request failed" : message);
    result.setData(data);
    return result;
  }

  static Map<String, Object> toMap(JSONObject jsonObject) {
    Map<String, Object> result = new LinkedHashMap<>();
    for (String key : jsonObject.keySet()) {
      result.put(key, normalizeJsonValue(jsonObject.get(key)));
    }
    return Map.copyOf(result);
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

  static String readOptionalString(Object rawValue) {
    if (rawValue == null) {
      return null;
    }
    if (rawValue instanceof Boolean booleanValue) {
      return booleanValue ? Boolean.TRUE.toString() : null;
    }
    String value = String.valueOf(rawValue).trim();
    return value.isEmpty() ? null : value;
  }

  static Integer toInteger(Object rawValue) {
    if (rawValue instanceof Number numberValue) {
      return numberValue.intValue();
    }
    if (rawValue instanceof String stringValue && !stringValue.isBlank()) {
      try {
        return Integer.parseInt(stringValue);
      } catch (NumberFormatException ignored) {
        return null;
      }
    }
    return null;
  }

  static boolean containsAny(JSONObject jsonObject, String... keys) {
    for (String key : keys) {
      if (jsonObject.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  static Date parseDate(String rawDate) {
    if (rawDate == null || rawDate.isBlank()) {
      return null;
    }

    try {
      LocalDateTime localDateTime = LocalDateTime.parse(rawDate, DATE_TIME_FORMATTER);
      return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    } catch (DateTimeParseException ignored) {
      // Fall through to the date-only format used by the current SSL APIs.
    }

    try {
      LocalDate localDate = LocalDate.parse(rawDate, DATE_FORMATTER);
      return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    } catch (DateTimeParseException ignored) {
      return null;
    }
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
