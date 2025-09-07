package net.heimeng.sdk.btapi.example;

import net.heimeng.sdk.btapi.v2.api.system.GetSystemInfoApi;
import net.heimeng.sdk.btapi.v2.api.website.GetWebsitesApi;
import net.heimeng.sdk.btapi.v2.client.BtClient;
import net.heimeng.sdk.btapi.v2.client.BtClientFactory;
import net.heimeng.sdk.btapi.v2.client.BtApiManager;
import net.heimeng.sdk.btapi.v2.config.BtSdkConfig;
import net.heimeng.sdk.btapi.v2.exception.BtApiException;
import net.heimeng.sdk.btapi.v2.model.BtResult;
import net.heimeng.sdk.btapi.v2.model.system.SystemInfo;
import net.heimeng.sdk.btapi.v2.model.website.WebsiteInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * V2版本SDK使用示例
 * <p>
 * 本示例展示了如何使用重构后的v2版本SDK来调用宝塔面板API，包括同步调用和异步调用方式。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class V2SdkExample {
    
    // 宝塔面板基础URL
    // private static final String BASE_URL = "http://your-panel-url:8888/";
    // 宝塔面板API密钥
    // private static final String API_KEY = "your-api-key";
    private static final String BASE_URL = System.getenv("BT_PANEL_URL");
    private static final String API_KEY = System.getenv("BT_PANEL_API_KEY");

    public static void main(String[] args) {
        // 创建并使用v2版本SDK
        try {
            System.out.println("===== V2版本SDK使用示例 =====");
            
            // 方式1：使用BtSdkConfig.Builder创建配置
            BtSdkConfig config = BtSdkConfig.builder()
                    .baseUrl(BASE_URL)
                    .apiKey(API_KEY)
                    .connectTimeout(30000)
                    .readTimeout(60000)
                    .enableRetry(true)
                    .retryCount(3)
                    .build();
            
            // 创建BtClient实例
            try (BtClient client = BtClientFactory.createClient(config)) {
                // 使用BtClient直接调用API（基础用法）
                directApiCallExample(client);
                
                // 使用BtApiManager调用API（推荐用法）
                apiManagerExample(client);
                
                // 异步API调用示例
                asyncApiCallExample(client);
            }
            
        } catch (Exception e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("===== 示例执行完毕 =====");
    }
    
    /**
     * 直接使用BtClient调用API示例
     */
    private static void directApiCallExample(BtClient client) {
        System.out.println("\n----- 直接使用BtClient调用API示例 -----");
        
        try {
            // 创建获取系统信息的API实例
            GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
            
            // 执行API调用
            BtResult<SystemInfo> result = client.execute(systemInfoApi);
            
            // 处理响应结果
            if (result.isSuccess()) {
                SystemInfo systemInfo = result.getData();
                if (systemInfo != null) {
                    System.out.println("服务器主机名: " + systemInfo.getHostname());
                    System.out.println("操作系统: " + systemInfo.getOs());
                    System.out.println("宝塔版本: " + systemInfo.getPanelVersion());
                    System.out.println("CPU使用率: " + systemInfo.getCpuUsage() + "%");
                    System.out.println("内存使用率: " + String.format("%.2f%%", systemInfo.getMemoryUsage()));
                    System.out.println("磁盘使用率: " + String.format("%.2f%%", systemInfo.getDiskUsage()));
                }
            } else {
                System.err.println("获取系统信息失败: " + result.getMsg());
            }
        } catch (BtApiException e) {
            System.err.println("API调用异常: " + e.getMessage());
        }
    }
    
    /**
     * 使用BtApiManager调用API示例
     */
    private static void apiManagerExample(BtClient client) {
        System.out.println("\n----- 使用BtApiManager调用API示例 -----");
        
        try {
            // 创建API管理器
            BtApiManager apiManager = new BtApiManager(client);
            
            // 创建获取网站列表的API实例（第1页，每页5条记录）
            GetWebsitesApi websitesApi = new GetWebsitesApi(1, 5);
            
            // 执行API调用
            BtResult<List<WebsiteInfo>> result = apiManager.execute(websitesApi);
            
            // 处理响应结果
            if (result.isSuccess()) {
                List<WebsiteInfo> websites = result.getData();
                if (websites != null && !websites.isEmpty()) {
                    System.out.println("网站列表: ");
                    for (WebsiteInfo website : websites) {
                        System.out.println("- " + website.getDomain() + " [" + website.getType() + "] " + 
                                          (website.isRunning() ? "运行中" : "已停止") + 
                                          (website.isSslEnabled() ? " [SSL]" : ""));
                    }
                } else {
                    System.out.println("没有找到网站");
                }
            } else {
                System.err.println("获取网站列表失败: " + result.getMsg());
            }
        } catch (BtApiException e) {
            System.err.println("API调用异常: " + e.getMessage());
        }
    }
    
    /**
     * 异步API调用示例
     */
    private static void asyncApiCallExample(BtClient client) {
        System.out.println("\n----- 异步API调用示例 -----");
        
        // 创建API管理器
        BtApiManager apiManager = new BtApiManager(client);
        
        // 创建获取系统信息的API实例
        GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
        
        // 执行异步API调用
        CompletableFuture<BtResult<SystemInfo>> future = apiManager.executeAsync(systemInfoApi);
        
        // 设置回调函数处理异步结果
        future.thenAccept(result -> {
            if (result.isSuccess()) {
                SystemInfo systemInfo = result.getData();
                if (systemInfo != null) {
                    System.out.println("异步获取系统信息成功");
                    System.out.println("服务器信息: " + systemInfo.getHostname() + ", " + systemInfo.getOs());
                }
            } else {
                System.err.println("异步获取系统信息失败: " + result.getMsg());
            }
        }).exceptionally(e -> {
            System.err.println("异步API调用异常: " + e.getMessage());
            return null;
        });
        
        // 等待异步操作完成（在实际应用中，可能不需要等待）
        try {
            System.out.println("等待异步操作完成...");
            future.join(); // 等待异步操作完成，但不会抛出受检异常
        } catch (Exception e) {
            System.err.println("异步操作异常: " + e.getMessage());
        }
    }
}