package net.heimeng.sdk.btapi.example;

import net.heimeng.sdk.btapi.api.system.GetSystemInfoApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.client.BtClientFactory;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.SystemInfo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 宝塔面板SDK示例代码集
 * <p>
 * 本类提供了多种使用宝塔面板SDK的示例场景，包括：
 * <ul>
 *     <li>基础使用示例 - 创建客户端并调用API</li>
 *     <li>自定义配置示例 - 配置超时、重试策略等</li>
 *     <li>异步API调用示例 - 使用CompletableFuture处理异步操作</li>
 *     <li>异常处理示例 - 演示如何捕获和处理API调用过程中的异常</li>
 * </ul>
 * </p>
 */
public class BtSdkExamples {

    // 服务器基本信息（在实际使用中，请替换为您的服务器信息）
    private static final String DEFAULT_PANEL_URL = "http://localhost:8888";
    private static final String DEFAULT_API_KEY = "your_api_key_here";

    /**
     * 主方法，用于展示所有示例
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("=== 宝塔面板Java SDK示例 ===\n");
        
        // 执行各示例
        try {
            basicUsageExample();
            customConfigurationExample();
            asyncApiCallExample();
            asyncApiCallWithTimeoutExample();
            exceptionHandlingExample();
        } catch (Exception e) {
            System.err.println("示例执行过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 基础使用示例
     * <p>
     * 演示如何创建SDK客户端并执行基本的API调用
     * </p>
     */
    public static void basicUsageExample() {
        System.out.println("=== 基础使用示例 ===");
        
        try {
            // 1. 创建默认配置的客户端
            BtClient client = BtClientFactory.createClient(DEFAULT_PANEL_URL, DEFAULT_API_KEY);
            
            // 2. 创建API管理器
            BtApiManager apiManager = new BtApiManager(client);
            
            try {
                // 3. 创建并执行API请求
                GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
                BtResult<SystemInfo> result = apiManager.execute(systemInfoApi);
                
                // 4. 处理API响应
                if (result.isSuccess()) {
                    displaySystemInfo(result.getData());
                } else {
                    System.err.println("获取系统信息失败: " + result.getMsg());
                }
            } finally {
                // 5. 释放资源
                apiManager.close();
            }
        } catch (Exception e) {
            System.err.println("基础使用示例失败: " + e.getMessage());
            // 在实际应用中，建议根据具体情况处理异常
        }
        
        System.out.println("\n");
    }

    /**
     * 自定义配置示例
     * <p>
     * 演示如何使用自定义配置创建SDK客户端
     * </p>
     */
    public static void customConfigurationExample() {
        System.out.println("=== 自定义配置示例 ===");
        
        try {
            // 1. 创建自定义配置
            BtSdkConfig config = BtSdkConfig.builder()
                    .baseUrl(DEFAULT_PANEL_URL)
                    .apiKey(DEFAULT_API_KEY)
                    .connectTimeout(5)  // 连接超时时间，单位：秒
                    .readTimeout(10)     // 读取超时时间，单位：秒
                    .retryCount(3)       // 失败重试次数
                    .retryInterval(Duration.ofMillis(1000)) // 重试间隔，单位：毫秒
                    .build();
            
            // 2. 使用自定义配置创建客户端
            BtClient client = BtClientFactory.createClient(config);
            
            // 3. 创建API管理器
            BtApiManager apiManager = new BtApiManager(client);
            
            try {
                // 4. 执行API调用（与基础示例相同）
                GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
                BtResult<SystemInfo> result = apiManager.execute(systemInfoApi);
                
                // 5. 处理API响应
                if (result.isSuccess()) {
                    System.out.println("使用自定义配置成功获取系统信息");
                } else {
                    System.err.println("使用自定义配置获取系统信息失败: " + result.getMsg());
                }
            } finally {
                // 6. 释放资源
                apiManager.close();
            }
        } catch (Exception e) {
            System.err.println("自定义配置示例失败: " + e.getMessage());
        }
        
        System.out.println("\n");
    }

