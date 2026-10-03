package net.heimeng.sdk.btapi.api.system;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.PanelUpdateInfo;

/**
 * 检查面板更新信息的 API。
 *
 * <p>该接口在不同面板版本中返回结构并不完全一致。较新的版本可能不再返回 {@code status} 字段，而是直接返回版本与升级说明。这里将“请求是否成功”与 “是否存在更新”分离处理。
 */
public class CheckPanelUpdateApi extends BaseBtApi<BtResult<PanelUpdateInfo>> {

  private static final String ENDPOINT = "ajax?action=UpdatePanel";
  private static final String SUCCESS_MESSAGE = "Success";

  public CheckPanelUpdateApi(boolean forceCheck) {
    super(ENDPOINT, HttpMethod.POST);
    if (forceCheck) {
      addParam("check", true);
    }
  }

  public CheckPanelUpdateApi() {
    this(false);
  }

  public CheckPanelUpdateApi setForceCheck(boolean forceCheck) {
    if (forceCheck) {
      addParam("check", true);
    } else {
      removeParam("check");
    }
    return this;
  }

  @Override
  public BtResult<PanelUpdateInfo> parseResponse(String response) {
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
        throw new BtApiException("Panel update response must be a JSON object");
      }

      PanelUpdateInfo updateInfo = new PanelUpdateInfo();
      boolean updateAvailable =
          jsonObject.containsKey("status")
              ? jsonObject.getBool("status", false)
              : jsonObject.containsKey("version") || jsonObject.containsKey("updateMsg");
      updateInfo.setStatus(updateAvailable);
      updateInfo.setVersion(jsonObject.getStr("version", ""));
      updateInfo.setUpdateMsg(jsonObject.getStr("updateMsg", ""));

      BtResult<PanelUpdateInfo> result = new BtResult<>();
      result.setStatus(true);
      result.setMsg(jsonObject.getStr("msg", SUCCESS_MESSAGE));
      result.setData(updateInfo);
      return result;
    } catch (JSONException exception) {
      throw new BtApiException("Invalid JSON response: " + normalizedResponse, exception);
    }
  }
}
