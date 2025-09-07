package net.heimeng.sdk.btapi.core;

import net.heimeng.sdk.btapi.api.system.SystemApi;
import net.heimeng.sdk.btapi.exception.BtApiException;

/**
 * 宝塔API管理器
 * <p>
 * 提供对所有宝塔API的统一访问入口，管理各种API实例的创建和获取。
 * 支持通过静态工厂方法创建，也可以通过客户端实例创建。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class BtApiManager implements AutoCloseable {
    private final BtClient client;
    private SystemApi systemApi;

    /**
     * 构造BtApiManager实例
     *
     * @param client BtClient实例
     */
    public BtApiManager(BtClient client) {
        this.client = client;
    }

    /**
     * 根据配置创建BtApiManager实例
     *
     * @param config 客户端配置
     * @return BtApiManager实例
     * @throws BtApiException 当配置无效时抛出
     */
    public static BtApiManager create(BtConfig config) {
        if (config == null || !config.isValid()) {
            throw new BtApiException("Invalid configuration");
        }
        BtClient client = BtClientFactory.createClient(config);
        return new BtApiManager(client);
    }

    /**
     * 使用基础连接信息创建BtApiManager实例
     *
     * @param baseUrl 宝塔面板的基础URL
     * @param apiKey API密钥
     * @return BtApiManager实例
     * @throws BtApiException 当创建失败时抛出
     */
    public static BtApiManager create(String baseUrl, String apiKey) {
        BtConfig config = DefaultBtConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        return create(config);
    }

    /**
     * 获取系统相关API
     *
     * @return SystemApi实例
     */
    public SystemApi system() {
        if (systemApi == null) {
            systemApi = new SystemApi(client);
        }
        return systemApi;
    }

    /**
     * 获取BtClient实例
     *
     * @return BtClient实例
     */
    public BtClient getClient() {
        return client;
    }

    /**
     * 关闭API管理器，释放资源
     */
    @Override
    public void close() {
        client.close();
    }
}