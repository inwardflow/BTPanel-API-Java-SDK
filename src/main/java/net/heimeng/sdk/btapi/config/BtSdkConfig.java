package net.heimeng.sdk.btapi.config;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * BtSdk配置类，提供客户端配置的标准实现
 * <p>
 * 此类使用Builder模式，允许灵活地配置客户端的各种参数。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Getter
@ToString
@Builder
@Slf4j
public class BtSdkConfig {
    /**
     * 创建默认配置实例
     *
     * @return 默认配置的BtSdkConfig实例
     */
    public static BtSdkConfig defaultConfig() {
        return builder().build();
    }
    // 基本配置
    private final String baseUrl;
    private final String apiKey;

    /**
     * 连接超时时间，默认值为10(秒)
     */
    @Builder.Default
    private final int connectTimeout = 10;
    @Builder.Default
    private final TimeUnit connectTimeoutUnit = TimeUnit.SECONDS;

    /**
     * 读取超时时间，默认值为30(秒)
     */
    @Builder.Default
    private final int readTimeout = 30;
    @Builder.Default
    private final TimeUnit readTimeoutUnit = TimeUnit.SECONDS;

    // 重试配置，默认值
    @Builder.Default
    private final int retryCount = 3;
    @Builder.Default
    private final Duration retryInterval = Duration.ofSeconds(1);
    @Builder.Default
    private final int[] retryableStatusCodes = {408, 429, 500, 502, 503, 504};

    // 额外配置
    @Builder.Default
    private final Map<String, String> extraHeaders = Collections.emptyMap();
    
    // 日志配置
    @Builder.Default
    private final boolean enableResponseLog = false;

    // 请求日志配置
    @Builder.Default
    private final boolean enableRequestLog = false;
    
    // SSL验证配置
    @Builder.Default
    private final boolean enableSslVerify = true;

    /**
     * 验证配置是否有效
     *
     * @return 如果配置有效则返回true，否则返回false
     */
    public boolean isValid() {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            log.warn("Base URL is required");
            return false;
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("API Key is required");
            return false;
        }

        try {
            // 验证URL格式
            new java.net.URL(baseUrl);
        } catch (Exception e) {
            log.warn("Invalid Base URL format: {}", baseUrl);
            return false;
        }

        // 验证超时设置
        if (connectTimeout < 0 || readTimeout < 0) {
            log.warn("Timeout values must be non-negative");
            return false;
        }

        // 验证重试设置
        if (retryCount < 0) {
            log.warn("Retry count must be non-negative");
            return false;
        }

        return true;
    }

    /**
     * 获取连接超时的Duration表示
     *
     * @return 连接超时Duration
     */
    public Duration getConnectTimeoutDuration() {
        return Duration.ofMillis(connectTimeoutUnit.toMillis(connectTimeout));
    }

    /**
     * 获取读取超时的Duration表示
     *
     * @return 读取超时Duration
     */
    public Duration getReadTimeoutDuration() {
        return Duration.ofMillis(readTimeoutUnit.toMillis(readTimeout));
    }
}