package net.heimeng.sdk.btapi.api.system;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 查询安装任务数量的 API。
 *
 * <p>根据宝塔接口文档，该接口成功时通常直接返回纯数字文本，例如 {@code 0}。 在异常场景下，也可能返回包含 {@code status} 与 {@code msg} 的 JSON
 * 对象。
 */
public class GetTaskCountApi extends BaseBtApi<BtResult<Integer>> {

  private static final String ENDPOINT = "ajax?action=GetTaskCount";
  private static final String SUCCESS_MESSAGE = "Success";

  public GetTaskCountApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  @Override
  public BtResult<Integer> parseResponse(String response) {
    if (response == null || response.isBlank()) {
      throw new BtApiException("Empty response received");
    }

    String normalizedResponse = response.trim();
    try {
      if (isIntegerLiteral(normalizedResponse)) {
        return successResult(Integer.parseInt(normalizedResponse));
      }

      if (isQuotedIntegerLiteral(normalizedResponse)) {
        return successResult(Integer.parseInt(stripQuotes(normalizedResponse)));
      }

      if (!JSONUtil.isTypeJSON(normalizedResponse)) {
        throw new BtApiException("Invalid task count response: " + normalizedResponse);
      }

      JSON json = JSONUtil.parse(normalizedResponse);
      if (!(json instanceof JSONObject jsonObject)) {
        throw new BtApiException("Task count response must be a JSON object");
      }

      if (jsonObject.containsKey("status")) {
        return parseStatusResponse(jsonObject);
      }

      if (jsonObject.containsKey("data") || jsonObject.containsKey("count")) {
        return successResult(readTaskCount(jsonObject));
      }

      throw new BtApiException("Task count response does not contain a supported payload");
    } catch (JSONException exception) {
      throw new BtApiException("Invalid JSON response: " + normalizedResponse, exception);
    } catch (NumberFormatException exception) {
      throw new BtApiException("Failed to parse task count: " + normalizedResponse, exception);
    }
  }

  private BtResult<Integer> parseStatusResponse(JSONObject jsonObject) {
    BtResult<Integer> result = new BtResult<>();
    boolean success = jsonObject.getBool("status", false);
    String message =
        jsonObject.getStr("msg", success ? SUCCESS_MESSAGE : "Failed to fetch task count");

    result.setStatus(success);
    result.setMsg(message);
    if (success) {
      result.setData(readTaskCount(jsonObject));
    }
    return result;
  }

  private int readTaskCount(JSONObject jsonObject) {
    if (jsonObject.containsKey("data")) {
      return parseIntegerValue(jsonObject.get("data"));
    }
    if (jsonObject.containsKey("count")) {
      return parseIntegerValue(jsonObject.get("count"));
    }
    throw new BtApiException("Task count payload is missing required data field");
  }

  private int parseIntegerValue(Object rawValue) {
    if (rawValue == null) {
      throw new BtApiException("Task count payload cannot be null");
    }
    if (rawValue instanceof Number number) {
      return number.intValue();
    }
    String value = String.valueOf(rawValue).trim();
    if (!isIntegerLiteral(value)) {
      throw new BtApiException("Task count payload is not a valid integer: " + value);
    }
    return Integer.parseInt(value);
  }

  private boolean isIntegerLiteral(String value) {
    return value.matches("[-+]?\\d+");
  }

  private boolean isQuotedIntegerLiteral(String value) {
    return value.length() >= 2
        && value.startsWith("\"")
        && value.endsWith("\"")
        && isIntegerLiteral(stripQuotes(value));
  }

  private String stripQuotes(String value) {
    return value.substring(1, value.length() - 1).trim();
  }

  private BtResult<Integer> successResult(int taskCount) {
    BtResult<Integer> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg(SUCCESS_MESSAGE);
    result.setData(taskCount);
    return result;
  }
}
