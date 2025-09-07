package net.heimeng.sdk.btapi.example;

import net.heimeng.sdk.btapi.v2.api.system.GetSystemInfoApi;
import net.heimeng.sdk.btapi.v2.client.BtApiManager;
import net.heimeng.sdk.btapi.v2.client.BtClient;
import net.heimeng.sdk.btapi.v2.client.BtClientFactory;
import net.heimeng.sdk.btapi.v2.model.BtResult;
import net.heimeng.sdk.btapi.v2.model.system.SystemInfo;

public class Application {

    // 宝塔面板基础URL和API密钥
    private static final String BASE_URL = System.getenv("BT_PANEL_URL");
    private static final String API_KEY = System.getenv("BT_PANEL_API_KEY");
    public static void main(String[] args) {
        // 创建客户端
        BtClient client = BtClientFactory.createClient(BASE_URL, API_KEY);

        // 创建API管理器
        BtApiManager apiManager = new BtApiManager(client);

        // 创建并执行获取系统信息的API
        GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
        BtResult<SystemInfo> systemInfoResult = apiManager.execute(systemInfoApi);

        // 处理结果
        if (systemInfoResult.isSuccess()) {
            SystemInfo systemInfo = systemInfoResult.getData();
            System.out.println("主机名: " + systemInfo.getHostname());
            System.out.println("CPU使用率: " + systemInfo.getCpuUsage() + "%");
            // 其他系统信息...
        } else {
            System.out.println("获取系统信息失败: " + systemInfoResult.getMsg());
        }

        // 关闭客户端
        apiManager.close();
    }
}
