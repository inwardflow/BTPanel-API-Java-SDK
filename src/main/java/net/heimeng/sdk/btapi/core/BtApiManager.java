package net.heimeng.sdk.btapi.core;

import net.heimeng.sdk.btapi.api.system.SystemApi;

/**
 * 宝塔API管理器
 * <p>
 * 提供对所有宝塔API的统一访问入口，管理各种API实例的创建和获取。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class BtApiManager {
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
    public void close() {
        client.close();
    }
}