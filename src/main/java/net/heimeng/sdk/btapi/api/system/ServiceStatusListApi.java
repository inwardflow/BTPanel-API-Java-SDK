package net.heimeng.sdk.btapi.api.system;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.system.ServiceStatusList;

/**
 * 获取服务状态列表的API
 * <p>
 * 用于获取宝塔面板上所有服务的状态信息，包括Web服务器、数据库、FTP服务器等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class ServiceStatusListApi extends BaseBtApi<ServiceStatusList> {

    /**
     * 获取API端点路径
     *
     * @return API端点路径
     */
    @Override
    public String getEndpoint() {
        return "plugin?action=a&name=servicestatus";
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
     * 解析API响应字符串为ServiceStatusList对象
     *
     * @param response API响应字符串
     * @return 解析后的ServiceStatusList对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public ServiceStatusList parseResponse(String response) {
        try {
            // 解析JSON对象
            JSONObject jsonObject = JSONUtil.parseObj(response);
            
            // 首先验证基本响应状态
            boolean status = jsonObject.getBool("status", false);
            String msg = jsonObject.getStr("msg", "");
            
            // 创建ServiceStatusList对象并设置基本信息
            ServiceStatusList serviceStatusList = new ServiceStatusList(status, msg);
            
            // 如果请求失败，直接返回
            if (!status) {
                return serviceStatusList;
            }
            
            // 尝试从不同的字段路径获取服务列表
            java.util.List<ServiceStatusList.ServiceInfo> services = new java.util.ArrayList<>();
            
            // 情况1: 直接在response中包含services字段
            if (jsonObject.containsKey("services")) {
                services = JSONUtil.toList(jsonObject.getJSONArray("services"), ServiceStatusList.ServiceInfo.class);
            }
            // 情况2: 数据在data字段中
            else if (jsonObject.containsKey("data")) {
                JSONObject dataObj = jsonObject.getJSONObject("data");
                if (dataObj.containsKey("services")) {
                    services = JSONUtil.toList(dataObj.getJSONArray("services"), ServiceStatusList.ServiceInfo.class);
                }
            }
            // 情况3: 直接解析API特定的返回结构（适配宝塔API可能的返回格式）
            else {
                // 尝试将整个对象转换为服务列表
                try {
                    // 这里根据实际API返回格式进行调整
                    // 例如，某些API可能直接返回服务列表数据
                    services = new java.util.ArrayList<>();
                    // 遍历所有字段，尝试找到服务相关信息
                    for (String key : jsonObject.keySet()) {
                        Object value = jsonObject.get(key);
                        if (value instanceof JSONObject) {
                            try {
                                ServiceStatusList.ServiceInfo serviceInfo = JSONUtil.toBean((JSONObject)value, ServiceStatusList.ServiceInfo.class);
                                // 补充可能缺失的信息
                                if (serviceInfo.getName() == null) {
                                    serviceInfo.setName(key);
                                }
                                services.add(serviceInfo);
                            } catch (Exception ignored) {
                                // 如果转换失败，跳过该字段
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // 如果尝试解析失败，保持services为空列表
                }
            }
            
            // 设置服务列表
            serviceStatusList.setServices(services);
            
            return serviceStatusList;
        } catch (Exception e) {
            if (e instanceof BtApiException) {
                throw e;
            }
            throw new BtApiException("Failed to parse service status list response", e);
        }
    }
}