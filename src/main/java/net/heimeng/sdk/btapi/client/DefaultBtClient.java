package net.heimeng.sdk.btapi.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.heimeng.sdk.btapi.BtUtils;
import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.exception.BtAuthenticationException;
import net.heimeng.sdk.btapi.exception.BtNetworkException;
import net.heimeng.sdk.btapi.interceptor.RequestContext;
import net.heimeng.sdk.btapi.interceptor.RequestInterceptor;
import net.heimeng.sdk.btapi.model.BtResult;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link BtClient} 的默认实现。
 *
 * <p>负责发送 HTTP 请求、应用拦截器链、处理重试与超时策略，并统一完成响应解析和异常转换。
 */
@Slf4j
public class DefaultBtClient implements BtClient, AutoCloseable {

  private static final String USER_AGENT = "btpanel-api-java-sdk";
  private static final String API_KEY_FAILURE_MESSAGE = "密钥校验失败";
  private static final Set<String> SENSITIVE_KEY_FRAGMENTS =
      Set.of("api_key", "token", "password", "secret", "access_key");

  private final BtSdkConfig config;
  private final RetryPolicy retryPolicy;
  private final HttpClient httpClient;
  private final List<RequestInterceptor> interceptors = new CopyOnWriteArrayList<>();
  private final ExecutorService executorService;
  private volatile boolean closed = false;

  public DefaultBtClient(BtSdkConfig config) {
    this.config = Objects.requireNonNull(config, "config cannot be null");
    this.retryPolicy = new RetryPolicy(config);

    // 单独的执行线程池用于异步请求与 HttpClient 回调，避免占用调用方线程。
    this.executorService = createExecutorService();
    this.httpClient = buildHttpClient();
  }

  /** 创建客户端使用的执行器服务。 */
  private ExecutorService createExecutorService() {
    ThreadFactory threadFactory =
        runnable -> {
          Thread thread = new Thread(runnable, "bt-client-worker-" + runnable.hashCode());
          thread.setDaemon(true);
          thread.setUncaughtExceptionHandler(
              (failedThread, exception) ->
                  log.error("Uncaught exception in thread {}", failedThread.getName(), exception));
          return thread;
        };
    return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
  }

  /** 根据配置构建底层 HTTP 客户端。 */
  private HttpClient buildHttpClient() {
    HttpClient.Builder clientBuilder =
        HttpClient.newBuilder()
            .connectTimeout(
                Duration.ofMillis(
                    config.getConnectTimeoutUnit().toMillis(config.getConnectTimeout())))
            .executor(executorService);

    SslContextConfigurer.configure(clientBuilder, config);

    return clientBuilder.build();
  }

