package net.heimeng.sdk.btapi.api.system;

import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.system.SystemLoadInfo;

/**
 * 获取系统负载信息的API
 * <p>
 * 用于获取服务器的系统负载信息，包括CPU负载、内存使用、磁盘I/O等数据。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SystemLoadApi extends BaseBtApi<SystemLoadInfo> {

    /**
     * 获取API端点路径
     *
     * @return API端点路径
     */
    @Override
    public String getEndpoint() {
        return "plugin?action=a&name=load";
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
     * 解析API响应字符串为SystemLoadInfo对象
     *
     * @param response API响应字符串
     * @return 解析后的SystemLoadInfo对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public SystemLoadInfo parseResponse(String response) {
        try {
            // 首先验证基本响应
            parseBasicResult(response);
            
            // 解析为SystemLoadInfo对象
            return JSONUtil.toBean(response, SystemLoadInfo.class);
        } catch (Exception e) {
            if (e instanceof BtApiException) {
                throw e;
            }
            throw new BtApiException("Failed to parse system load response", e);
        }
    }
}