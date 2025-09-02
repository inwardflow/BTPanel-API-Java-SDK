package net.heimeng.sdk.btapi.core;

import java.util.concurrent.CompletableFuture;

/**
 * 宝塔面板客户端接口，是SDK的核心接口，定义了与宝塔面板API交互的基本操作
 * <p>
 * 该接口支持同步和异步API调用，并提供拦截器机制，允许用户自定义请求和响应处理过程。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public interface BtClient {

    /**
     * 执行同步API请求
     *
     * @param <T> API返回数据类型
     * @param api API接口实例
     * @return API返回结果
     * @throws net.heimeng.sdk.btapi.exception.BtApiException 当API调用失败时抛出
     */
    <T> T execute(BtApi<T> api);

    /**
     * 执行异步API请求
     *
     * @param <T> API返回数据类型
     * @param api API接口实例
     * @return 包含API返回结果的CompletableFuture
     */
    <T> CompletableFuture<T> executeAsync(BtApi<T> api);

    /**
     * 添加请求拦截器，用于在发送请求前或接收响应后执行自定义逻辑
     *
     * @param interceptor 拦截器实例
     * @return 当前客户端实例，支持链式调用
     */
    BtClient addInterceptor(Interceptor interceptor);

    /**
     * 获取客户端配置信息
     *
     * @return 客户端配置实例
     */
    BtConfig getConfig();

    /**
     * 关闭客户端，释放资源
     */
    void close();

    /**
     * 检查客户端是否已关闭
     *
     * @return 如果客户端已关闭则返回true，否则返回false
     */
    boolean isClosed();
}