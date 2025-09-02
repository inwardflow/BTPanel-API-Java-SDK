package net.heimeng.sdk.btapi.core;

import java.util.Map;

/**
 * 宝塔API接口，定义了API请求的基本特性
 * <p>
 * 所有具体的宝塔API请求都应该实现此接口，提供端点、HTTP方法、参数和响应解析逻辑。
 * </p>
 *
 * @param <T> API返回数据类型
 * @author InwardFlow
 * @since 2.0.0
 */
public interface BtApi<T> {

    /**
     * 获取API端点路径（不包含基础URL）
     *
     * @return API端点路径
     */
    String getEndpoint();

    /**
     * 获取HTTP请求方法
     *
     * @return HTTP请求方法
     */
    HttpMethod getMethod();

    /**
     * 获取请求参数
     *
     * @return 请求参数字典
     */
    Map<String, Object> getParams();

    /**
     * 解析API响应字符串为指定类型的对象
     *
     * @param response API响应字符串
     * @return 解析后的对象
     * @throws net.heimeng.sdk.btapi.exception.BtApiException 当解析失败时抛出
     */
    T parseResponse(String response);

    /**
     * HTTP请求方法枚举
     */
    enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH
    }
}