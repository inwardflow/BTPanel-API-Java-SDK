package net.heimeng.sdk.btapi.interceptor;

import lombok.Getter;
import lombok.Setter;
import net.heimeng.sdk.btapi.api.BtApi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 请求上下文类，包含请求和响应的相关信息
 * <p>
 * 该类提供了对请求参数、请求头、响应结果等的访问和修改能力，用于拦截器处理请求和响应。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class RequestContext {
    
    /**
     * API接口实例
     */
    @Getter
    private final BtApi<?> api;
    
    /**
     * 请求参数
     */
    @Getter
    private final Map<String, Object> params;
    
    /**
     * 请求头
     */
    @Getter
    private final Map<String, String> headers;
    
    /**
     * HTTP状态码
     */
    @Getter
    @Setter
    private int statusCode;
    
    /**
     * 响应体
     */
    @Getter
    @Setter
    private String responseBody;
    
    /**
     * API返回结果
     */
    @Getter
    @Setter
    private Object result;
    
    /**
     * 异常信息
     */
    @Getter
    @Setter
    private Exception exception;
    
    /**
     * 是否已被取消
     */
    @Getter
    @Setter
    private boolean canceled;
    
    /**
     * 是否强制重试
     */
    @Getter
    @Setter
    private boolean forceRetry = false;
    
    /**
     * 构造函数，创建一个新的RequestContext实例
     * 
     * @param api API接口实例
     */
    public RequestContext(BtApi<?> api) {
        this.api = api;
        this.params = new HashMap<>(api.getParams());
        this.headers = new HashMap<>();
        this.statusCode = -1;
        this.responseBody = null;
        this.result = null;
        this.exception = null;
        this.canceled = false;
    }
    
    /**
     * 添加请求头
     * 
     * @param name 头名称
     * @param value 头值
     * @return 当前上下文实例，支持链式调用
     */
    public RequestContext addHeader(String name, String value) {
        if (name != null && !name.isEmpty() && value != null) {
            this.headers.put(name, value);
        }
        return this;
    }
    
    /**
     * 批量添加请求头
     * 
     * @param headers 头映射
     * @return 当前上下文实例，支持链式调用
     */
    public RequestContext addHeaders(Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            this.headers.putAll(headers);
        }
        return this;
    }
    
    /**
     * 移除请求头
     * 
     * @param name 头名称
     * @return 当前上下文实例，支持链式调用
     */
    public RequestContext removeHeader(String name) {
        if (name != null && !name.isEmpty()) {
            this.headers.remove(name);
        }
        return this;
    }

    /**
     * 获取请求参数的不可变视图
     *
     * @return 请求参数字典的不可变视图
     */
    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * 添加请求参数
     *
     * @param key 参数键，不能为空
     * @param value 参数值
     * @return 当前请求上下文实例，支持链式调用
     * @throws IllegalArgumentException 如果key为null
     */
    public RequestContext addParam(String key, Object value) {
        Objects.requireNonNull(key, "Parameter key cannot be null");
        if (value != null) {
            params.put(key, value);
        } else {
            params.remove(key);
        }
        return this;
    }

    /**
     * 移除请求参数
     *
     * @param key 参数键，不能为空
     * @return 被移除的参数值，如果不存在则返回null
     * @throws IllegalArgumentException 如果key为null
     */
    public Object removeParam(String key) {
        Objects.requireNonNull(key, "Parameter key cannot be null");
        return params.remove(key);
    }

    /**
     * 清空请求参数
     *
     * @return 当前上下文实例，支持链式调用
     */
    public RequestContext clearParams() {
        this.params.clear();
        return this;
    }
    
    /**
     * 检查是否有异常
     * 
     * @return 如果有异常则返回true，否则返回false
     */
    public boolean hasException() {
        return exception != null;
    }
    
    /**
     * 检查请求是否成功
     * 
     * @return 如果请求成功（状态码在200-299之间）则返回true，否则返回false
     */
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300 && !hasException() && !canceled;
    }
    
    /**
     * 拦截器链接口，用于继续请求流程
     */
    public interface Chain {
        
        /**
         * 获取当前请求上下文
         * 
         * @return 请求上下文实例
         */
        RequestContext getContext();
        
        /**
         * 继续请求流程，调用下一个拦截器或执行实际请求
         * 
         * @return 请求上下文实例，包含处理后的结果
         * @throws Exception 当处理失败时抛出
         */
        RequestContext proceed() throws Exception;
    }
}