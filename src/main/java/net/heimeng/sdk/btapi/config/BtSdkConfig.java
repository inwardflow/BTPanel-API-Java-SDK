package net.heimeng.sdk.btapi.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * SDK 的不可变配置对象。
 *
 * <p>该对象会在构建阶段完成地址规范化、超时参数校验和重试参数校验，尽量把明显的配置错误前置到启动时。
 */
public final class BtSdkConfig {

  private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_READ_TIMEOUT_SECONDS = 30;
  private static final int DEFAULT_RETRY_COUNT = 3;
  private static final int[] DEFAULT_RETRYABLE_STATUS_CODES = {408, 429, 500, 502, 503, 504};

  private final String baseUrl;
  private final String apiKey;
  private final int connectTimeout;
  private final TimeUnit connectTimeoutUnit;
  private final int readTimeout;
  private final TimeUnit readTimeoutUnit;
  private final RetryMode retryMode;
  private final int retryCount;
  private final Duration retryInterval;
  private final int[] retryableStatusCodes;
  private final Map<String, String> extraHeaders;
  private final boolean enableResponseLog;
  private final boolean enableRequestLog;
  private final SslMode sslMode;
  private final String trustStorePath;
  private final String trustStorePassword;
  private final String trustStoreType;

  private BtSdkConfig(Builder builder) {
    this.baseUrl = normalizeBaseUrl(builder.baseUrl);
    this.apiKey = requireNotBlank(builder.apiKey, "apiKey");
    this.connectTimeout = requirePositive(builder.connectTimeout, "connectTimeout");
    this.connectTimeoutUnit =
        Objects.requireNonNull(builder.connectTimeoutUnit, "connectTimeoutUnit cannot be null");
    this.readTimeout = requirePositive(builder.readTimeout, "readTimeout");
    this.readTimeoutUnit =
        Objects.requireNonNull(builder.readTimeoutUnit, "readTimeoutUnit cannot be null");
    this.retryMode = Objects.requireNonNull(builder.retryMode, "retryMode cannot be null");
    this.retryCount = requireNonNegative(builder.retryCount, "retryCount");
    this.retryInterval = requireNonNullAndPositive(builder.retryInterval, "retryInterval");
    this.retryableStatusCodes = builder.retryableStatusCodes.clone();
    this.extraHeaders = Map.copyOf(new LinkedHashMap<>(builder.extraHeaders));
    this.enableResponseLog = builder.enableResponseLog;
    this.enableRequestLog = builder.enableRequestLog;
    this.sslMode = Objects.requireNonNull(builder.sslMode, "sslMode cannot be null");
    this.trustStorePath = normalizeOptional(builder.trustStorePath);
    this.trustStorePassword = builder.trustStorePassword;
    this.trustStoreType = normalizeOptional(builder.trustStoreType);

    validateBaseUrl(this.baseUrl);
    validateSslSettings();
    validateRetrySettings();
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getApiKey() {
    return apiKey;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public TimeUnit getConnectTimeoutUnit() {
    return connectTimeoutUnit;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public TimeUnit getReadTimeoutUnit() {
    return readTimeoutUnit;
  }

  public RetryMode getRetryMode() {
    return retryMode;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public Duration getRetryInterval() {
    return retryInterval;
  }

  public int[] getRetryableStatusCodes() {
    return retryableStatusCodes.clone();
  }

  public Map<String, String> getExtraHeaders() {
    return extraHeaders;
  }

  public boolean isEnableResponseLog() {
    return enableResponseLog;
  }

  public boolean isEnableRequestLog() {
    return enableRequestLog;
  }

  public SslMode getSslMode() {
    return sslMode;
  }

  public String getTrustStorePath() {
    return trustStorePath;
  }

  public String getTrustStorePassword() {
    return trustStorePassword;
  }

  public String getTrustStoreType() {
    return trustStoreType;
  }

  /**
   * @deprecated use {@link #getRetryMode()} instead.
   */
  @Deprecated
  public boolean isEnableRetry() {
    return retryMode != RetryMode.NONE;
  }

  /**
   * @deprecated use {@link #getSslMode()} instead.
   */
  @Deprecated
  public boolean isVerifySsl() {
    return sslMode != SslMode.INSECURE_TRUST_ALL;
  }

  public boolean isValid() {
    return true;
  }

  @Override
  public String toString() {
    return "BtSdkConfig{"
        + "baseUrl='"
        + baseUrl
        + '\''
        + ", apiKey='***'"
        + ", connectTimeout="
        + connectTimeout
        + ", connectTimeoutUnit="
        + connectTimeoutUnit
        + ", readTimeout="
        + readTimeout
        + ", readTimeoutUnit="
        + readTimeoutUnit
        + ", retryMode="
        + retryMode
        + ", retryCount="
        + retryCount
        + ", retryInterval="
        + retryInterval
        + ", retryableStatusCodes="
        + Arrays.toString(retryableStatusCodes)
        + ", extraHeaders="
        + extraHeaders.keySet()
        + ", enableResponseLog="
        + enableResponseLog
        + ", enableRequestLog="
        + enableRequestLog
        + ", sslMode="
        + sslMode
        + ", trustStorePath='"
        + (trustStorePath == null ? "" : trustStorePath)
        + '\''
        + ", trustStoreType='"
        + (trustStoreType == null ? "" : trustStoreType)
        + '\''
        + '}';
  }

  private void validateSslSettings() {
    if (sslMode != SslMode.CUSTOM_TRUST_STORE) {
      return;
    }

    if (trustStorePath == null) {
      throw new IllegalArgumentException(
          "trustStorePath is required when sslMode is CUSTOM_TRUST_STORE");
    }

    if (trustStoreType == null) {
      throw new IllegalArgumentException(
          "trustStoreType is required when sslMode is CUSTOM_TRUST_STORE");
    }
  }

  private void validateRetrySettings() {
    if (retryMode == RetryMode.NONE || retryCount == 0) {
      return;
    }

    if (retryableStatusCodes.length == 0) {
      throw new IllegalArgumentException("retryableStatusCodes cannot be empty");
    }
  }

  private static String normalizeBaseUrl(String rawBaseUrl) {
    String trimmedBaseUrl = requireNotBlank(rawBaseUrl, "baseUrl");
    if (trimmedBaseUrl.endsWith("/")) {
      return trimmedBaseUrl.substring(0, trimmedBaseUrl.length() - 1);
    }
    return trimmedBaseUrl;
  }

  private static String normalizeOptional(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private static void validateBaseUrl(String baseUrl) {
    try {
      URI uri = new URI(baseUrl);
      String scheme = uri.getScheme();
      if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
        throw new IllegalArgumentException("baseUrl must start with http:// or https://");
      }
      if (uri.getHost() == null || uri.getHost().isBlank()) {
        throw new IllegalArgumentException("baseUrl must contain a host");
      }
    } catch (URISyntaxException exception) {
      throw new IllegalArgumentException("baseUrl is not a valid URI", exception);
    }
  }

  private static String requireNotBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
    return value.trim();
  }

  private static int requirePositive(int value, String fieldName) {
    if (value <= 0) {
      throw new IllegalArgumentException(fieldName + " must be greater than zero");
    }
    return value;
  }

  private static int requireNonNegative(int value, String fieldName) {
    if (value < 0) {
      throw new IllegalArgumentException(fieldName + " must not be negative");
    }
    return value;
  }

  private static Duration requireNonNullAndPositive(Duration value, String fieldName) {
    Objects.requireNonNull(value, fieldName + " cannot be null");
    if (value.isNegative() || value.isZero()) {
      throw new IllegalArgumentException(fieldName + " must be greater than zero");
    }
    return value;
  }

  /**
   * {@link BtSdkConfig} 的构建器。
   *
   * <p>未显式设置的参数会使用 SDK 默认值，最终在 {@link #build()} 时统一完成校验。
   */
  public static final class Builder {

    private String baseUrl;
    private String apiKey;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT_SECONDS;
    private TimeUnit connectTimeoutUnit = TimeUnit.SECONDS;
    private int readTimeout = DEFAULT_READ_TIMEOUT_SECONDS;
    private TimeUnit readTimeoutUnit = TimeUnit.SECONDS;
    private RetryMode retryMode = RetryMode.SAFE_REQUESTS_ONLY;
    private int retryCount = DEFAULT_RETRY_COUNT;
    private Duration retryInterval = Duration.ofSeconds(1);
    private int[] retryableStatusCodes = DEFAULT_RETRYABLE_STATUS_CODES.clone();
    private Map<String, String> extraHeaders = new LinkedHashMap<>();
    private boolean enableResponseLog = true;
    private boolean enableRequestLog = true;
    private SslMode sslMode = SslMode.SYSTEM_TRUST;
    private String trustStorePath;
    private String trustStorePassword;
    private String trustStoreType = "PKCS12";

    private Builder() {}

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder connectTimeout(int connectTimeout) {
      this.connectTimeout = connectTimeout;
      return this;
    }

    public Builder connectTimeout(Duration connectTimeout) {
      this.connectTimeout = toPositiveMillis(connectTimeout, "connectTimeout");
      this.connectTimeoutUnit = TimeUnit.MILLISECONDS;
      return this;
    }

    public Builder connectTimeoutUnit(TimeUnit connectTimeoutUnit) {
      this.connectTimeoutUnit = connectTimeoutUnit;
      return this;
    }

    public Builder readTimeout(int readTimeout) {
      this.readTimeout = readTimeout;
      return this;
    }

    public Builder readTimeout(Duration readTimeout) {
      this.readTimeout = toPositiveMillis(readTimeout, "readTimeout");
      this.readTimeoutUnit = TimeUnit.MILLISECONDS;
      return this;
    }

    public Builder readTimeoutUnit(TimeUnit readTimeoutUnit) {
      this.readTimeoutUnit = readTimeoutUnit;
      return this;
    }

    /**
     * @deprecated use {@link #retryMode(RetryMode)} instead.
     */
    @Deprecated
    public Builder enableRetry(boolean enableRetry) {
      this.retryMode = enableRetry ? RetryMode.SAFE_REQUESTS_ONLY : RetryMode.NONE;
      return this;
    }

    public Builder retryMode(RetryMode retryMode) {
      this.retryMode = Objects.requireNonNull(retryMode, "retryMode cannot be null");
      return this;
    }

    public Builder retryCount(int retryCount) {
      this.retryCount = retryCount;
      return this;
    }

    public Builder retryInterval(Duration retryInterval) {
      this.retryInterval = retryInterval;
      return this;
    }

    public Builder retryableStatusCodes(int... retryableStatusCodes) {
      this.retryableStatusCodes =
          retryableStatusCodes == null ? new int[0] : retryableStatusCodes.clone();
      return this;
    }

    public Builder extraHeaders(Map<String, String> extraHeaders) {
      this.extraHeaders =
          extraHeaders == null ? new LinkedHashMap<>() : new LinkedHashMap<>(extraHeaders);
      return this;
    }

    public Builder extraHeader(String name, String value) {
      Objects.requireNonNull(name, "header name cannot be null");
      Objects.requireNonNull(value, "header value cannot be null");
      this.extraHeaders.put(name, value);
      return this;
    }

    public Builder enableResponseLog(boolean enableResponseLog) {
      this.enableResponseLog = enableResponseLog;
      return this;
    }

    public Builder enableRequestLog(boolean enableRequestLog) {
      this.enableRequestLog = enableRequestLog;
      return this;
    }

    /**
     * @deprecated use {@link #sslMode(SslMode)} or {@link #trustStore(String, String)} instead.
     */
    @Deprecated
    public Builder verifySsl(boolean verifySsl) {
      this.sslMode = verifySsl ? SslMode.SYSTEM_TRUST : SslMode.INSECURE_TRUST_ALL;
      if (!verifySsl) {
        this.trustStorePath = null;
        this.trustStorePassword = null;
        this.trustStoreType = "PKCS12";
      }
      return this;
    }

    public Builder sslMode(SslMode sslMode) {
      this.sslMode = Objects.requireNonNull(sslMode, "sslMode cannot be null");
      if (sslMode != SslMode.CUSTOM_TRUST_STORE) {
        this.trustStorePath = null;
        this.trustStorePassword = null;
        this.trustStoreType = "PKCS12";
      }
      return this;
    }

    public Builder trustStore(String trustStorePath, String trustStorePassword) {
      return trustStore(trustStorePath, trustStorePassword, "PKCS12");
    }

    public Builder trustStore(
        String trustStorePath, String trustStorePassword, String trustStoreType) {
      this.sslMode = SslMode.CUSTOM_TRUST_STORE;
      this.trustStorePath = trustStorePath;
      this.trustStorePassword = trustStorePassword;
      this.trustStoreType = trustStoreType;
      return this;
    }

    public BtSdkConfig build() {
      return new BtSdkConfig(this);
    }

    private static int toPositiveMillis(Duration duration, String fieldName) {
      Objects.requireNonNull(duration, fieldName + " cannot be null");
      if (duration.isZero() || duration.isNegative()) {
        throw new IllegalArgumentException(fieldName + " must be greater than zero");
      }
      long millis = duration.toMillis();
      if (millis > Integer.MAX_VALUE) {
        throw new IllegalArgumentException(fieldName + " is too large to fit milliseconds int");
      }
      return (int) millis;
    }
  }

  public enum RetryMode {
    NONE,
    SAFE_REQUESTS_ONLY,
    ALL_REQUESTS
  }

  public enum SslMode {
    SYSTEM_TRUST,
    CUSTOM_TRUST_STORE,
    INSECURE_TRUST_ALL
  }
}
