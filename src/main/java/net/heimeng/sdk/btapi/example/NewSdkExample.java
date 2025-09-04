package net.heimeng.sdk.btapi.example;

import net.heimeng.sdk.btapi.core.BtClient;
import net.heimeng.sdk.btapi.core.BtClientFactory;
import net.heimeng.sdk.btapi.core.BtConfig;
import net.heimeng.sdk.btapi.core.BtApiManager;
import net.heimeng.sdk.btapi.core.Interceptor;
import net.heimeng.sdk.btapi.core.RequestContext;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.system.SystemInfo;
import net.heimeng.sdk.btapi.model.system.ServiceStatusList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * 新SDK使用示例
 * <p>
 * 展示如何使用新的API架构进行同步和异步API调用，以及如何应用拦截器等高级功能。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class NewSdkExample {
    private static final Logger log = LoggerFactory.getLogger(NewSdkExample.class);

    public static void main(String[] args) {
        // 初始化SDK客户端
        BtClient client = initClient();
        
        try {
            // 创建API管理器
            BtApiManager apiManager = new BtApiManager(client);
            
            // 示例1：同步调用API获取系统信息
            systemInfoSyncExample(apiManager);
            
            // 示例2：异步调用API获取服务状态
            serviceStatusAsyncExample(apiManager);
            
            // 示例3：使用自定义配置创建客户端
            customConfigExample();
            
        } catch (Exception e) {
            log.error("SDK usage example failed", e);
        } finally {
            // 关闭客户端，释放资源
            client.close();
        }
    }

    /**
     * 初始化SDK客户端
     *
     * @return BtClient实例
     */
    private static BtClient initClient() {
        // 从环境变量或配置文件加载敏感信息
        // 推荐在实际使用时通过环境变量设置这些值
        String baseUrl = getEnvOrDefault("BT_PANEL_URL", "http://your-bt-panel-url:8888");
        String apiKey = getEnvOrDefault("BT_PANEL_API_KEY", "your-api-key");

        // 检查是否使用了默认值（用于演示环境）
        if (apiKey.equals("your-api-key")) {
            log.warn("正在使用默认API密钥，请在生产环境中通过环境变量设置实际的密钥");
            log.warn("示例: export BT_PANEL_URL=\"http://your-bt-panel-url:8888\"");
            log.warn("       export BT_PANEL_API_KEY=\"your-actual-api-key\"");
        }

        // 创建客户端
        BtClient client = BtClientFactory.createClient(baseUrl, apiKey);
        
        // 添加日志拦截器
        client.addInterceptor(new LoggingInterceptor());
        
        // 添加重试拦截器
        client.addInterceptor(new RetryInterceptor(3, 1000));
        
        return client;
    }
    
    /**
     * 获取环境变量或默认值
     * 
     * @param key 环境变量键名
     * @param defaultValue 默认值
     * @return 环境变量值或默认值
     */
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 同步调用API获取系统信息示例
     *
     * @param apiManager API管理器
     */
    private static void systemInfoSyncExample(BtApiManager apiManager) {
        try {
            log.info("\n--- 同步调用API获取系统信息示例 ---");
            
            // 同步获取系统信息
            SystemInfo systemInfo = apiManager.system().getSystemInfo();
            
            // 处理结果
            if (systemInfo != null && systemInfo.isSuccess()) {
                log.info("系统信息获取成功");
                log.info("操作系统: {}", systemInfo.getOsName());
                log.info("宝塔版本: {}", systemInfo.getBtVersion());
                log.info("CPU使用率: {}%", systemInfo.getCpuUsage());
                log.info("内存使用率: {}%", systemInfo.getMemoryUsage());
            } else {
                log.error("系统信息获取失败: {}", systemInfo.getMsg());
            }
        } catch (BtApiException e) {
            log.error("系统信息获取异常: {}", e.getMessage());
        }
    }

    /**
     * 异步调用API获取服务状态示例
     *
     * @param apiManager API管理器
     */
    private static void serviceStatusAsyncExample(BtApiManager apiManager) {
        try {
            log.info("\n--- 异步调用API获取服务状态示例 ---");
            
            // 异步获取服务状态列表
            CompletableFuture<ServiceStatusList> future = apiManager.system().getServiceStatusListAsync();
            
            // 处理异步结果
            future.thenAccept(serviceStatusList -> {
                if (serviceStatusList != null && serviceStatusList.isSuccess()) {
                    log.info("服务状态列表获取成功");
                    serviceStatusList.getServices().forEach(service -> {
                        log.info("服务名称: {}, 状态: {}", service.getDisplayName(), service.getStatus());
                    });
                } else {
                    log.error("服务状态列表获取失败: {}", serviceStatusList.getMsg());
                }
            }).exceptionally(e -> {
                log.error("服务状态列表获取异常", e);
                return null;
            });
            
            // 等待异步任务完成（实际应用中可能不需要）
            future.join();
        } catch (Exception e) {
            log.error("异步调用异常", e);
        }
    }

    /**
     * 使用自定义配置创建客户端示例
     */
    private static void customConfigExample() {
        try {
            log.info("\n--- 使用自定义配置创建客户端示例 ---");
            
            // 使用Builder模式创建自定义配置
            BtConfig config = BtClientFactory.configBuilder()
                    .baseUrl(getEnvOrDefault("BT_PANEL_URL", "http://your-bt-panel-url:8888"))
                    .apiKey(getEnvOrDefault("BT_PANEL_API_KEY", "your-api-key"))
                    .connectTimeout(15) // 连接超时15秒
                    .readTimeout(45)   // 读取超时45秒
                    .retryCount(5)     // 重试5次
                    .build();
            
            // 使用自定义配置创建客户端
            BtClient customClient = BtClientFactory.createClient(config);
            
            // 使用自定义客户端创建API管理器
            BtApiManager customApiManager = new BtApiManager(customClient);
            
            // 使用自定义API管理器调用API
            SystemInfo systemInfo = customApiManager.system().getSystemInfo();
            
            log.info("使用自定义配置调用API成功: 服务器名称 = {}", systemInfo.getServerName());
            
            // 关闭自定义客户端
            customClient.close();
        } catch (Exception e) {
            log.error("自定义配置示例失败", e);
        }
    }

    /**
     * 日志拦截器，用于记录请求和响应信息
     */
    private static class LoggingInterceptor implements Interceptor {
        @Override
        public void intercept(RequestContext context) {
            // 请求前日志
            log.debug("请求API: {}, 方法: {}", context.getApi().getEndpoint(), context.getApi().getMethod());
            log.debug("请求参数: {}", context.getParams());
            
            // 继续请求处理
            context.proceed();
            
            // 响应后日志
            if (context.getException() == null) {
                log.debug("API请求成功: {}, 响应: {}", context.getApi().getEndpoint(), context.getResponse());
            } else {
                log.debug("API请求失败: {}, 异常: {}, 响应: {}", context.getApi().getEndpoint(),
                        context.getException().getMessage(),
                        context.getResponse());
            }
        }
    }

    /**
     * 重试拦截器，用于处理特定错误的重试逻辑
     */
    private static class RetryInterceptor implements Interceptor {
        private final int maxRetries;
        private final long retryIntervalMs;
        private int retryCount = 0;

        public RetryInterceptor(int maxRetries, long retryIntervalMs) {
            this.maxRetries = maxRetries;
            this.retryIntervalMs = retryIntervalMs;
        }

        @Override
        public void intercept(RequestContext context) {
            // 重置重试计数
            retryCount = 0;
            boolean shouldRetry;
            
            do {
                shouldRetry = false;
                context.setException(null);
                
                // 继续请求处理
                context.proceed();
                
                // 检查是否需要重试
                if (context.getException() instanceof BtApiException) {
                    BtApiException btEx = (BtApiException) context.getException();
                    // 对于服务器错误和网络错误进行重试
                    if ((btEx.isServerError() || btEx.isNetworkError()) && retryCount < maxRetries) {
                        shouldRetry = true;
                        retryCount++;
                        log.debug("重试请求, 尝试 {}/{}", retryCount, maxRetries);
                        try {
                            Thread.sleep(retryIntervalMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            } while (shouldRetry);
        }
    }
}