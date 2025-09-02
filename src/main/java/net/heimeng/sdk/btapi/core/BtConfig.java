package net.heimeng.sdk.btapi.core;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 宝塔客户端配置接口，定义了客户端的配置属性和方法
 * <p>
 * 通过此接口，可以自定义客户端的连接超时、读取超时、重试策略等配置。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public interface BtConfig {

    /**
     * 默认连接超时时间（秒）
     */
    int DEFAULT_CONNECT_TIMEOUT = 10;

    /**
     * 默认读取超时时间（秒）
     */
    int DEFAULT_READ_TIMEOUT = 30;

    /**
     * 默认重试次数
     */
    int DEFAULT_RETRY_COUNT = 3;

    /**
     * 默认重试间隔
     */
    Duration DEFAULT_RETRY_INTERVAL = Duration.ofSeconds(1);

    /**
     * 默认可重试的HTTP状态码
     */
    int[] DEFAULT_RETRYABLE_STATUS_CODES = {408, 429, 500, 502, 503, 504};

    /**
     * 默认连接超时时间单位
     */
    TimeUnit DEFAULT_CONNECT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * 默认读取超时时间单位
     */
    TimeUnit DEFAULT_READ_TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * 默认额外HTTP请求头
     */
    Map<String, String> DEFAULT_EXTRA_HEADERS = Collections.emptyMap();

    /**
     * 获取宝塔面板的基础URL
     *
     * @return 基础URL，不能为空
     */
    String getBaseUrl();

    /**
     * 获取API密钥
     *
     * @return API密钥，不能为空
     */
    String getApiKey();

    /**
     * 获取API令牌
     *
     * @return API令牌，不能为空
     */
    String getApiToken();

    /**
     * 获取连接超时时间
     *
     * @return 连接超时时间，单位由 {@link #getConnectTimeoutUnit()} 指定，默认为10
     */
    default int getConnectTimeout() {
        return DEFAULT_CONNECT_TIMEOUT;
    }

    /**
     * 获取连接超时时间的时间单位
     *
     * @return 连接超时时间的时间单位，默认为 {@link TimeUnit#SECONDS}
     */
    default TimeUnit getConnectTimeoutUnit() {
        return DEFAULT_CONNECT_TIMEOUT_UNIT;
    }

    /**
     * 获取读取超时时间
     *
     * @return 读取超时时间，单位由 {@link #getReadTimeoutUnit()} 指定，默认为30
     */
    default int getReadTimeout() {
        return DEFAULT_READ_TIMEOUT;
    }

    /**
     * 获取读取超时时间的时间单位
     *
     * @return 读取超时时间的时间单位，默认为 {@link TimeUnit#SECONDS}
     */
    default TimeUnit getReadTimeoutUnit() {
        return DEFAULT_READ_TIMEOUT_UNIT;
    }

    /**
     * 获取请求重试次数
     *
     * @return 请求重试次数，默认为3
     */
    default int getRetryCount() {
        return DEFAULT_RETRY_COUNT;
    }

    /**
     * 获取请求重试间隔
     *
     * @return 请求重试间隔，默认为1秒
     */
    default Duration getRetryInterval() {
        return DEFAULT_RETRY_INTERVAL;
    }

    /**
     * 获取请求重试的HTTP状态码列表
     *
     * @return 请求重试的HTTP状态码列表，默认包含408, 429, 500, 502, 503, 504
     */
    default int[] getRetryableStatusCodes() {
        return DEFAULT_RETRYABLE_STATUS_CODES.clone();
    }

    /**
     * 获取额外的HTTP请求头
     *
     * @return 额外的HTTP请求头，默认为空Map
     */
    default Map<String, String> getExtraHeaders() {
        return DEFAULT_EXTRA_HEADERS;
    }

    /**
     * 获取连接超时的Duration表示
     *
     * @return 连接超时Duration
     */
    default Duration getConnectTimeoutDuration() {
        return Duration.ofMillis(getConnectTimeoutUnit().toMillis(getConnectTimeout()));
    }

    /**
     * 获取读取超时的Duration表示
     *
     * @return 读取超时Duration
     */
    default Duration getReadTimeoutDuration() {
        return Duration.ofMillis(getReadTimeoutUnit().toMillis(getReadTimeout()));
    }

    /**
     * 检查配置是否有效
     *
     * @return 如果配置有效则返回true，否则返回false
     */
    boolean isValid();

    /**
     * 创建一个新的Builder实例
     *
     * @return BtConfig.Builder实例
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * BtConfig构建器
     */
    class Builder {
        private String baseUrl;
        private String apiKey;
        private String apiToken;
        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private TimeUnit connectTimeoutUnit = DEFAULT_CONNECT_TIMEOUT_UNIT;
        private int readTimeout = DEFAULT_READ_TIMEOUT;
        private TimeUnit readTimeoutUnit = DEFAULT_READ_TIMEOUT_UNIT;
        private int retryCount = DEFAULT_RETRY_COUNT;
        private Duration retryInterval = DEFAULT_RETRY_INTERVAL;
        private int[] retryableStatusCodes = DEFAULT_RETRYABLE_STATUS_CODES.clone();
        private Map<String, String> extraHeaders = DEFAULT_EXTRA_HEADERS;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder apiToken(String apiToken) {
            this.apiToken = apiToken;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
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

        public Builder readTimeoutUnit(TimeUnit readTimeoutUnit) {
            this.readTimeoutUnit = readTimeoutUnit;
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
            this.retryableStatusCodes = retryableStatusCodes != null ?
                    retryableStatusCodes.clone() : new int[0];
            return this;
        }

        public Builder extraHeaders(Map<String, String> extraHeaders) {
            this.extraHeaders = extraHeaders != null ?
                    Collections.unmodifiableMap(extraHeaders) : Collections.emptyMap();
            return this;
        }

        public BtConfig build() {
            return new DefaultBtConfig(
                    baseUrl, apiKey, apiToken,
                    connectTimeout, connectTimeoutUnit,
                    readTimeout, readTimeoutUnit,
                    retryCount, retryInterval,
                    retryableStatusCodes, extraHeaders
            );
        }
    }
}