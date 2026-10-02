package net.heimeng.sdk.btapi.api.file;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 获取文件内容的 API。
 *
 * <p>该接口既可能返回标准的 {@code status/msg/data} JSON 包装结果，也可能直接返回原始文件内容。
 */
public class GetFileContentApi extends BaseBtApi<BtResult<String>> {

  private static final String ENDPOINT = "files?action=GetFileBody";

  public GetFileContentApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  public GetFileContentApi setPath(String path) {
    addParam("path", path);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return params.get("path") instanceof String path && !path.isBlank();
  }

  @Override
  public BtResult<String> parseResponse(String response) {
    if (response == null || response.isBlank()) {
      throw new BtApiException("Empty response received");
    }

    String normalizedResponse = response.trim();
    try {
      if (!JSONUtil.isTypeJSON(normalizedResponse)) {
        return rawContentResult(response);
      }

      JSON json = JSONUtil.parse(normalizedResponse);
      if (!(json instanceof JSONObject jsonObject) || !jsonObject.containsKey("status")) {
        return rawContentResult(response);
      }

      boolean status = jsonObject.getBool("status", false);
      BtResult<String> result = new BtResult<>();
      result.setStatus(status);
      result.setMsg(jsonObject.getStr("msg", status ? "获取成功" : "获取失败"));
      result.setData(jsonObject.getStr("data", ""));
      return result;
    } catch (JSONException exception) {
      throw new BtApiException("Invalid JSON response: " + normalizedResponse, exception);
    }
  }

  private BtResult<String> rawContentResult(String response) {
    BtResult<String> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg("获取成功");
    result.setData(response);
    return result;
  }
}
