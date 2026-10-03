package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * Website 模块中返回布尔结果的 API 公共基类。
 *
 * <p>统一处理标准 {@code status/msg} 响应解析，并提供常用参数校验与布尔标志位辅助方法。
 */
abstract class AbstractWebsiteBooleanApi extends BaseBtApi<BtResult<Boolean>> {

  private final String successMessage;
  private final String failureMessage;

  protected AbstractWebsiteBooleanApi(
      String endpoint, String successMessage, String failureMessage) {
    super(endpoint, HttpMethod.POST);
    this.successMessage = successMessage;
    this.failureMessage = failureMessage;
  }

  @Override
  public BtResult<Boolean> parseResponse(String response) {
    JSON json = WebsiteApiResponseSupport.parseJsonResponse(response, endpoint);
    if (!(json instanceof JSONObject jsonObject)) {
      throw new BtApiException("Website API response must be a JSON object");
    }
    if (!jsonObject.containsKey("status")) {
      throw new BtApiException("Website API response is missing required status field");
    }

    boolean status = jsonObject.getBool("status", false);
    BtResult<Boolean> result = new BtResult<>();
    result.setStatus(status);
    result.setMsg(jsonObject.getStr("msg", status ? successMessage : failureMessage));
    result.setData(status);
    return result;
  }

  protected final boolean hasRequiredParams(String... paramNames) {
    for (String paramName : paramNames) {
      if (!hasNonBlankStringParam(paramName)) {
        return false;
      }
    }
    return true;
  }

  protected final boolean hasParam(String paramName) {
    return params.containsKey(paramName);
  }

  protected final boolean hasNonBlankStringParam(String paramName) {
    Object value = params.get(paramName);
    return value instanceof String stringValue && !stringValue.isBlank();
  }

  protected final boolean hasPositiveIntegerParam(String paramName) {
    Object value = params.get(paramName);
    return value instanceof Number numberValue && numberValue.intValue() > 0;
  }

  protected final boolean hasNonNegativeNumberParam(String paramName) {
    Object value = params.get(paramName);
    return value instanceof Number numberValue && numberValue.intValue() >= 0;
  }

  protected final boolean hasOptionalNonNegativeNumberParam(String paramName) {
    return !params.containsKey(paramName) || hasNonNegativeNumberParam(paramName);
  }

  protected final boolean hasBooleanFlagIntParam(String paramName) {
    Object value = params.get(paramName);
    if (!(value instanceof Number numberValue)) {
      return false;
    }
    int flag = numberValue.intValue();
    return flag == 0 || flag == 1;
  }

  protected final boolean hasOptionalBooleanFlagIntParam(String paramName) {
    return !params.containsKey(paramName) || hasBooleanFlagIntParam(paramName);
  }

  protected final void requireNonBlank(String value, String paramName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(paramName + " cannot be blank");
    }
  }

  protected final void requireNonNull(Object value, String paramName) {
    if (value == null) {
      throw new IllegalArgumentException(paramName + " cannot be null");
    }
  }

  protected final void requirePositiveInteger(Integer value, String paramName) {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException(paramName + " must be positive");
    }
  }

  protected final void putOptionalBooleanFlag(String paramName, Boolean value) {
    if (value == null) {
      removeParam(paramName);
      return;
    }
    addParam(paramName, value ? 1 : 0);
  }

  protected final void putOptionalNonNegativeInteger(String paramName, Integer value) {
    if (value == null) {
      removeParam(paramName);
      return;
    }
    if (value < 0) {
      throw new IllegalArgumentException(paramName + " cannot be negative");
    }
    addParam(paramName, value);
  }
}