  @Override
  public <T> T execute(BtApi<T> api) {
    checkNotClosed();
    Objects.requireNonNull(api, "API must not be null");

    RequestContext context = new RequestContext(api);
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
    } catch (Exception exception) {
      throw wrapException(exception);
    }
  }

  @Override
  public <T> CompletableFuture<T> executeAsync(BtApi<T> api) {
    checkNotClosed();
    Objects.requireNonNull(api, "API must not be null");

    return CompletableFuture.supplyAsync(() -> execute(api), executorService)
        .exceptionally(
            throwable -> {
              Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
              throw new CompletionException(wrapException(cause));
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
    if (closed) {
      return;
    }

    closed = true;

    try {
      executorService.shutdown();
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        log.warn("Executor service shutdown timeout, forcing shutdown");
        executorService.shutdownNow();
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
          log.error("Executor service did not terminate");
        }
      }
    } catch (InterruptedException exception) {
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

  private BtApiException wrapException(Throwable exception) {
    if (exception instanceof BtApiException) {
      return (BtApiException) exception;
    }
    if (exception instanceof BtNetworkException) {
      return new BtApiException("Network error", exception);
    }
    return new BtApiException("Unexpected error during API execution", exception);
  }

  /** 自动补齐宝塔接口鉴权所需的时间戳与签名参数。 */
  private void addAuthParameters(RequestContext context) {
    Map<String, Object> params = context.getParams();

    if (!params.containsKey("request_token") && !params.containsKey("request_time")) {
      long requestTime = BtUtils.generateRequestTime();
      String requestToken = BtUtils.generateRequestToken(config.getApiKey(), requestTime);

      context
          .addParam("request_token", requestToken)
          .addParam("request_time", String.valueOf(requestTime));
    }
  }

  /** 执行实际的 HTTP 请求并写回上下文。 */
  private <T> void executeHttpRequest(RequestContext context) throws Exception {
    @SuppressWarnings("unchecked")
    BtApi<T> api = (BtApi<T>) context.getApi();

    String baseEndpoint = api.getEndpoint();
    String fullUrl = RequestEncodingUtils.buildUrl(config.getBaseUrl(), baseEndpoint);
    fullUrl =
        RequestEncodingUtils.appendQueryParameters(fullUrl, api.getMethod(), context.getParams());

    log.debug("Built request URL: {}", maskUrl(fullUrl));
    log.debug("Base URL: {}", config.getBaseUrl());
    log.debug("Endpoint: {}", baseEndpoint);
    log.debug("HTTP method: {}", api.getMethod());

    URI uri = URI.create(fullUrl);
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri);

    switch (api.getMethod()) {
      case GET:
        requestBuilder.GET();
        break;
      case POST:
        requestBuilder.POST(RequestEncodingUtils.buildFormBodyPublisher(context.getParams()));
        break;
      case PUT:
        requestBuilder.PUT(RequestEncodingUtils.buildFormBodyPublisher(context.getParams()));
        break;
      case DELETE:
        requestBuilder.DELETE();
        break;
      case PATCH:
        requestBuilder.method(
            "PATCH", RequestEncodingUtils.buildFormBodyPublisher(context.getParams()));
        break;
      default:
        throw new IllegalArgumentException("Unsupported HTTP method: " + api.getMethod());
    }

    requestBuilder.timeout(
        Duration.ofMillis(config.getReadTimeoutUnit().toMillis(config.getReadTimeout())));
    requestBuilder.header("User-Agent", USER_AGENT);
    if (RequestEncodingUtils.shouldSendFormContentType(api.getMethod(), context.getParams())
        && !hasHeaderIgnoreCase(config.getExtraHeaders(), "Content-Type")
        && !hasHeaderIgnoreCase(context.getHeaders(), "Content-Type")) {
      requestBuilder.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    }

    config.getExtraHeaders().forEach(requestBuilder::header);
    context.getHeaders().forEach(requestBuilder::header);

    if (config.isEnableRequestLog()) {
      logRequest(api, context);
    }

    HttpResponse<String> response = executeWithRetry(requestBuilder, api.getMethod(), context);
    processResponse(response, context, api);
  }

  private boolean hasHeaderIgnoreCase(Map<String, String> headers, String headerName) {
    for (String existingHeaderName : headers.keySet()) {
      if (existingHeaderName.equalsIgnoreCase(headerName)) {
        return true;
      }
    }
    return false;
  }

  /** 按配置执行带重试策略的请求。 */
  private HttpResponse<String> executeWithRetry(
      HttpRequest.Builder requestBuilder, BtApi.HttpMethod method, RequestContext context)
      throws Exception {
    int retryCount = config.getRetryCount();

    for (int attempt = 0; attempt <= retryCount; attempt++) {
      try {
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (attempt < retryCount
            && retryPolicy.shouldRetryStatus(
                method, response.statusCode(), context.isForceRetry())) {
          log.warn(
              "Retryable response [{}], attempt {}/{}",
              response.statusCode(),
              attempt + 1,
              retryCount + 1);
          sleepBeforeRetry();
          continue;
        }

        return response;
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        throw new BtNetworkException("Request interrupted", exception);
      } catch (java.net.http.HttpTimeoutException | java.net.SocketTimeoutException exception) {
        log.warn("Timeout on attempt {}/{}", attempt + 1, retryCount + 1);
        if (attempt < retryCount
            && retryPolicy.shouldRetryException(method, exception, context.isForceRetry())) {
          sleepBeforeRetry();
          continue;
        }
        throw new BtNetworkException("Request timeout after retries", exception);
      } catch (Exception exception) {
        log.warn(
            "Request failed on attempt {}/{}: {}",
            attempt + 1,
            retryCount + 1,
            exception.toString(),
            exception);

        if (attempt < retryCount
            && retryPolicy.shouldRetryException(method, exception, context.isForceRetry())) {
          sleepBeforeRetry();
          continue;
        }
        context.setException(exception);
        throw exception;
      }
    }

    throw new BtNetworkException("Max retries exceeded: " + retryCount, context.getException());
  }

  private void sleepBeforeRetry() {
    try {
      Thread.sleep(config.getRetryInterval().toMillis());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new BtNetworkException("Retry backoff interrupted", exception);
    }
  }

  /** 处理 HTTP 响应，并将解析后的结果写回上下文。 */
  private <T> void processResponse(
      HttpResponse<String> response, RequestContext context, BtApi<T> api) throws Exception {

    context.setStatusCode(response.statusCode());
    context.setResponseBody(response.body());

    if (config.isEnableResponseLog()) {
      logResponse(response);
    }

    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      T result = api.parseResponse(response.body());

      if (result instanceof BtResult<?>) {
        BtResult<?> btResult = (BtResult<?>) result;
        if (btResult.isFailed()) {
          String message = btResult.getMsg();
          if (API_KEY_FAILURE_MESSAGE.equals(message)) {
            throw new BtAuthenticationException("API key verification failed", "API_KEY", null);
          }
          throw new BtApiException(
              "API business logic failed: " + message, response.statusCode(), response.body());
        }
      }

      context.setResult(result);
      return;
    }

    throw new BtApiException(
        "API request failed with status: " + response.statusCode(),
        response.statusCode(),
        response.body());
  }

  /** 输出请求日志，并对敏感信息做脱敏处理。 */
  private void logRequest(BtApi<?> api, RequestContext context) {
    log.debug(
        "-> {} {}",
        api.getMethod(),
        maskUrl(RequestEncodingUtils.buildUrl(config.getBaseUrl(), api.getEndpoint())));
    if (!context.getParams().isEmpty()) {
      log.debug("Params: {}", maskParams(context.getParams()));
    }
  }

  /** 输出响应日志，避免打印过长的响应体。 */
  private void logResponse(HttpResponse<String> response) {
    String bodyPreview =
        response.body() != null && response.body().length() > 1000
            ? response.body().substring(0, 1000) + " [truncated...]"
            : response.body();
    log.debug("<- {} {}", response.statusCode(), bodyPreview);
  }

  /** 对 URL 查询参数中的敏感信息做脱敏。 */
  static String maskUrl(String url) {
    int queryIndex = url.indexOf('?');
    if (queryIndex == -1) {
      return url;
    }

    String base = url.substring(0, queryIndex);
    String query = url.substring(queryIndex + 1);

    String maskedQuery =
        Arrays.stream(query.split("&"))
            .map(DefaultBtClient::maskQueryParam)
            .collect(Collectors.joining("&"));

    return base + "?" + maskedQuery;
  }

  private static String maskQueryParam(String param) {
    String[] parts = param.split("=", 2);
    if (parts.length == 2 && isSensitiveKey(parts[0])) {
      return parts[0] + "=***";
    }
    return param;
  }

  /** 对参数集合中的敏感字段做脱敏。 */
  static String maskParams(Map<String, Object> params) {
    return params.entrySet().stream()
        .map(
            entry -> {
              String key = entry.getKey();
              String value = isSensitiveKey(key) ? "***" : String.valueOf(entry.getValue());
              return key + "=" + value;
            })
        .collect(Collectors.joining(", ", "{", "}"));
  }

  /** 参数名包含任一敏感片段（如 {@code ftp_password}、{@code request_token}）即视为敏感。 */
  static boolean isSensitiveKey(String key) {
    String normalizedKey = key.toLowerCase(Locale.ROOT);
    return SENSITIVE_KEY_FRAGMENTS.stream().anyMatch(normalizedKey::contains);
  }

  /** 拦截器责任链，用于依次执行请求拦截器。 */
  private static class InterceptorChain implements RequestContext.Chain {
    private final List<RequestInterceptor> interceptors;
    private final RequestExecutor executor;
    private int index = 0;
    private RequestContext context;

    InterceptorChain(List<RequestInterceptor> interceptors, RequestExecutor executor) {
      this.interceptors = new ArrayList<>(interceptors);
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
