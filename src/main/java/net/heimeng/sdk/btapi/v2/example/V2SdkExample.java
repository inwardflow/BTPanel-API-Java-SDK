package net.heimeng.sdk.btapi.v2.example;

import net.heimeng.sdk.btapi.v2.api.system.GetSystemInfoApi;
import net.heimeng.sdk.btapi.v2.api.website.GetWebsitesApi;
import net.heimeng.sdk.btapi.v2.client.BtApiManager;
import net.heimeng.sdk.btapi.v2.client.BtClient;
import net.heimeng.sdk.btapi.v2.client.BtClientFactory;
import net.heimeng.sdk.btapi.v2.config.BtSdkConfig;
import net.heimeng.sdk.btapi.v2.exception.BtApiException;
import net.heimeng.sdk.btapi.v2.model.BtResult;
import net.heimeng.sdk.btapi.v2.model.system.SystemInfo;
import net.heimeng.sdk.btapi.v2.model.website.WebsiteInfo;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * BTPanel-API-Java-SDK v2版本使用示例
 * <p>
 * 本示例展示了如何使用SDK v2版本进行同步和异步API调用、异常处理以及自定义配置等功能。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class V2SdkExample {
    
    // 宝塔面板基础URL和API密钥
    private static final String BASE_URL = System.getenv("BT_PANEL_URL");
    private static final String API_KEY = System.getenv("BT_PANEL_API_KEY");

    /**
     * 主方法，展示SDK的使用方式
     */
    public static void main(String[] args) {
        System.out.println("=== BTPanel-API-Java-SDK v2 示例程序 ===\n");
        
        try {
            // 1. 创建默认配置的客户端并使用
            basicUsageExample();
            
            // 2. 使用自定义配置
            customConfigExample();
            
            // 3. 异步API调用示例
            asyncApiExample();
            
            // 4. 带超时的异步API调用示例
            asyncApiWithTimeoutExample();
            
            // 5. 异常处理示例
            exceptionHandlingExample();
            
        } catch (Exception e) {
            System.err.println("示例程序执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 基础使用示例
     */
    private static void basicUsageExample() {
        System.out.println("[基础使用示例]");
        
        try {
            // 1. 创建客户端
            BtClient client = BtClientFactory.createClient(BASE_URL, API_KEY);
            
            // 2. 创建API管理器
            BtApiManager apiManager = new BtApiManager(client);
            
            // 3. 创建并执行获取系统信息的API
            GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
            BtResult<SystemInfo> systemInfoResult = apiManager.execute(systemInfoApi);
            
            // 4. 处理结果
            if (systemInfoResult.isSuccess()) {
                SystemInfo systemInfo = systemInfoResult.getData();
                System.out.println("服务器信息:");
                // 如果主机名为空或null，显示"Unknown"作为默认值
                System.out.println("  主机名: " + (systemInfo.getHostname() != null && !systemInfo.getHostname().isEmpty() ? systemInfo.getHostname() : "Unknown"));
                System.out.println("  操作系统: " + systemInfo.getOs());
                System.out.println("  CPU使用率: " + systemInfo.getCpuUsage() + "%");
                // 直接使用MB单位显示内存，因为SystemInfo中的字段已经是MB单位
                System.out.println("  内存使用: " + systemInfo.getMemoryUsed() + " MB / " + 
                                   systemInfo.getMemoryTotal() + " MB");
                System.out.println("  宝塔版本: " + systemInfo.getPanelVersion());
            } else {
                System.out.println("获取系统信息失败: " + systemInfoResult.getMsg());
            }
            
            // 5. 创建并执行获取网站列表的API
            GetWebsitesApi websitesApi = new GetWebsitesApi(1, 1); // 第1页，每页20条
            BtResult<List<WebsiteInfo>> websitesResult = apiManager.execute(websitesApi);
            
            // 6. 处理结果
            if (websitesResult.isSuccess()) {
                List<WebsiteInfo> websites = websitesResult.getData();
                System.out.println("\n网站列表 (共" + websites.size() + "个):");
                
                for (int i = 0; i < Math.min(5, websites.size()); i++) { // 只显示前5个
                    WebsiteInfo website = websites.get(i);
                    System.out.println("  " + (i+1) + ". " + website.getDomain() + " (" + website.getType() + ")");
                }
                
                if (websites.size() > 5) {
                    System.out.println("  ... 还有" + (websites.size() - 5) + "个网站");
                }
            } else {
                System.out.println("获取网站列表失败: " + websitesResult.getMsg());
            }
            
            // 7. 关闭客户端
            apiManager.close();
            
        } catch (Exception e) {
            System.err.println("基础使用示例出错: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 自定义配置示例
     */
    private static void customConfigExample() {
        System.out.println("[自定义配置示例]");
        
        try {
            // 1. 创建自定义配置
            BtSdkConfig config = BtSdkConfig.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .connectTimeout(5) // 连接超时5秒
                .readTimeout(60) // 读取超时60秒
                .retryCount(5) // 重试5次
                .retryInterval(Duration.ofSeconds(2)) // 重试间隔2秒
                .enableRequestLog(true) // 启用请求日志
                .enableResponseLog(true) // 启用响应日志
                .extraHeaders(Map.of("User-Agent", "BTPanel-Java-SDK-Example")) // 自定义请求头
                .build();
            
            // 2. 使用自定义配置创建客户端
            BtClient client = BtClientFactory.createClient(config);
            BtApiManager apiManager = new BtApiManager(client);
            
            // 3. 执行API
            GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
            BtResult<SystemInfo> result = apiManager.execute(systemInfoApi);
            
            System.out.println("使用自定义配置调用API: " + 
                              (result.isSuccess() ? "成功" : "失败: " + result.getMsg()));
            
            // 4. 关闭客户端
            apiManager.close();
            
        } catch (Exception e) {
            System.err.println("自定义配置示例出错: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 异步API调用示例
     */
    private static void asyncApiExample() {
        System.out.println("[异步API调用示例]");
        
        try {
            // 1. 创建客户端
            BtClient client = BtClientFactory.createClient(BASE_URL, API_KEY);
            BtApiManager apiManager = new BtApiManager(client);
            
            // 2. 异步执行获取系统信息的API
            GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
            CompletableFuture<BtResult<SystemInfo>> future = apiManager.executeAsync(systemInfoApi);
            
            System.out.println("异步请求已发送，等待响应...");
            
            // 3. 添加回调处理
            future.thenAccept(result -> {
                if (result.isSuccess()) {
                    System.out.println("异步请求成功: 获取到系统信息");
                } else {
                    System.out.println("异步请求失败: " + result.getMsg());
                }
            }).exceptionally(ex -> {
                System.err.println("异步请求发生异常: " + ex.getMessage());
                return null;
            });
            
            // 等待异步操作完成
            future.join();
            
            // 4. 关闭客户端
            apiManager.close();
            
        } catch (Exception e) {
            System.err.println("异步API调用示例出错: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 带超时的异步API调用示例
     */
    private static void asyncApiWithTimeoutExample() {
        System.out.println("[带超时的异步API调用示例]");
        
        try {
            // 1. 创建客户端
            BtClient client = BtClientFactory.createClient(BASE_URL, API_KEY);
            BtApiManager apiManager = new BtApiManager(client);
            
            // 2. 带超时执行异步API
            GetWebsitesApi websitesApi = new GetWebsitesApi();
            BtResult<List<WebsiteInfo>> result = 
                apiManager.executeAsyncWithTimeout(websitesApi, 10, TimeUnit.SECONDS);
            
            System.out.println("带超时的异步请求完成: " + 
                              (result.isSuccess() ? "成功获取到" + result.getData().size() + "个网站" : 
                               "失败: " + result.getMsg()));
            
            // 3. 关闭客户端
            apiManager.close();
            
        } catch (Exception e) {
            System.err.println("带超时的异步API调用示例出错: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 异常处理示例
     */
    private static void exceptionHandlingExample() {
        System.out.println("[异常处理示例]");
        
        try {
            // 1. 创建客户端 (使用无效的API密钥模拟错误情况)
            BtClient client = BtClientFactory.createClient(BASE_URL, "invalid_api_key");
            BtApiManager apiManager = new BtApiManager(client);

            // 2. 执行API并捕获异常
            GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
            try {
                BtResult<SystemInfo> result = apiManager.execute(systemInfoApi);
                System.out.println("API调用结果: " + (result.isSuccess() ? "成功" : "失败"));
            } catch (BtApiException e) {
                System.out.println("捕获到BtApiException异常:");
                System.out.println("  错误消息: " + e.getMessage());
                System.out.println("  错误代码: " + e.getErrorCode());
                if (e.getStatusCode() != null) {
                    System.out.println("  HTTP状态码: " + e.getStatusCode());
                }
            } catch (Exception e) {
                System.out.println("捕获到其他异常: " + e.getMessage());
            }

            // 3. 关闭客户端
            apiManager.close();

        } catch (Exception e) {
            System.err.println("异常处理示例出错: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 辅助方法：将字节数格式化为人类可读的单位
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int k = 1024;
        String[] units = {"KB", "MB", "GB", "TB"};
        int i = (int) Math.floor(Math.log(bytes) / Math.log(k));
        return String.format("%.2f %s", bytes / Math.pow(k, i), units[i]);
    }
}