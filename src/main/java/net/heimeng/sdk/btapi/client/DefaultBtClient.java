package net.heimeng.sdk.btapi.client;

import net.heimeng.sdk.btapi.BtUtils;
import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.exception.BtNetworkException;
import net.heimeng.sdk.btapi.interceptor.RequestContext;
import net.heimeng.sdk.btapi.interceptor.RequestInterceptor;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.extern.slf4j.Slf4j;

/**
 * BtClient 接口的默认实现，提供客户端的核心功能
 * <p>
 * 此类负责发送API请求、处理响应、应用拦截器，并支持同步和异步API调用。
 * 使用 Java 17 HttpClient，支持结构化并发、标准化异常、资源自动回收。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Slf4j
public class DefaultBtClient implements BtClient, AutoCloseable {

    private final BtSdkConfig config;
    private final HttpClient httpClient;
    private final List<RequestInterceptor> interceptors = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executorService;
    private final Set<String> sensitiveKeys = Set.of("api_key", "token", "password", "secret", "access_key");
    private volatile boolean closed = false;

    public DefaultBtClient(BtSdkConfig config) {
        Objects.requireNonNull(config, "Config must not be null");
        if (!config.isValid()) {
            throw new IllegalArgumentException("Invalid configuration");
        }
        this.config = config;

        // 创建线程池
        this.executorService = createExecutorService();

        // 构建 HttpClient
        this.httpClient = buildHttpClient();
    }

    /**
     * 创建执行器服务
     */
    private ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread t = new Thread(r, "bt-client-worker-" + r.hashCode());
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler((thread, ex) ->
                            log.error("Uncaught exception in thread {}", thread.getName(), ex));
                    return t;
                });
    }

    /**
     * 构建 HttpClient
     */
    private HttpClient buildHttpClient() {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(
                        config.getConnectTimeoutUnit().toMillis(config.getConnectTimeout())))
                .executor(executorService);

        // 配置 SSL 验证
        if (!config.isVerifySsl()) {
            configureInsecureSSL(clientBuilder);
        }

        return clientBuilder.build();
    }

    /**
     * 配置不安全的 SSL（仅用于测试环境）
     */
    private void configureInsecureSSL(HttpClient.Builder clientBuilder) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            clientBuilder.sslContext(sslContext);
        } catch (Exception e) {
            log.warn("Failed to configure SSL verification bypass", e);
        }
    }

    @Override
    public <T> T execute(BtApi<T> api) {
        checkNotClosed();
        Objects.requireNonNull(api, "API must not be null");

        RequestContext context = new RequestContext(api);

        // 添加认证参数
        addAuthParameters(context);

        try {
            InterceptorChain chain = new InterceptorChain(interceptors, this::executeHttpRequest);
            chain.proceed(context);

            if (context.hasException()) {
                throw wrapException(context.getException());
            }

            @SuppressWarnings("unchecked")
            T result = (T) context.getResult();
            return result;

        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(BtApi<T> api) {
        checkNotClosed();
        Objects.requireNonNull(api, "API must not be null");

        return CompletableFuture
                .supplyAsync(() -> execute(api), executorService)
                .exceptionally(throwable -> {
                    throw new CompletionException(wrapException(
                            throwable.getCause() != null ? throwable.getCause() : throwable));
                });
    }

    @Override
    public BtClient addInterceptor(RequestInterceptor interceptor) {
        checkNotClosed();
        Objects.requireNonNull(interceptor, "Interceptor must not be null");
        interceptors.add(interceptor);
        interceptors.sort(Comparator.comparingInt(RequestInterceptor::getPriority));
        return this;
    }

    @Override
    public BtSdkConfig getConfig() {
        return config;
    }

    @Override
    public void close() {
        if (closed) return;

        closed = true;

        try {
            // 优雅关闭执行器
            executorService.shutdown();
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Executor service shutdown timeout, forcing shutdown");
                executorService.shutdownNow();
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("Executor service did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("DefaultBtClient closed gracefully");
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("Client is closed");
        }
    }

    private BtApiException wrapException(Throwable e) {
        if (e instanceof BtApiException) {
            return (BtApiException) e;
        } else if (e instanceof BtNetworkException) {
            return new BtApiException("Network error", e);
        } else {
            return new BtApiException("Unexpected error during API execution", e);
        }
    }

    /**
     * 添加认证参数
     */
    private void addAuthParameters(RequestContext context) {
        Map<String, Object> params = context.getParams();

        // 只有当参数中没有认证信息时才添加
        if (!params.containsKey("request_token") && !params.containsKey("request_time")) {
            long requestTime = System.currentTimeMillis() / 1000;
            String requestToken = BtUtils.generateRequestToken(config.getApiKey(), requestTime);

            context.addParam("request_token", requestToken)
                    .addParam("request_time", String.valueOf(requestTime));
        }
    }

    /**
     * 执行 HTTP 请求
     */
    private <T> void executeHttpRequest(RequestContext context) throws Exception {
        @SuppressWarnings("unchecked")
        BtApi<T> api = (BtApi<T>) context.getApi();

        // 构建基本URL
        String baseEndpoint = api.getEndpoint();
        String fullUrl = buildUrl(baseEndpoint);
        
        // 对于宝塔面板API，所有参数都应该作为URL查询参数，无论HTTP方法是什么
        if (!context.getParams().isEmpty()) {
            StringBuilder urlBuilder = new StringBuilder(fullUrl);
            boolean hasExistingParams = fullUrl.contains("?");
            
            // 将请求参数添加到URL查询字符串
            for (Map.Entry<String, Object> entry : context.getParams().entrySet()) {
                urlBuilder.append(hasExistingParams ? '&' : '?');
                urlBuilder.append(entry.getKey());
                urlBuilder.append('=');
                urlBuilder.append(encodeValue(String.valueOf(entry.getValue())));
                hasExistingParams = true;
            }
            
            fullUrl = urlBuilder.toString();
        }
        
        // 记录完整URL用于调试
        log.debug("构建的完整URL: {}", fullUrl);
        log.debug("Base URL: {}", config.getBaseUrl());
        log.debug("Endpoint: {}", baseEndpoint);
        log.debug("HTTP方法: {}", api.getMethod());

        URI uri = URI.create(fullUrl);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri);

        // 设置 HTTP 方法
        switch (api.getMethod()) {
            case GET:
                requestBuilder.GET();
                break;
            case POST:
                requestBuilder.POST(buildBodyPublisher(api, context));
                break;
            case PUT:
                requestBuilder.PUT(buildBodyPublisher(api, context));
                break;
            case DELETE:
                requestBuilder.DELETE();
                break;
            case PATCH:
                requestBuilder.method("PATCH", buildBodyPublisher(api, context));
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + api.getMethod());
        }

        // 设置超时
        requestBuilder.timeout(Duration.ofMillis(
                config.getReadTimeoutUnit().toMillis(config.getReadTimeout())));

        // 设置请求头
        config.getExtraHeaders().forEach(requestBuilder::header);
        context.getHeaders().forEach(requestBuilder::header);

        // 日志记录
        if (config.isEnableRequestLog()) {
            logRequest(api, context);
        }

        // 执行带重试的请求
        HttpResponse<String> response = executeWithRetry(requestBuilder, 0, context);

        // 处理响应
        processResponse(response, context, api);
    }

    /**
     * 构建请求体发布器
     */
    private HttpRequest.BodyPublisher buildBodyPublisher(BtApi<?> api, RequestContext context) {
        Map<String, Object> params = context.getParams();

        if (params.isEmpty()) {
            return HttpRequest.BodyPublishers.noBody();
        }

        String formData = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + encodeValue(String.valueOf(e.getValue())))
                .collect(Collectors.joining("&"));

        return HttpRequest.BodyPublishers.ofString(formData);
    }

    /**
     * URL 编码值
     */
    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value; // 如果编码失败，返回原始值
        }
    }

    /**
     * 构建完整 URL
     */
    private String buildUrl(String endpoint) {
        String baseUrl = config.getBaseUrl();
        if (endpoint == null || endpoint.isEmpty()) {
            return baseUrl;
        }

        // 规范化 endpoint（移除前导斜杠）
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }

        // 规范化 baseUrl（确保以斜杠结尾）
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        // 直接拼接 baseUrl 和 endpoint
        return baseUrl + endpoint;
    }

    /**
     * 带重试的请求执行
     */
    private HttpResponse<String> executeWithRetry(
            HttpRequest.Builder requestBuilder,
            int attempt,
            RequestContext context) throws Exception {

        if (attempt > config.getRetryCount()) {
            throw new BtNetworkException("Max retries exceeded: " + config.getRetryCount(),
                    context.getException());
        }

        try {
            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (isRetryableStatusCode(response.statusCode()) || context.isForceRetry()) {
                if (attempt < config.getRetryCount()) {
                    log.warn("Retryable response [{}], attempt {}/{}",
                            response.statusCode(), attempt + 1, config.getRetryCount());
                    Thread.sleep(config.getRetryInterval().toMillis());
                    return executeWithRetry(requestBuilder, attempt + 1, context);
                }
            }

            return response;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BtNetworkException("Request interrupted", e);
        } catch (java.net.http.HttpTimeoutException | java.net.SocketTimeoutException e) {
            log.warn("Timeout on attempt {}/{}", attempt + 1, config.getRetryCount());
            if (attempt < config.getRetryCount()) {
                Thread.sleep(config.getRetryInterval().toMillis());
                return executeWithRetry(requestBuilder, attempt + 1, context);
            }
            throw new BtNetworkException("Request timeout after retries", e);
        } catch (Exception e) {
            log.warn("Request failed on attempt {}/{}: {}",
                    attempt + 1, config.getRetryCount(), e.getMessage(), e);

            if (attempt < config.getRetryCount()) {
                Thread.sleep(config.getRetryInterval().toMillis());
                return executeWithRetry(requestBuilder, attempt + 1, context);
            }
            context.setException(e);
            throw e;
        }
    }

    private boolean isRetryableStatusCode(int code) {
        return Arrays.stream(config.getRetryableStatusCodes())
                .anyMatch(statusCode -> statusCode == code);
    }

    /**
     * 处理响应
     */
    private <T> void processResponse(
            HttpResponse<String> response,
            RequestContext context,
            BtApi<T> api) throws Exception {

        context.setStatusCode(response.statusCode());
        context.setResponseBody(response.body());

        if (config.isEnableResponseLog()) {
            logResponse(response);
        }

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            T result = api.parseResponse(response.body());
            context.setResult(result);
        } else {
            throw new BtApiException("API request failed with status: " + response.statusCode(),
                    response.statusCode(), response.body());
        }
    }

    /**
     * 记录请求日志
     */
    private void logRequest(BtApi<?> api, RequestContext context) {
        log.debug("→ {} {}", api.getMethod(), maskUrl(buildUrl(api.getEndpoint())));
        if (!context.getParams().isEmpty()) {
            log.debug("Params: {}", maskParams(context.getParams()));
        }
    }

    /**
     * 记录响应日志
     */
    private void logResponse(HttpResponse<String> response) {
        String bodyPreview = response.body() != null && response.body().length() > 1000
                ? response.body().substring(0, 1000) + " [truncated...]"
                : response.body();
        log.debug("← {} {}", response.statusCode(), bodyPreview);
    }

    /**
     * 遮蔽 URL 中的敏感信息
     */
    private String maskUrl(String url) {
        int queryIndex = url.indexOf('?');
        if (queryIndex == -1) return url;

        String base = url.substring(0, queryIndex);
        String query = url.substring(queryIndex + 1);

        String maskedQuery = Arrays.stream(query.split("&"))
                .map(this::maskQueryParam)
                .collect(Collectors.joining("&"));

        return base + "?" + maskedQuery;
    }

    private String maskQueryParam(String param) {
        String[] parts = param.split("=", 2);
        if (parts.length == 2 && sensitiveKeys.contains(parts[0].toLowerCase())) {
            return parts[0] + "=***";
        }
        return param;
    }

    /**
     * 遮蔽参数中的敏感信息
     */
    private String maskParams(Map<String, Object> params) {
        return params.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    String value = sensitiveKeys.stream().anyMatch(key::contains)
                            ? "***"
                            : String.valueOf(entry.getValue());
                    return key + "=" + value;
                })
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * 拦截器链
     */
    private static class InterceptorChain implements RequestContext.Chain {
        private final List<RequestInterceptor> interceptors;
        private final RequestExecutor executor;
        private int index = 0;
        private RequestContext context;

        InterceptorChain(List<RequestInterceptor> interceptors, RequestExecutor executor) {
            this.interceptors = new ArrayList<>(interceptors); // 防止并发修改
            this.executor = executor;
        }

        public void proceed(RequestContext context) throws Exception {
            this.context = context;
            proceed();
        }

        @Override
        public RequestContext getContext() {
            return context;
        }

        @Override
        public RequestContext proceed() throws Exception {
            if (context.isCanceled()) {
                throw new CancellationException("Request was canceled");
            }
            if (index < interceptors.size()) {
                RequestInterceptor interceptor = interceptors.get(index++);
                interceptor.intercept(context, this);
            } else {
                executor.execute(context);
            }
            return context;
        }
    }

    @FunctionalInterface
    private interface RequestExecutor {
        void execute(RequestContext context) throws Exception;
    }
}