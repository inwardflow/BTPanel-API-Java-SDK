package net.heimeng.sdk.btapi.client;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;

import java.time.Duration;
import java.util.concurrent.*;

/**
 * API管理器，提供更高级的API访问功能
 * <p>
 * 该类封装了BtClient的功能，提供了更简洁的API调用方式，并支持同步和异步API调用。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class BtApiManager {

    /**
     * BtClient实例
     */
    private final BtClient client;

    /**
     * 默认超时时间（毫秒）
     */
    private static final long DEFAULT_TIMEOUT = 60000;

    /**
     * 构造函数，创建一个新的BtApiManager实例
     *
     * @param client BtClient实例
     */
    public BtApiManager(BtClient client) {
        if (client == null) {
            throw new IllegalArgumentException("BtClient cannot be null");
        }
        this.client = client;
    }

    /**
     * 执行同步API请求
     *
     * @param <T> API返回数据类型
     * @param api API接口实例
     * @return API返回结果
     * @throws BtApiException 当API调用失败时抛出
     */
    public <T> T execute(BtApi<T> api) {
        return client.execute(api);
    }

    /**
     * 执行异步API请求
     *
     * @param <T> API返回数据类型
     * @param api API接口实例
     * @return 包含API返回结果的CompletableFuture
     */
    public <T> CompletableFuture<T> executeAsync(BtApi<T> api) {
        return client.executeAsync(api);
    }

    /**
     * 执行异步API请求，并等待指定的超时时间
     *
     * @param <T> API返回数据类型
     * @param api API接口实例
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return API返回结果
     * @throws BtApiException 当API调用失败或超时时抛出
     */
    public <T> T executeAsyncWithTimeout(BtApi<T> api, long timeout, TimeUnit unit) {
        try {
            return executeAsync(api).get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BtApiException("API execution interrupted", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof BtApiException) {
                throw (BtApiException) e.getCause();
            } else {
                throw new BtApiException("API execution failed", e.getCause());
            }
        } catch (TimeoutException e) {
            throw new BtApiException("API execution timed out after " + timeout + " " + unit.name().toLowerCase(), e);
        }
    }

    /**
     * 执行异步API请求，并等待默认的超时时间（60秒）
     *
     * @param <T> API返回数据类型
     * @param api API接口实例
     * @return API返回结果
     * @throws BtApiException 当API调用失败或超时时抛出
     */
    public <T> T executeAsyncWithTimeout(BtApi<T> api) {
        return executeAsyncWithTimeout(api, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行异步API请求，并等待指定的超时时间（Duration）
     *
     * @param <T> API返回数据类型
     * @param api API接口实例
     * @param timeout 超时时间
     * @return API返回结果
     * @throws BtApiException 当API调用失败或超时时抛出
     */
    public <T> T executeAsyncWithTimeout(BtApi<T> api, Duration timeout) {
        return executeAsyncWithTimeout(api, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 执行异步API请求，并返回带超时控制的 CompletableFuture（非阻塞）
     *
     * @param <T> API返回数据类型
     * @param api API接口实例
     * @param timeout 超时时间
     * @return 包含API返回结果的CompletableFuture
     */
    public <T> CompletableFuture<T> executeAsyncWithTimeoutFuture(BtApi<T> api, Duration timeout) {
        CompletableFuture<T> future = new CompletableFuture<>();

        executeAsync(api).whenComplete((result, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
            } else {
                future.complete(result);
            }
        });

        return future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 获取底层的BtClient实例
     *
     * @return BtClient实例
     */
    public BtClient getClient() {
        return client;
    }

    /**
     * 关闭API管理器，释放资源
     */
    public void close() {
        client.close();
    }
}