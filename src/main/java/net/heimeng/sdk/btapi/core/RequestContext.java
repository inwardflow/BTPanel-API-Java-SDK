package net.heimeng.sdk.btapi.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 请求上下文，在拦截器链中传递，包含请求和响应的相关信息
 * <p>
 * 拦截器可以通过此上下文访问和修改请求参数，以及获取响应信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class RequestContext {
    private final BtApi<?> api;
    private final Map<String, Object> params;
    private final Map<String, String> headers;
    private volatile String response;
    private volatile Object result;
    private volatile Exception exception;
    private final Chain chain;

    /**
     * 构造请求上下文
     *
     * @param api API接口实例，不能为空
     * @param chain 拦截器链，不能为空
     * @throws IllegalArgumentException 如果api或chain为null
     */
    public RequestContext(BtApi<?> api, Chain chain) {
        this.api = Objects.requireNonNull(api, "API cannot be null");
        this.chain = Objects.requireNonNull(chain, "Chain cannot be null");

        // 安全地复制API参数
        Map<String, Object> apiParams = api.getParams();
        this.params = apiParams != null ?
                new HashMap<>(apiParams) : new HashMap<>();

        this.headers = new HashMap<>();
    }

    /**
     * 获取API接口实例
     *
     * @return API接口实例，不会为null
     */
    public BtApi<?> getApi() {
        return api;
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
     * 获取请求头的不可变视图
     *
     * @return 请求头字典的不可变视图
     */
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * 添加请求头
     *
     * @param key 请求头键，不能为空
     * @param value 请求头值，不能为空
     * @return 当前请求上下文实例，支持链式调用
     * @throws IllegalArgumentException 如果key或value为null
     */
    public RequestContext addHeader(String key, String value) {
        Objects.requireNonNull(key, "Header key cannot be null");
        Objects.requireNonNull(value, "Header value cannot be null");
        headers.put(key, value);
        return this;
    }

    /**
     * 移除请求头
     *
     * @param key 请求头键，不能为空
     * @return 被移除的请求头值，如果不存在则返回null
     * @throws IllegalArgumentException 如果key为null
     */
    public String removeHeader(String key) {
        Objects.requireNonNull(key, "Header key cannot be null");
        return headers.remove(key);
    }

    /**
     * 获取API响应字符串
     *
     * @return API响应字符串，可能为null
     */
    public String getResponse() {
        return response;
    }

    /**
     * 设置API响应字符串
     *
     * @param response API响应字符串
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * 获取解析后的API结果
     *
     * @return 解析后的API结果，可能为null
     */
    public Object getResult() {
        return result;
    }

    /**
     * 设置解析后的API结果
     *
     * @param result 解析后的API结果
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 获取异常信息
     *
     * @return 异常信息，可能为null
     */
    public Exception getException() {
        return exception;
    }

    /**
     * 设置异常信息
     *
     * @param exception 异常信息
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * 继续请求处理流程
     * <p>
     * 在拦截器实现中，必须调用此方法以继续拦截器链的执行。
     * </p>
     */
    public void proceed() {
        chain.proceed(this);
    }

    /**
     * 检查上下文是否包含异常
     *
     * @return 如果包含异常则返回true，否则返回false
     */
    public boolean hasException() {
        return exception != null;
    }

    /**
     * 检查请求是否已成功处理并获得结果
     *
     * @return 如果已成功处理则返回true，否则返回false
     */
    public boolean isSuccessful() {
        return result != null && exception == null;
    }

    /**
     * 清除当前结果和异常状态
     *
     * @return 当前请求上下文实例，支持链式调用
     */
    public RequestContext clearResult() {
        this.result = null;
        this.exception = null;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestContext that = (RequestContext) o;
        return Objects.equals(api, that.api) &&
                Objects.equals(params, that.params) &&
                Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(api, params, headers);
    }

    @Override
    public String toString() {
        return "RequestContext{" +
                "api=" + api.getEndpoint() +
                ", params=" + params.size() +
                ", headers=" + headers.size() +
                ", hasResponse=" + (response != null) +
                ", hasResult=" + (result != null) +
                ", hasException=" + (exception != null) +
                '}';
    }

    /**
     * 拦截器链接口
     */
    @FunctionalInterface
    public interface Chain {
        /**
         * 继续执行拦截器链
         *
         * @param context 请求上下文
         */
        void proceed(RequestContext context);
    }
}