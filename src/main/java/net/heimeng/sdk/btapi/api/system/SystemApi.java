package net.heimeng.sdk.btapi.api.system;

import net.heimeng.sdk.btapi.core.BtClient;
import net.heimeng.sdk.btapi.model.system.ServiceStatusList;
import net.heimeng.sdk.btapi.model.system.SystemInfo;
import net.heimeng.sdk.btapi.model.system.SystemLoadInfo;

import java.util.concurrent.CompletableFuture;

/**
 * 系统相关API的集合类
 * <p>
 * 提供对宝塔面板系统相关API的统一访问入口，包括获取系统信息、服务状态等功能。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SystemApi {
    private final BtClient client;

    /**
     * 构造SystemApi实例
     *
     * @param client BtClient实例
     */
    public SystemApi(BtClient client) {
        this.client = client;
    }

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    public SystemInfo getSystemInfo() {
        SystemInfoApi api = new SystemInfoApi();
        return client.execute(api);
    }

    /**
     * 异步获取系统信息
     *
     * @return 包含系统信息的CompletableFuture
     */
    public CompletableFuture<SystemInfo> getSystemInfoAsync() {
        SystemInfoApi api = new SystemInfoApi();
        return client.executeAsync(api);
    }

    /**
     * 获取系统负载信息
     *
     * @return 系统负载信息
     */
    public SystemLoadInfo getSystemLoad() {
        SystemLoadApi api = new SystemLoadApi();
        return client.execute(api);
    }

    /**
     * 异步获取系统负载信息
     *
     * @return 包含系统负载信息的CompletableFuture
     */
    public CompletableFuture<SystemLoadInfo> getSystemLoadAsync() {
        SystemLoadApi api = new SystemLoadApi();
        return client.executeAsync(api);
    }

    /**
     * 获取服务状态列表
     *
     * @return 服务状态列表
     */
    public ServiceStatusList getServiceStatusList() {
        ServiceStatusListApi api = new ServiceStatusListApi();
        return client.execute(api);
    }

    /**
     * 异步获取服务状态列表
     *
     * @return 包含服务状态列表的CompletableFuture
     */
    public CompletableFuture<ServiceStatusList> getServiceStatusListAsync() {
        ServiceStatusListApi api = new ServiceStatusListApi();
        return client.executeAsync(api);
    }

    /**
     * 重启面板
     *
     * @return 操作结果
     */
    public boolean restartPanel() {
        RestartPanelApi api = new RestartPanelApi();
        return client.execute(api);
    }

    /**
     * 异步重启面板
     *
     * @return 包含操作结果的CompletableFuture
     */
    public CompletableFuture<Boolean> restartPanelAsync() {
        RestartPanelApi api = new RestartPanelApi();
        return client.executeAsync(api);
    }
}