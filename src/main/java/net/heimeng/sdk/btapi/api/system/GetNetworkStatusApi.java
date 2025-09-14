package net.heimeng.sdk.btapi.api.system;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.NetworkStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取网络状态信息API实现
 * <p>
 * 用于获取宝塔面板的实时网络状态信息，包括CPU使用率、内存使用情况、网络流量、负载等实时信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetNetworkStatusApi extends BaseBtApi<BtResult<NetworkStatus>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "system?action=GetNetWork";
    
    /**
     * 构造函数，创建一个新的GetNetworkStatusApi实例
     */
    public GetNetworkStatusApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 解析API响应字符串为BtResult<NetworkStatus>对象
     * 
     * @param response API响应字符串
     * @return BtResult<NetworkStatus>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<NetworkStatus> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            JSONObject json = JSONUtil.parseObj(response);
            BtResult<NetworkStatus> result = new BtResult<>();
            
            // 检查是否包含status字段（错误响应格式）
            if (json.containsKey("status")) {
                result.setStatus(json.getBool("status", false));
                result.setMsg(json.getStr("msg", ""));
            } else {
                // 没有status字段，说明是成功的响应直接返回了数据
                result.setStatus(true);
                result.setMsg("Success");
            }

            // 解析网络状态信息
            if (result.isSuccess()) {
                NetworkStatus networkStatus = new NetworkStatus();
                
                // 网络流量信息
                networkStatus.setDownTotal(json.getLong("downTotal", 0L));
                networkStatus.setUpTotal(json.getLong("upTotal", 0L));
                networkStatus.setDownPackets(json.getLong("downPackets", 0L));
                networkStatus.setUpPackets(json.getLong("upPackets", 0L));
                networkStatus.setDown(json.getDouble("down", 0.0));
                networkStatus.setUp(json.getDouble("up", 0.0));
                
                // CPU信息
                JSONArray cpuArray = json.getJSONArray("cpu");
                if (cpuArray != null) {
                    List<Double> cpuList = new ArrayList<>();
                    for (int i = 0; i < cpuArray.size(); i++) {
                        cpuList.add(cpuArray.getDouble(i, 0.0));
                    }
                    networkStatus.setCpu(cpuList);
                }
                
                // 内存信息
                JSONObject memObj = json.getJSONObject("mem");
                if (memObj != null) {
                    networkStatus.setMem(memObj);
                }
                
                // 负载信息
                JSONObject loadObj = json.getJSONObject("load");
                if (loadObj != null) {
                    networkStatus.setLoad(loadObj);
                }
                
                result.setData(networkStatus);
            }
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse network status response: " + e.getMessage(), e);
        }
    }
}