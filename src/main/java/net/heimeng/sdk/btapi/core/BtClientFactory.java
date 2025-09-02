package net.heimeng.sdk.btapi.core;

import lombok.extern.slf4j.Slf4j;

/**
 * 宝塔客户端工厂，用于创建BtClient实例
 * <p>
 * 提供多种创建客户端的便捷方法，支持通过配置对象或直接提供连接信息来创建客户端。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Slf4j
public class BtClientFactory {

    /**
     * 根据配置对象创建客户端
     *
     * @param config 客户端配置
     * @return BtClient实例
     */
    public static BtClient createClient(BtConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        log.debug("Creating BtClient with config: {}", config);
        return new DefaultBtClient(config);
    }

    /**
     * 使用基础连接信息创建客户端，采用默认配置
     *
     * @param baseUrl 宝塔面板的基础URL
     * @param apiKey API密钥
     * @param apiToken API令牌
     * @return BtClient实例
     */
    public static BtClient createClient(String baseUrl, String apiKey, String apiToken) {
        DefaultBtConfig config = DefaultBtConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .apiToken(apiToken)
                .build();
        return createClient(config);
    }

    /**
     * 创建默认配置的客户端构建器
     *
     * @return DefaultBtConfig.DefaultBtConfigBuilder实例
     */
    public static DefaultBtConfig.DefaultBtConfigBuilder configBuilder() {
        return DefaultBtConfig.builder();
    }

    /**
     * 创建预配置的默认客户端
     * <p>
     * 注意：此方法仅用于测试和演示，实际使用时应提供有效的连接信息
     * </p>
     *
     * @return 默认配置的BtClient实例
     * @deprecated 仅用于测试，实际使用时应提供有效的连接信息
     */
    @Deprecated
    public static BtClient createDefaultClient() {
        log.warn("Creating default client, which is for testing purposes only. Please provide valid connection information in production.");
        return createClient("http://localhost:8888", "default_api_key", "default_api_token");
    }
}