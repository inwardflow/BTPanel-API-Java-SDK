package net.heimeng.sdk.btapi.api.system;

import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.SystemInfo;

/**
 * 获取系统信息API实现
 * <p>
 * 用于获取宝塔面板的系统信息，包括服务器基本信息、CPU使用率、内存使用情况等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetSystemInfoApi extends BaseBtApi<BtResult<SystemInfo>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "system?action=GetSystemTotal";
    
    /**
     * 构造函数，创建一个新的GetSystemInfoApi实例
     */
    public GetSystemInfoApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 解析API响应字符串为BtResult<SystemInfo>对象
     * 
     * @param response API响应字符串
     * @return BtResult<SystemInfo>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<SystemInfo> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            JSONObject json = JSONUtil.parseObj(response);

            BtResult<SystemInfo> result = new BtResult<>();
            
            // 检查是否包含status字段（错误响应格式）
            if (json.containsKey("status")) {
                result.setStatus(json.getBool("status", false));
                result.setMsg(json.getStr("msg", ""));
            } else {
                // 没有status字段，说明是成功的响应直接返回了数据
                result.setStatus(true);
                result.setMsg("Success");
            }

            // 解析系统信息
            if (result.isSuccess()) {
                SystemInfo systemInfo = new SystemInfo();

                // 主机名
                systemInfo.setHostname(json.getStr("system", "Unknown"));

                // 操作系统
                systemInfo.setOs(json.getStr("system", "Unknown"));

                // 内核（目前只能使用 system 字段作为近似）
                systemInfo.setKernel(json.getStr("system", "Unknown"));

                // CPU 使用率
                systemInfo.setCpuUsage(json.getDouble("cpuRealUsed", 0.0));

                // 内存信息（单位：MB）
                systemInfo.setMemoryTotal(json.getLong("memTotal", 0L));
                systemInfo.setMemoryUsed(json.getLong("memRealUsed", 0L));

                // 磁盘信息（暂无数据，设置为 0）
                systemInfo.setDiskTotal(0.0);
                systemInfo.setDiskUsed(0.0);

                // 面板版本
                systemInfo.setPanelVersion(json.getStr("version", "Unknown"));

                result.setData(systemInfo);
            }
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse system info response: " + e.getMessage(), e);
        }
    }
}