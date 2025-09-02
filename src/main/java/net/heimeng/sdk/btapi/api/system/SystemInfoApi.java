package net.heimeng.sdk.btapi.api.system;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.system.SystemInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取宝塔面板系统信息的API
 * <p>
 * 用于获取服务器的系统信息，包括操作系统版本、CPU、内存、磁盘使用情况等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SystemInfoApi extends BaseBtApi<SystemInfo> {

    /**
     * 获取API端点路径
     *
     * @return API端点路径
     */
    @Override
    public String getEndpoint() {
        return "system?action=GetNetWork";
    }

    /**
     * 获取HTTP请求方法
     *
     * @return HTTP请求方法
     */
    @Override
    public HttpMethod getMethod() {
        return HttpMethod.GET;
    }

    /**
     * 解析API响应字符串为SystemInfo对象
     *
     * @param response API响应字符串
     * @return 解析后的SystemInfo对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public SystemInfo parseResponse(String response) {
        try {
            // 解析JSON响应
            JSONObject jsonObject = JSONUtil.parseObj(response);
            
            // 确定响应状态 - 如果包含status字段就使用，否则默认为true（因为HTTP状态码是200）
            Boolean status = jsonObject.containsKey("status") ? jsonObject.getBool("status", true) : true;
            String msg = jsonObject.getStr("msg", "");
            
            // 创建SystemInfo对象并设置状态和消息
            SystemInfo systemInfo = new SystemInfo(status, msg);
            
            // 从响应中提取系统信息
            // 操作系统信息
            if (jsonObject.containsKey("system")) {
                systemInfo.setOsName(jsonObject.getStr("system"));
            }
            
            // CPU信息
            if (jsonObject.containsKey("cpu_times")) {
                JSONObject cpuTimes = jsonObject.getJSONObject("cpu_times");
                systemInfo.setCpuUsage(cpuTimes.getDouble("user", 0.0) + cpuTimes.getDouble("system", 0.0));
            }
            
            if (jsonObject.containsKey("cpu")) {
                List<?> cpuInfo = jsonObject.getBeanList("cpu", Object.class);
                if (cpuInfo.size() >= 4) {
                    systemInfo.setCpuModel(cpuInfo.get(3).toString());
                }
                if (cpuInfo.size() >= 5) {
                    systemInfo.setCpuCores(Integer.parseInt(cpuInfo.get(4).toString()));
                }
            }
            
            // 内存信息
            if (jsonObject.containsKey("mem")) {
                JSONObject memInfo = jsonObject.getJSONObject("mem");
                systemInfo.setMemoryTotal(memInfo.getLong("memTotal", 0L));
                systemInfo.setMemoryUsage(memInfo.getDouble("memRealUsed", 0.0) / memInfo.getDouble("memTotal", 1.0) * 100);
            }
            
            // 磁盘信息
            if (jsonObject.containsKey("disk")) {
                List<JSONObject> diskList = jsonObject.getBeanList("disk", JSONObject.class);
                List<SystemInfo.DiskInfo> disks = new ArrayList<>();
                
                for (JSONObject diskJson : diskList) {
                    SystemInfo.DiskInfo diskInfo = new SystemInfo.DiskInfo();
                    diskInfo.setDevice(diskJson.getStr("filesystem"));
                    diskInfo.setMountPoint(diskJson.getStr("path"));
                    
                    // 解析磁盘大小信息
                    List<?> sizeInfo = diskJson.getBeanList("size", Object.class);
                    if (sizeInfo.size() >= 5) {
                        try {
                            String totalStr = sizeInfo.get(0).toString().replace(" GB", "");
                            diskInfo.setTotal((long) (Double.parseDouble(totalStr) * 1024 * 1024 * 1024));
                        } catch (Exception e) {
                            // 忽略解析错误
                        }
                    }
                    
                    disks.add(diskInfo);
                }
                
                systemInfo.setDisks(disks);
            }
            
            // 网络信息
            if (jsonObject.containsKey("network")) {
                JSONObject networkInfo = jsonObject.getJSONObject("network");
                List<SystemInfo.NetworkInfo> networks = new ArrayList<>();
                
                for (String netName : networkInfo.keySet()) {
                    if (!netName.equals("upTotal") && !netName.equals("downTotal") && 
                        !netName.equals("up") && !netName.equals("down") &&
                        !netName.equals("upPackets") && !netName.equals("downPackets")) {
                        
                        SystemInfo.NetworkInfo netInfo = new SystemInfo.NetworkInfo();
                        netInfo.setName(netName);
                        
                        JSONObject netData = networkInfo.getJSONObject(netName);
                        netInfo.setInBytes(netData.getLong("downTotal", 0L));
                        netInfo.setOutBytes(netData.getLong("upTotal", 0L));
                        
                        networks.add(netInfo);
                    }
                }
                
                systemInfo.setNetworks(networks);
            }
            
            // 宝塔面板信息
            if (jsonObject.containsKey("version")) {
                systemInfo.setBtVersion(jsonObject.getStr("version"));
            }
            
            // 服务器运行时间
            if (jsonObject.containsKey("time")) {
                systemInfo.setUptime(jsonObject.getStr("time"));
            }
            
            return systemInfo;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse system info response", e);
        }
    }
}