    /**
     * 异步API调用示例
     * <p>
     * 演示如何使用CompletableFuture进行异步API调用
     * </p>
     */
    public static void asyncApiCallExample() {
        System.out.println("=== 异步API调用示例 ===");
        
        try {
            // 1. 创建客户端和API管理器
            BtClient client = BtClientFactory.createClient(DEFAULT_PANEL_URL, DEFAULT_API_KEY);
            BtApiManager apiManager = new BtApiManager(client);
            
            // 2. 创建API请求
            GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
            
            // 3. 执行异步API调用
            CompletableFuture<BtResult<SystemInfo>> future = apiManager.executeAsyncFuture(systemInfoApi);
            
            // 4. 添加回调处理
            future.thenAccept(result -> {
                if (result.isSuccess()) {
                    System.out.println("异步调用成功获取系统信息");
                    displaySystemInfo(result.getData());
                } else {
                    System.err.println("异步调用获取系统信息失败: " + result.getMsg());
                }
            }).exceptionally(e -> {
                System.err.println("异步调用发生异常: " + e.getMessage());
                return null;
            });
            
            // 5. 等待异步调用完成（实际应用中，您可以根据需要决定是否等待）
            try {
                future.get(15, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                System.err.println("异步调用超时");
            }
            
            // 注意：在实际应用中，apiManager的关闭时机应根据应用生命周期决定
            // 在此示例中，为了简化演示，我们不立即关闭apiManager
            System.out.println("异步API调用示例执行完成\n");
        } catch (Exception e) {
            System.err.println("异步API调用示例失败: " + e.getMessage());
        }
    }

    /**
     * 带超时的异步API调用示例
     * <p>
     * 演示如何使用带超时的异步API调用
     * </p>
     */
    public static void asyncApiCallWithTimeoutExample() {
        System.out.println("=== 带超时的异步API调用示例 ===");
        
        try {
            // 1. 创建客户端和API管理器
            BtClient client = BtClientFactory.createClient(DEFAULT_PANEL_URL, DEFAULT_API_KEY);
            BtApiManager apiManager = new BtApiManager(client);
            
            try {
                // 2. 创建API请求
                GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
                
                // 3. 执行带超时的异步API调用
                Duration timeout = Duration.ofSeconds(10);
                CompletableFuture<BtResult<SystemInfo>> future = 
                        apiManager.executeAsyncWithTimeoutFuture(systemInfoApi, timeout);
                
                // 4. 等待结果
                BtResult<SystemInfo> result = future.get();
                
                // 5. 处理结果
                if (result.isSuccess()) {
                    System.out.println("带超时的异步调用成功获取系统信息");
                } else {
                    System.err.println("带超时的异步调用获取系统信息失败: " + result.getMsg());
                }
            } catch (ExecutionException e) {
                // 处理执行异常
                Throwable cause = e.getCause();
                if (cause instanceof TimeoutException) {
                    System.err.println("带超时的异步调用超时: " + cause.getMessage());
                } else {
                    System.err.println("带超时的异步调用发生异常: " + cause.getMessage());
                }
            } finally {
                // 6. 释放资源
                apiManager.close();
            }
        } catch (Exception e) {
            System.err.println("带超时的异步API调用示例失败: " + e.getMessage());
        }
        
        System.out.println("\n");
    }

    /**
     * 异常处理示例
     * <p>
     * 演示如何捕获和处理API调用过程中的各种异常
     * </p>
     */
    public static void exceptionHandlingExample() {
        System.out.println("=== 异常处理示例 ===");
        
        try {
            // 1. 创建客户端和API管理器
            // 注意：此处使用了可能导致异常的配置
            BtClient client = BtClientFactory.createClient(DEFAULT_PANEL_URL, "invalid_api_key");
            BtApiManager apiManager = new BtApiManager(client);
            
            try {
                // 2. 创建并执行API请求
                GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
                BtResult<SystemInfo> result = apiManager.execute(systemInfoApi);
                
                // 3. 处理API响应
                if (result.isSuccess()) {
                    displaySystemInfo(result.getData());
                } else {
                    System.err.println("API返回错误: " + result.getMsg());
                }
            } catch (BtApiException e) {
                // 捕获宝塔API特定异常
                System.err.println("宝塔API异常: " + e.getMessage());
                System.err.println("错误码: " + e.getErrorCode());
            } catch (Exception e) {
                // 捕获其他通用异常
                System.err.println("发生通用异常: " + e.getMessage());
            } finally {
                // 4. 释放资源
                apiManager.close();
            }
        } catch (Exception e) {
            System.err.println("初始化客户端失败: " + e.getMessage());
        }
        
        System.out.println("\n");
    }

    /**
     * 显示系统信息
     * 
     * @param systemInfo 系统信息对象
     */
    private static void displaySystemInfo(SystemInfo systemInfo) {
        if (systemInfo == null) {
            System.out.println("系统信息为null");
            return;
        }
        
        // 获取主机名（带空值检查）
        String hostname = systemInfo.getHostname() != null ? systemInfo.getHostname() : "Unknown";
        
        System.out.println("系统信息:");
        System.out.println("- 主机名: " + hostname);
        System.out.println("- 操作系统: " + systemInfo.getOs());
        System.out.println("- 内核版本: " + systemInfo.getKernel());
        System.out.println("- CPU使用率: " + systemInfo.getCpuUsage() + "%");
        System.out.println("- 内存总量: " + systemInfo.getMemoryTotal() + " MB");
        System.out.println("- 已用内存: " + systemInfo.getMemoryUsed() + " MB");
        System.out.println("- 内存使用率: " + String.format("%.1f%%", systemInfo.getMemoryUsagePercentage()));
        System.out.println("- 面板版本: " + systemInfo.getPanelVersion());
    }
}