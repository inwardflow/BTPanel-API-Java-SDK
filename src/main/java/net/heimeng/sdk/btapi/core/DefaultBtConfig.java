package net.heimeng.sdk.btapi.core;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * BtConfig接口的默认实现，提供客户端配置的标准实现
 * <p>
 * 此类使用Builder模式，允许灵活地配置客户端的各种参数。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Getter
@ToString
@Builder(toBuilder = true)
@Slf4j
public class DefaultBtConfig implements BtConfig {
    // 基本配置
    private final String baseUrl;
    private final String apiKey;

    // 超时配置，默认值
    @lombok.Builder.Default
    private final int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    @lombok.Builder.Default
    private final TimeUnit connectTimeoutUnit = DEFAULT_CONNECT_TIMEOUT_UNIT;
    @lombok.Builder.Default
    private final int readTimeout = DEFAULT_READ_TIMEOUT;
    @lombok.Builder.Default
    private final TimeUnit readTimeoutUnit = DEFAULT_READ_TIMEOUT_UNIT;

    // 重试配置，默认值
    @lombok.Builder.Default
    private final int retryCount = DEFAULT_RETRY_COUNT;
    @lombok.Builder.Default
    private final Duration retryInterval = DEFAULT_RETRY_INTERVAL;
    @lombok.Builder.Default
    private final int[] retryableStatusCodes = DEFAULT_RETRYABLE_STATUS_CODES.clone();

    // 额外配置
    @lombok.Builder.Default
    private final Map<String, String> extraHeaders = DEFAULT_EXTRA_HEADERS;

    /**
     * 验证配置是否有效
     *
     * @return 如果配置有效则返回true，否则返回false
     */
    @Override
    public boolean isValid() {
        return validateBaseUrl() && validateApiKey() && validateTimeouts()
                && validateRetrySettings();
    }

    private boolean validateBaseUrl() {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            log.warn("Base URL is required but was null or empty.");
            return false;
        }

        try {
            new URL(baseUrl);
        } catch (MalformedURLException e) {
            log.warn("Invalid Base URL format: {}", baseUrl, e);
            return false;
        }

        return true;
    }

    private boolean validateApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("API Key is required but was null or empty.");
            return false;
        }
        return true;
    }

    private boolean validateTimeouts() {
        if (connectTimeout < 0 || readTimeout < 0) {
            log.warn("Timeout values must be non-negative. connectTimeout={}, readTimeout={}", connectTimeout, readTimeout);
            return false;
        }
        return true;
    }

    private boolean validateRetrySettings() {
        if (retryCount < 0) {
            log.warn("Retry count must be non-negative. retryCount={}", retryCount);
            return false;
        }
        return true;
    }

    // 私有构造函数，供Builder使用
    DefaultBtConfig(
            String baseUrl,
            String apiKey,
            int connectTimeout,
            TimeUnit connectTimeoutUnit,
            int readTimeout,
            TimeUnit readTimeoutUnit,
            int retryCount,
            Duration retryInterval,
            int[] retryableStatusCodes,
            Map<String, String> extraHeaders) {

        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.connectTimeout = connectTimeout;
        this.connectTimeoutUnit = Objects.requireNonNull(connectTimeoutUnit, "connectTimeoutUnit must not be null");
        this.readTimeout = readTimeout;
        this.readTimeoutUnit = Objects.requireNonNull(readTimeoutUnit, "readTimeoutUnit must not be null");
        this.retryCount = retryCount;
        this.retryInterval = Objects.requireNonNull(retryInterval, "retryInterval must not be null");
        this.retryableStatusCodes = retryableStatusCodes != null ?
                Arrays.copyOf(retryableStatusCodes, retryableStatusCodes.length) : new int[0];
        this.extraHeaders = extraHeaders != null ?
                Collections.unmodifiableMap(extraHeaders) : Collections.emptyMap();
    }
}