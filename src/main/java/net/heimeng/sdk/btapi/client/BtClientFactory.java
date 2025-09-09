package net.heimeng.sdk.btapi.client;

import net.heimeng.sdk.btapi.config.BtSdkConfig;

/**
 * BtClient工厂类，用于创建BtClient实例
 * <p>
 * 该工厂类提供了静态方法来创建客户端实例，简化了客户端的创建过程，并支持多种创建方式。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class BtClientFactory {
    
    /**
     * 创建BtClient实例
     * 
     * @param config 客户端配置
     * @return BtClient实例
     */
    public static BtClient createClient(BtSdkConfig config) {
        return new DefaultBtClient(config);
    }
    
    /**
     * 使用基础URL和API密钥创建BtClient实例
     * 
     * @param baseUrl 基础URL
     * @param apiKey API密钥
     * @return BtClient实例
     */
    public static BtClient createClient(String baseUrl, String apiKey) {
        BtSdkConfig config = BtSdkConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        return createClient(config);
    }
    
    /**
     * 创建默认的BtClient实例，使用默认配置
     * 
     * @param baseUrl 基础URL
     * @param apiKey API密钥
     * @param connectTimeout 连接超时时间
     * @param readTimeout 读取超时时间
     * @return BtClient实例
     */
    public static BtClient createClient(String baseUrl, String apiKey, int connectTimeout, int readTimeout) {
        BtSdkConfig config = BtSdkConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .build();
        return createClient(config);
    }
    
    /**
     * 创建自定义配置的BtClient实例
     * 
     * @param baseUrl 基础URL
     * @param apiKey API密钥
     * @param connectTimeout 连接超时时间
     * @param readTimeout 读取超时时间
     * @param retryCount 重试次数
     * @return BtClient实例
     */
    public static BtClient createClient(String baseUrl, String apiKey, int connectTimeout, int readTimeout, int retryCount) {
        BtSdkConfig config = BtSdkConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .retryCount(retryCount)
                .build();
        return createClient(config);
    }
}