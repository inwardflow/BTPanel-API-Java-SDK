package net.heimeng.sdk.btapi.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * BtSdkConfig配置类，用于配置宝塔SDK的行为
 * <p>
 * 采用Builder模式，提供灵活的配置选项，确保配置的不可变性和类型安全
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Builder
@Getter
@ToString
public class BtSdkConfig {
    
    /**
     * 基础URL，指向宝塔面板的访问地址
     */
    private final String baseUrl;
    
    /**
     * API密钥，用于身份验证
     */
    private final String apiKey;
    
    /**
     * 连接超时时间，默认为10秒
     */
    @Builder.Default
    private final int connectTimeout = 10;
    
    /**
     * 连接超时时间单位，默认为秒
     */
    @Builder.Default
    private final TimeUnit connectTimeoutUnit = TimeUnit.SECONDS;
    
    /**
     * 读取超时时间，默认为30秒
     */
    @Builder.Default
    private final int readTimeout = 30;
    
    /**
     * 读取超时时间单位，默认为秒
     */
    @Builder.Default
    private final TimeUnit readTimeoutUnit = TimeUnit.SECONDS;
    
    /**
     * 是否启用重试，默认为true
     */
    @Builder.Default
    private final boolean enableRetry = true;
    
    /**
     * 重试次数，默认为3次
     */
    @Default
    private int retryCount = 3;
    
    /**
     * 重试间隔，默认为1秒
     */
    @Builder.Default
    private final Duration retryInterval = Duration.ofSeconds(1);
    
    /**
     * 可重试的HTTP状态码，默认包括408、429、500、502、503、504
     */
    @Builder.Default
    private final int[] retryableStatusCodes = {408, 429, 500, 502, 503, 504};
    
    /**
     * 额外的HTTP头，默认为空Map
     */
    @Builder.Default
    private final Map<String, String> extraHeaders = Collections.emptyMap();
    
    /**
     * 是否启用响应日志，默认为true
     */
    @Builder.Default
    private final boolean enableResponseLog = true;
    
    /**
     * 是否启用请求日志，默认为true
     */
    @Builder.Default
    private final boolean enableRequestLog = true;
    
    /**
     * 是否验证SSL证书，默认为false（宝塔面板IP大部分使用自签证书）
     */
    @Builder.Default
    private final boolean verifySsl = false;
    
    // V2版本SDK不需要自定义Builder类，Lombok会自动生成
    // 直接使用builder().retryCount(3)即可设置重试次数
    
    /**
     * 检查配置是否有效
     * 
     * @return 如果配置有效则返回true，否则返回false
     */
    public boolean isValid() {
        return baseUrl != null && !baseUrl.isEmpty() && 
               apiKey != null && !apiKey.isEmpty() &&
               connectTimeout > 0 &&
               readTimeout > 0 &&
               retryCount >= 0;
    }
}