package net.heimeng.sdk.btapi.api.system;

import java.util.ArrayList;
import java.util.List;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.DiskInfo;

/**
 * 查询磁盘分区信息的 API。
 *
 * <p>根据宝塔接口文档，该接口成功时通常直接返回数组；异常场景下也可能返回带 {@code status}/{@code msg} 的 JSON 对象。
 */
public class GetDiskInfoApi extends BaseBtApi<BtResult<List<DiskInfo>>> {

  private static final String ENDPOINT = "system?action=GetDiskInfo";
  private static final String SUCCESS_MESSAGE = "Success";

  public GetDiskInfoApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  @Override
  public BtResult<List<DiskInfo>> parseResponse(String response) {
    if (response == null || response.isBlank()) {
      throw new BtApiException("Empty response received");
    }

    String normalizedResponse = response.trim();
    try {
      if (!JSONUtil.isTypeJSON(normalizedResponse)) {
        throw new BtApiException("Invalid JSON response: " + normalizedResponse);
      }

      JSON json = JSONUtil.parse(normalizedResponse);
      if (json instanceof JSONArray jsonArray) {
        return successResult(parseDiskArray(jsonArray), SUCCESS_MESSAGE);
      }
      if (json instanceof JSONObject jsonObject) {
        return parseObjectResponse(jsonObject);
      }

      throw new BtApiException("Unsupported disk info response format");
    } catch (JSONException exception) {
      throw new BtApiException("Invalid JSON response: " + normalizedResponse, exception);
    }
  }

  private BtResult<List<DiskInfo>> parseObjectResponse(JSONObject jsonObject) {
    if (jsonObject.containsKey("status")) {
      boolean success = jsonObject.getBool("status", false);
      String message =
          jsonObject.getStr("msg", success ? SUCCESS_MESSAGE : "Failed to fetch disk info");
      if (!success) {
        return failureResult(message);
      }

      Object rawData = jsonObject.get("data");
      if (!(rawData instanceof JSONArray jsonArray)) {
        throw new BtApiException("Disk info response is missing required data array");
      }
      return successResult(parseDiskArray(jsonArray), message);
    }

    Object rawData = jsonObject.get("data");
    if (rawData instanceof JSONArray jsonArray) {
      return successResult(parseDiskArray(jsonArray), SUCCESS_MESSAGE);
    }

    throw new BtApiException("Disk info response must be a JSON array or object containing data");
  }

  private List<DiskInfo> parseDiskArray(JSONArray jsonArray) {
    List<DiskInfo> diskInfos = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject diskJson = jsonArray.getJSONObject(i);
      if (diskJson == null) {
        continue;
      }

      DiskInfo diskInfo = new DiskInfo();
      diskInfo.setPath(diskJson.getStr("path", ""));
      diskInfo.setInodes(readStringArray(diskJson.getJSONArray("inodes")));
      diskInfo.setSize(readStringArray(diskJson.getJSONArray("size")));
      diskInfos.add(diskInfo);
    }
    return List.copyOf(diskInfos);
  }

  private List<String> readStringArray(JSONArray jsonArray) {
    if (jsonArray == null) {
      return List.of();
    }

    List<String> values = new ArrayList<>(jsonArray.size());
    for (int i = 0; i < jsonArray.size(); i++) {
      Object value = jsonArray.get(i);
      values.add(value == null ? "" : String.valueOf(value));
    }
    return List.copyOf(values);
  }

  private BtResult<List<DiskInfo>> successResult(List<DiskInfo> diskInfos, String message) {
    BtResult<List<DiskInfo>> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg(message == null || message.isBlank() ? SUCCESS_MESSAGE : message);
    result.setData(diskInfos);
    return result;
  }

  private BtResult<List<DiskInfo>> failureResult(String message) {
    BtResult<List<DiskInfo>> result = new BtResult<>();
    result.setStatus(false);
    result.setMsg(message);
    result.setData(List.of());
    return result;
  }
}
