package net.heimeng.sdk.btapi.api.ftp;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * FTP 模块中返回布尔结果的 API 公共基类。
 *
 * <p>用于统一处理标准的 {@code status/msg} JSON 响应，减少不同 FTP API 之间重复的解析逻辑。
 */
abstract class AbstractFtpBooleanApi extends BaseBtApi<BtResult<Boolean>> {

  private final String successMessage;
  private final String failureMessage;

  protected AbstractFtpBooleanApi(String endpoint, String successMessage, String failureMessage) {
    super(endpoint, HttpMethod.POST);
    this.successMessage = successMessage;
    this.failureMessage = failureMessage;
  }

  @Override
  public BtResult<Boolean> parseResponse(String response) {
    if (response == null || response.isBlank()) {
      throw new BtApiException("Empty response received");
    }

    String normalizedResponse = response.trim();
    try {
      if (!JSONUtil.isTypeJSON(normalizedResponse)) {
        throw new BtApiException("Invalid JSON response: " + normalizedResponse);
      }

      JSON json = JSONUtil.parse(normalizedResponse);
      if (!(json instanceof JSONObject jsonObject)) {
        throw new BtApiException("FTP API response must be a JSON object");
      }
      if (!jsonObject.containsKey("status")) {
        throw new BtApiException("FTP API response is missing required status field");
      }

      boolean status = jsonObject.getBool("status", false);
      BtResult<Boolean> result = new BtResult<>();
      result.setStatus(status);
      result.setMsg(jsonObject.getStr("msg", status ? successMessage : failureMessage));
      result.setData(status);
      return result;
    } catch (JSONException exception) {
      throw new BtApiException("Invalid JSON response: " + normalizedResponse, exception);
    }
  }

  protected final boolean hasNonBlankParam(String paramName) {
    Object value = params.get(paramName);
    return value instanceof String stringValue && !stringValue.isBlank();
  }

  protected final boolean hasRequiredParams(String... paramNames) {
    for (String paramName : paramNames) {
      if (!hasNonBlankParam(paramName)) {
        return false;
      }
    }
    return true;
  }

  protected final boolean hasNonNegativeNumberParam(String paramName) {
    Object value = params.get(paramName);
    return value instanceof Number numberValue && numberValue.longValue() >= 0L;
  }
}
