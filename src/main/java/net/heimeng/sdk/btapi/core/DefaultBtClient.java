package net.heimeng.sdk.btapi.core;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import net.heimeng.sdk.btapi.BtUtils;
import net.heimeng.sdk.btapi.exception.BtApiException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * BtClient接口的默认实现，提供客户端的核心功能
 * <p>
 * 此类负责发送API请求、处理响应、应用拦截器，并支持同步和异步API调用。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Slf4j
public class DefaultBtClient implements BtClient, AutoCloseable {

    /**
     * 请求执行器，用于执行实际的API请求
     */
    private interface RequestExecutor {
        /**
         * 执行请求
         *
         * @param context 请求上下文
         */
        void execute(RequestContext context);
    }
    private final BtConfig config;
    private final List<Interceptor> interceptors = new ArrayList<>();
    private final ExecutorService executorService;
    private volatile boolean closed = false;

    /**
     * 构造DefaultBtClient实例
     *
     * @param config 客户端配置
     */
    public DefaultBtClient(BtConfig config) {
        if (config == null || !config.isValid()) {
            throw new IllegalArgumentException("Invalid configuration");
        }
        this.config = config;
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread t = new Thread(r, "bt-client-thread");
                    t.setDaemon(false);
                    return t;
                }
        );
    }

    /**
     * 执行同步API请求
     *
     * @param api API接口实例
     * @param <T> API返回数据类型
     * @return API返回结果
     * @throws BtApiException 当API调用失败时抛出
     */
    @Override
    public <T> T execute(BtApi<T> api) {
        checkClosed();
        if (api == null) {
            throw new IllegalArgumentException("API cannot be null");
        }

        RequestContext context = new RequestContext(api, new InterceptorChain(interceptors, this::executeInternal));
        applyInterceptors(context);

        if (context.getException() != null) {
            if (context.getException() instanceof BtApiException) {
                throw (BtApiException) context.getException();
            } else {
                throw new BtApiException("API execution failed", context.getException());
            }
        }

        @SuppressWarnings("unchecked")
        T result = (T) context.getResult();
        return result;
    }

    /**
     * 执行异步API请求
     *
     * @param api API接口实例
     * @param <T> API返回数据类型
     * @return 包含API返回结果的CompletableFuture
     */
    @Override
    public <T> CompletableFuture<T> executeAsync(BtApi<T> api) {
        checkClosed();
        return CompletableFuture.supplyAsync(() -> execute(api), executorService);
    }

    /**
     * 添加请求拦截器
     *
     * @param interceptor 拦截器实例
     * @return 当前客户端实例，支持链式调用
     */
    @Override
    public BtClient addInterceptor(Interceptor interceptor) {
        checkClosed();
        if (interceptor != null) {
            interceptors.add(interceptor);
        }
        return this;
    }

    /**
     * 获取客户端配置信息
     *
     * @return 客户端配置实例
     */
    @Override
    public BtConfig getConfig() {
        return config;
    }

    /**
     * 关闭客户端，释放资源
     */
    @Override
    public void close() {
        if (!closed) {
            synchronized (this) {
                if (!closed) {
                    executorService.shutdown();
                    try {
                        // 等待最多60秒让现有任务完成
                        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                            executorService.shutdownNow();
                            // 再等待最多60秒让任务响应中断
                            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                                log.warn("ExecutorService did not terminate");
                            }
                        }
                    } catch (InterruptedException e) {
                        executorService.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                    closed = true;
                }
            }
        }
    }

    /**
     * 检查客户端是否已关闭
     *
     * @return 如果客户端已关闭则返回true，否则返回false
     */
    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * 应用所有拦截器
     *
     * @param context 请求上下文
     */
    private void applyInterceptors(RequestContext context) {
        if (interceptors.isEmpty()) {
            context.proceed();
            return;
        }
        context.proceed();
    }

    /**
     * 内部执行API请求的方法
     *
     * @param context 请求上下文
     */
    private void executeInternal(RequestContext context) {
        BtApi<?> api = context.getApi();
        try {
            String url = buildUrl(api.getEndpoint());
            log.debug("Sending {} request to {}", api.getMethod(), url);

            HttpRequest request = createRequest(api, url, context);
            int retries = 0;
            boolean shouldRetry;

            do {
                shouldRetry = false;
                try (HttpResponse response = request.execute()) {
                    int statusCode = response.getStatus();
                    log.debug("Received response with status code: {}", statusCode);

                    String responseBody = response.body();
                    context.setResponse(responseBody);

                    if (statusCode >= 200 && statusCode < 300) {
                        @SuppressWarnings("unchecked")
                        BtApi<Object> typedApi = (BtApi<Object>) api;
                        Object result = typedApi.parseResponse(responseBody);
                        context.setResult(result);
                    } else {
                        // 检查是否需要重试
                        shouldRetry = isRetryable(statusCode) && retries < config.getRetryCount();
                        if (shouldRetry) {
                            retries++;
                            log.debug("Retrying request, attempt {}/{}", retries, config.getRetryCount());
                            Thread.sleep(config.getRetryInterval().toMillis());
                        } else {
                            throw new BtApiException("API request failed with status code: " + statusCode,
                                    statusCode, responseBody);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BtApiException("API request interrupted", e);
                } catch (Exception e) {
                    // 网络异常也可以重试
                    shouldRetry = retries < config.getRetryCount() && !(e instanceof BtApiException);
                    if (shouldRetry) {
                        retries++;
                        log.debug("Retrying request due to exception: {}, attempt {}/{}",
                                e.getMessage(), retries, config.getRetryCount());
                        try {
                            Thread.sleep(config.getRetryInterval().toMillis());
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new BtApiException("API request interrupted during retry", ie);
                        }
                    } else {
                        throw new BtApiException("API request failed: " + e.getMessage(), e);
                    }
                }
            } while (shouldRetry);
        } catch (Exception e) {
            context.setException(e);
        }
    }

    /**
     * 构建完整的URL
     *
     * @param endpoint API端点
     * @return 完整的URL
     */
    private String buildUrl(String endpoint) {
        String baseUrl = config.getBaseUrl();
        if (endpoint == null || endpoint.isEmpty()) {
            return baseUrl;
        }

        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        return baseUrl + endpoint;
    }

    /**
     * 创建HTTP请求
     *
     * @param api     API接口实例
     * @param url     完整的URL
     * @param context 请求上下文
     * @return HTTP请求实例
     */
    private HttpRequest createRequest(BtApi<?> api, String url, RequestContext context) {
        HttpRequest request = switch (api.getMethod()) {
            case GET -> HttpRequest.get(url);
            case POST -> HttpRequest.post(url);
            case PUT -> HttpRequest.put(url);
            case DELETE -> HttpRequest.delete(url);
            case PATCH -> HttpRequest.patch(url);
        };

        // 设置请求参数
        Map<String, Object> params = context.getParams();
        if (params != null) {
            params.forEach((key, value) -> {
                if (value != null) {
                    request.form(key, value.toString());
                }
            });
        }

        // 添加API认证信息
        long requestTime = System.currentTimeMillis() / 1000;
        String requestToken = BtUtils.generateRequestToken(config.getApiKey(), requestTime);
        request.form("request_token", requestToken)
                .form("request_time", requestTime);

        // 设置请求头
        Map<String, String> headers = context.getHeaders();
        if (headers != null) {
            headers.forEach(request::header);
        }

        Map<String, String> extraHeaders = config.getExtraHeaders();
        if (extraHeaders != null) {
            extraHeaders.forEach(request::header);
        }

        // 设置超时
        Duration connectTimeout = config.getConnectTimeoutDuration();
        Duration readTimeout = config.getReadTimeoutDuration();
        request.setConnectionTimeout((int) connectTimeout.toMillis())
                .setReadTimeout((int) readTimeout.toMillis());

        return request;
    }

    /**
     * 检查HTTP状态码是否可重试
     *
     * @param statusCode HTTP状态码
     * @return 如果可重试则返回true，否则返回false
     */
    private boolean isRetryable(int statusCode) {
        int[] retryableCodes = config.getRetryableStatusCodes();
        if (retryableCodes != null) {
            for (int code : retryableCodes) {
                if (code == statusCode) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查客户端是否已关闭
     *
     * @throws IllegalStateException 如果客户端已关闭则抛出
     */
    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("Client is already closed");
        }
    }

    /**
     * 拦截器链，用于按顺序执行所有拦截器
     */
    private static class InterceptorChain implements RequestContext.Chain {
        private final List<Interceptor> interceptors;
        private final java.util.function.Consumer<RequestContext> actualExecutor;
        private int index = 0;

        public InterceptorChain(List<Interceptor> interceptors, java.util.function.Consumer<RequestContext> actualExecutor) {
            this.interceptors = new ArrayList<>(interceptors); // 创建副本保证线程安全
            this.actualExecutor = actualExecutor;
        }

        @Override
        public void proceed(RequestContext context) {
            if (index < interceptors.size()) {
                Interceptor interceptor = interceptors.get(index++);
                interceptor.intercept(context);
            } else {
                // 执行实际请求
                actualExecutor.accept(context);
            }
        }
    }
}