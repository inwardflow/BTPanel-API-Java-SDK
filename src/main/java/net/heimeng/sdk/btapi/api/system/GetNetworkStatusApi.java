package net.heimeng.sdk.btapi.api.system;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.NetworkStatus;

/**
 * 查询实时网络状态的 API。
 *
 * <p>成功时通常直接返回带网络统计字段的 JSON 对象；异常场景下也可能返回带 {@code status}/{@code msg} 的包装对象。
 */
public class GetNetworkStatusApi extends BaseBtApi<BtResult<NetworkStatus>> {

  private static final String ENDPOINT = "system?action=GetNetWork";
  private static final String SUCCESS_MESSAGE = "Success";

  public GetNetworkStatusApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  @Override
  public BtResult<NetworkStatus> parseResponse(String response) {
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
        throw new BtApiException("Network status response must be a JSON object");
      }

      return parseObjectResponse(jsonObject);
    } catch (JSONException exception) {
      throw new BtApiException("Invalid JSON response: " + normalizedResponse, exception);
    }
  }

  private BtResult<NetworkStatus> parseObjectResponse(JSONObject jsonObject) {
    BtResult<NetworkStatus> result = new BtResult<>();

    JSONObject payload = jsonObject;
    if (jsonObject.containsKey("status")) {
      boolean success = jsonObject.getBool("status", false);
      result.setStatus(success);
      result.setMsg(
          jsonObject.getStr("msg", success ? SUCCESS_MESSAGE : "Failed to fetch network status"));
      if (!success) {
        return result;
      }

      if (jsonObject.containsKey("data")) {
        Object rawData = jsonObject.get("data");
        if (!(rawData instanceof JSONObject dataObject)) {
          throw new BtApiException("Network status response data must be a JSON object");
        }
        payload = dataObject;
      }
    } else {
      result.setStatus(true);
      result.setMsg(SUCCESS_MESSAGE);
    }

    if (!containsPayloadField(payload)) {
      throw new BtApiException("Network status response does not contain a supported payload");
    }

    result.setData(parseNetworkStatus(payload));
    return result;
  }

  private boolean containsPayloadField(JSONObject jsonObject) {
    return jsonObject.containsKey("downTotal")
        || jsonObject.containsKey("upTotal")
        || jsonObject.containsKey("cpu")
        || jsonObject.containsKey("mem")
        || jsonObject.containsKey("load");
  }

  private NetworkStatus parseNetworkStatus(JSONObject jsonObject) {
    NetworkStatus networkStatus = new NetworkStatus();
    networkStatus.setDownTotal(jsonObject.getLong("downTotal", 0L));
    networkStatus.setUpTotal(jsonObject.getLong("upTotal", 0L));
    networkStatus.setDownPackets(jsonObject.getLong("downPackets", 0L));
    networkStatus.setUpPackets(jsonObject.getLong("upPackets", 0L));
    networkStatus.setDown(jsonObject.getDouble("down", 0.0));
    networkStatus.setUp(jsonObject.getDouble("up", 0.0));
    networkStatus.setCpu(readDoubleArray(jsonObject.getJSONArray("cpu")));
    networkStatus.setMem(readMap(jsonObject.getJSONObject("mem")));
    networkStatus.setLoad(readMap(jsonObject.getJSONObject("load")));
    return networkStatus;
  }

  private List<Double> readDoubleArray(JSONArray jsonArray) {
    if (jsonArray == null) {
      return List.of();
    }

    List<Double> values = new ArrayList<>(jsonArray.size());
    for (int i = 0; i < jsonArray.size(); i++) {
      values.add(jsonArray.getDouble(i, 0.0));
    }
    return List.copyOf(values);
  }

  private Map<String, Object> readMap(JSONObject jsonObject) {
    if (jsonObject == null) {
      return Map.of();
    }
    return Map.copyOf(new LinkedHashMap<>(jsonObject));
  }
}
