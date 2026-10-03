package net.heimeng.sdk.btapi.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * SDK 的不可变配置对象。
 *
 * <p>该对象会在构建阶段完成地址规范化、超时参数校验和重试参数校验，尽量把明显的配置错误前置到启动时。
 */
public final class BtSdkConfig {

  private static final String DEFAULT_TRUST_STORE_TYPE = "PKCS12";
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
  private final Duration maxRetryInterval;
  private final int[] retryableStatusCodes;
  private final Map<String, String> extraHeaders;
  private final boolean enableResponseLog;
  private final boolean enableRequestLog;
  private final SslMode sslMode;
  private final String trustStorePath;
  private final String trustStorePassword;
  private final String trustStoreType;
  private final List<String> pinnedPublicKeys;

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
    this.maxRetryInterval = requireNonNullAndPositive(builder.maxRetryInterval, "maxRetryInterval");
    this.retryableStatusCodes = builder.retryableStatusCodes.clone();
    this.extraHeaders = Map.copyOf(new LinkedHashMap<>(builder.extraHeaders));
    this.enableResponseLog = builder.enableResponseLog;
    this.enableRequestLog = builder.enableRequestLog;
    this.sslMode = Objects.requireNonNull(builder.sslMode, "sslMode cannot be null");
    this.trustStorePath = normalizeOptional(builder.trustStorePath);
    this.trustStorePassword = builder.trustStorePassword;
    this.trustStoreType = normalizeOptional(builder.trustStoreType);
    this.pinnedPublicKeys = normalizePins(builder.pinnedPublicKeys);

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

  /** 指数退避的等待上限。 */
  public Duration getMaxRetryInterval() {
    return maxRetryInterval;
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

  /** {@link SslMode#PINNED_PUBLIC_KEY} 模式下允许的服务端公钥指纹，格式为 {@code sha256/<base64>}。 */
  public List<String> getPinnedPublicKeys() {
    return pinnedPublicKeys;
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
        + ", maxRetryInterval="
        + maxRetryInterval
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
        + ", pinnedPublicKeys="
        + pinnedPublicKeys
        + '}';
  }

  private void validateSslSettings() {
    if (sslMode == SslMode.PINNED_PUBLIC_KEY && pinnedPublicKeys.isEmpty()) {
      throw new IllegalArgumentException(
          "At least one pinned public key is required when sslMode is PINNED_PUBLIC_KEY");
    }
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
    if (maxRetryInterval.compareTo(retryInterval) < 0) {
      throw new IllegalArgumentException("maxRetryInterval cannot be less than retryInterval");
    }
    if (retryMode == RetryMode.NONE || retryCount == 0) {
      return;
    }

    if (retryableStatusCodes.length == 0) {
      throw new IllegalArgumentException("retryableStatusCodes cannot be empty");
    }
  }

  private static List<String> normalizePins(List<String> pins) {
    List<String> normalized = new ArrayList<>();
    for (String pin : pins) {
      String validPin = CertificatePins.requireValid(pin);
      if (!normalized.contains(validPin)) {
        normalized.add(validPin);
      }
    }
    return List.copyOf(normalized);
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
    private Duration maxRetryInterval = Duration.ofSeconds(30);
    private int[] retryableStatusCodes = DEFAULT_RETRYABLE_STATUS_CODES.clone();
    private Map<String, String> extraHeaders = new LinkedHashMap<>();
    private boolean enableResponseLog = true;
    private boolean enableRequestLog = true;
    private SslMode sslMode = SslMode.SYSTEM_TRUST;
    private String trustStorePath;
    private String trustStorePassword;
    private String trustStoreType = DEFAULT_TRUST_STORE_TYPE;
    private List<String> pinnedPublicKeys = new ArrayList<>();

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

    /** 首次重试前的基础等待时间，之后按指数增长并加入随机抖动。 */
    public Builder retryInterval(Duration retryInterval) {
      this.retryInterval = retryInterval;
      return this;
    }

    /** 指数退避的等待上限，默认 30 秒；也用于限制服务端 {@code Retry-After} 的等待时间。 */
    public Builder maxRetryInterval(Duration maxRetryInterval) {
      this.maxRetryInterval = maxRetryInterval;
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
        this.trustStoreType = DEFAULT_TRUST_STORE_TYPE;
      }
      return this;
    }

    public Builder sslMode(SslMode sslMode) {
      this.sslMode = Objects.requireNonNull(sslMode, "sslMode cannot be null");
      if (sslMode != SslMode.CUSTOM_TRUST_STORE) {
        this.trustStorePath = null;
        this.trustStorePassword = null;
        this.trustStoreType = DEFAULT_TRUST_STORE_TYPE;
      }
      if (sslMode != SslMode.PINNED_PUBLIC_KEY) {
        this.pinnedPublicKeys = new ArrayList<>();
      }
      return this;
    }

    /**
     * 只信任公钥指纹匹配的服务端证书，适用于宝塔默认的自签名面板证书。
     *
     * <p>比关闭证书校验安全得多：只有持有对应私钥的服务端才能通过握手。证书续期时只要公钥不变，指纹就不变。 可以用下面的命令获取指纹，并通过可信渠道（例如在面板服务器本机执行）核对：
     *
     * <pre>{@code
     * openssl s_client -connect host:port </dev/null 2>/dev/null \
     *   | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der \
     *   | openssl dgst -sha256 -binary | openssl enc -base64
     * }</pre>
     *
     * <p>条件允许时，更推荐按宝塔官方教程为面板申请受信任的 IP 证书，然后使用默认的 {@link SslMode#SYSTEM_TRUST}。
     *
     * @param pins 一个或多个 {@code sha256/<base64>} 格式的指纹；轮换公钥时可同时配置新旧指纹
     * @return 当前 Builder
     */
    public Builder pinnedPublicKeys(String... pins) {
      Objects.requireNonNull(pins, "pins cannot be null");
      sslMode(SslMode.PINNED_PUBLIC_KEY);
      this.pinnedPublicKeys = new ArrayList<>(Arrays.asList(pins));
      return this;
    }

    public Builder trustStore(String trustStorePath, String trustStorePassword) {
      return trustStore(trustStorePath, trustStorePassword, DEFAULT_TRUST_STORE_TYPE);
    }

    public Builder trustStore(
        String trustStorePath, String trustStorePassword, String trustStoreType) {
      this.sslMode = SslMode.CUSTOM_TRUST_STORE;
      this.pinnedPublicKeys = new ArrayList<>();
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

  /** 服务端证书的信任方式。 */
  public enum SslMode {
    /** 使用 JDK 默认信任库校验证书链和主机名。面板配置了受信任证书时使用，这是默认值。 */
    SYSTEM_TRUST,
    /** 使用自定义信任库（例如导入了面板自签 CA 的 PKCS12 文件）。 */
    CUSTOM_TRUST_STORE,
    /** 只信任公钥指纹匹配的证书，适用于宝塔默认的自签名证书。见 {@link Builder#pinnedPublicKeys}。 */
    PINNED_PUBLIC_KEY,
    /** 不校验证书，会受到中间人攻击。只应在隔离的测试环境中使用，启用时 SDK 会输出警告日志。 */
    INSECURE_TRUST_ALL
  }
}
