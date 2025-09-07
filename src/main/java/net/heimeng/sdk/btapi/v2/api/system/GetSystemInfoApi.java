package net.heimeng.sdk.btapi.v2.api.system;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.v2.api.BaseBtApi;
import net.heimeng.sdk.btapi.v2.exception.BtApiException;
import net.heimeng.sdk.btapi.v2.model.BtResult;
import net.heimeng.sdk.btapi.v2.model.system.SystemInfo;

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
        super(ENDPOINT, HttpMethod.GET);
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
            // 检查响应是否为有效的JSON
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }
            
            // 解析JSON对象
            JSONObject json = JSONUtil.parseObj(response);
            
            // 创建结果对象
            BtResult<SystemInfo> result = new BtResult<>();
            
            // GetSystemTotal接口没有status字段，HTTP 200响应即表示成功
            result.setStatus(true);
            result.setMsg("Success");
            
            // 创建SystemInfo对象并从根节点直接提取信息
            SystemInfo systemInfo = new SystemInfo();
            
            // 从根节点提取系统信息
            // 尝试从system字段提取有意义的信息作为主机名，或者使用服务器IP作为备选
            String systemInfoStr = json.getStr("system", "");
            if (!systemInfoStr.isEmpty()) {
                systemInfo.setHostname("Unknown");
            }            
            // 操作系统信息
            systemInfo.setOs(json.getStr("system", "Unknown"));
            
            // 内核信息 - 从system字段中提取
            String systemStr = json.getStr("system", "");
            if (!systemStr.isEmpty()) {
                // 提取系统名称作为内核信息
                systemInfo.setKernel(systemStr);
            } else {
                systemInfo.setKernel("Unknown");
            }
            
            // CPU使用率
            systemInfo.setCpuUsage(json.getDouble("cpuRealUsed", 0.0));
            
            // 内存信息（单位：MB） - SystemInfo类定义的就是MB单位
            systemInfo.setMemoryTotal(json.getLong("memTotal", 0L)); // 直接使用API返回的MB值
            systemInfo.setMemoryUsed(json.getLong("memRealUsed", 0L)); // 直接使用API返回的MB值
            
            // 磁盘信息 - API响应中没有直接的diskTotal和diskUsed字段
            // 设置为0，或者如果将来API提供这些字段，可以在这里添加
            systemInfo.setDiskTotal(0.0);
            systemInfo.setDiskUsed(0.0);
            
            // 面板版本
            systemInfo.setPanelVersion(json.getStr("version", "Unknown"));
            
            // 设置数据到结果中
            result.setData(systemInfo);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse system info response: " + e.getMessage(), e);
        }
    }
}