# BTPanel-API-Java-SDK 重新设计方案

## 1. 设计目标

重新设计宝塔面板API Java SDK，使其更符合现代Java开发最佳实践，提供更灵活、易用、可扩展的接口，同时保持向后兼容性。

## 2. 当前架构分析

### 2.1 优点
- 已采用Builder模式（BtSdkConfig）
- 已采用工厂模式（BTApiFactory）
- 提供了异常处理机制
- 使用Lombok减少样板代码
- 有基本的测试覆盖

### 2.2 局限性
- 强制单例模式限制了多面板管理能力
- API分类不清晰，所有API混合在一个类中
- 缺乏异步API支持
- 缺少请求/响应拦截机制
- 重试机制实现简单
- 缺少完整的文档和使用示例

## 3. 新架构设计

### 3.1 核心原则
- **接口与实现分离**：定义清晰的接口，便于扩展和测试
- **模块化设计**：按功能领域划分API
- **多实例支持**：移除强制单例限制
- **异步支持**：提供同步和异步API
- **事件驱动**：添加请求/响应拦截器
- **灵活的认证**：支持多种认证方式
- **统一的错误处理**：提供更详细的错误分类

### 3.2 架构图

```
┌───────────────────────────────────────────────────────────────────────────┐
│                              BTPanel SDK                                  │
├─────────────┬───────────────┬───────────────┬─────────────────────────────┤
│  接口层     │   核心层      │  实现层       │    工具层                    │
├─────────────┼───────────────┼───────────────┼─────────────────────────────┤
│ SiteApi     │ BtClient      │ DefaultBtClient│ HttpUtils                  │
│ FileApi     │ BtConfig      │ ApiExecutor   │ JsonUtils                  │
│ FtpApi      │ ApiCallback   │ RetryStrategy │ SecurityUtils              │
│ SystemApi   │ Interceptor   │                RetryInterceptor            │
│ UserApi     │               │                LoggingInterceptor          │
└─────────────┴───────────────┴───────────────┴─────────────────────────────┘
```

### 3.3 主要组件

#### 3.3.1 客户端（Client）
- `BtClient`：核心接口，定义客户端的基本操作
- `DefaultBtClient`：默认实现，负责管理连接和执行API请求
- `BtClientFactory`：客户端工厂，用于创建和管理BtClient实例

#### 3.3.2 API接口层
按功能领域划分为多个API接口：
- `SiteApi`：站点管理相关API
- `FileApi`：文件管理相关API
- `FtpApi`：FTP管理相关API
- `SystemApi`：系统管理相关API
- `SecurityApi`：安全管理相关API

#### 3.3.3 配置（Configuration）
- `BtConfig`：客户端配置类，包含连接参数、认证信息等
- `BtConfigBuilder`：配置构建器

#### 3.3.4 拦截器（Interceptor）
- `RequestInterceptor`：请求拦截器，在发送请求前执行
- `ResponseInterceptor`：响应拦截器，在接收响应后执行
- `LoggingInterceptor`：日志拦截器，记录请求和响应
- `RetryInterceptor`：重试拦截器，处理失败重试逻辑

#### 3.3.5 回调（Callback）
- `ApiCallback<T>`：异步API回调接口
- `CompletableFuture<T>`：Java标准的异步处理机制

#### 3.3.6 模型（Model）
- 数据模型类，用于表示API请求和响应的数据结构
- 采用Builder模式构建复杂对象

## 4. 具体实现方案

### 4.1 核心接口定义

#### 4.1.1 BtClient接口
```java
public interface BtClient {
    // 同步API调用方法
    <T> T execute(BtApi<T> api);
    
    // 异步API调用方法
    <T> CompletableFuture<T> executeAsync(BtApi<T> api);
    
    // 添加拦截器
    BtClient addInterceptor(Interceptor interceptor);
    
    // 获取配置
    BtConfig getConfig();
    
    // 关闭客户端
    void close();
}
```

#### 4.1.2 BtApi接口
```java
public interface BtApi<T> {
    String getEndpoint();
    HttpMethod getMethod();
    Map<String, Object> getParams();
    T parseResponse(String response) throws BtApiException;
}
```

#### 4.1.3 API模块设计
每个功能模块提供独立的接口和实现类：
```java
public interface SiteApi {
    List<Site> getSites();
    Site createSite(SiteCreateRequest request);
    boolean deleteSite(int siteId);
    // 其他站点相关API...
}

public class DefaultSiteApi implements SiteApi {
    private final BtClient client;
    
    public DefaultSiteApi(BtClient client) {
        this.client = client;
    }
    
    @Override
    public List<Site> getSites() {
        return client.execute(new GetSitesApi());
    }
    
    // 实现其他方法...
}
```

### 4.2 客户端实现

#### 4.2.1 DefaultBtClient实现
```java
public class DefaultBtClient implements BtClient {
    private final BtConfig config;
    private final List<Interceptor> interceptors = new ArrayList<>();
    private final HttpClient httpClient;
    
    // 构造函数、方法实现...
    
    @Override
    public <T> T execute(BtApi<T> api) {
        // 执行请求并返回结果
        // 1. 应用拦截器
        // 2. 构建请求
        // 3. 发送请求
        // 4. 解析响应
        // 5. 返回结果
    }
}
```

#### 4.2.2 BtClientFactory工厂类
```java
public class BtClientFactory {
    private static final Map<String, BtClient> CLIENTS = new ConcurrentHashMap<>();
    private static volatile BtClient defaultClient;
    
    // 创建客户端
    public static BtClient createClient(BtConfig config) {
        // 创建并返回新的客户端实例
    }
    
    // 获取默认客户端
    public static BtClient getDefaultClient() {
        // 返回默认客户端
    }
    
    // 设置默认客户端
    public static void setDefaultClient(BtClient client) {
        // 设置默认客户端
    }
    
    // 根据ID获取客户端
    public static BtClient getClient(String id) {
        // 根据ID返回客户端
    }
}
```

### 4.3 配置管理

#### 4.3.1 BtConfig类
```java
public class BtConfig {
    private String baseUrl;
    private String apiKey;
    private String apiToken;
    private int connectTimeout = 10_000;
    private int readTimeout = 30_000;
    private int writeTimeout = 30_000;
    private int retryCount = 0;
    private long retryInterval = 1_000;
    private boolean enableSslVerify = true;
    private boolean enableLogging = true;
    
    // 私有构造函数，通过Builder创建实例
    private BtConfig() {
    }
    
    // Builder内部类
    public static class Builder {
        private BtConfig config = new BtConfig();
        
        // 构建方法
        public BtConfig build() {
            validate();
            return config;
        }
        
        // 参数设置方法...
    }
}
```

### 4.4 拦截器实现

#### 4.4.1 基础拦截器接口
```java
public interface Interceptor {
    void intercept(RequestContext context);
}
```

#### 4.4.2 日志拦截器
```java
public class LoggingInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public void intercept(RequestContext context) {
        // 记录请求信息
        if (logger.isDebugEnabled()) {
            logger.debug("API Request: {} {}", context.getMethod(), context.getEndpoint());
            logger.debug("Request Params: {}", context.getParams());
        }
        
        // 执行请求
        context.proceed();
        
        // 记录响应信息
        if (logger.isDebugEnabled()) {
            logger.debug("API Response: Status={}, Time={}ms", 
                        context.getResponseStatus(), 
                        context.getExecutionTime());
        }
    }
}
```

#### 4.4.3 重试拦截器
```java
public class RetryInterceptor implements Interceptor {
    @Override
    public void intercept(RequestContext context) {
        BtConfig config = context.getConfig();
        int maxRetries = config.getRetryCount();
        long retryInterval = config.getRetryInterval();
        
        int attempts = 0;
        while (true) {
            try {
                // 执行请求
                context.proceed();
                break; // 成功，退出循环
            } catch (Exception e) {
                attempts++;
                if (attempts > maxRetries || !isRetryable(e)) {
                    throw e; // 超过重试次数或不可重试的异常，抛出
                }
                
                // 等待重试间隔
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BtApiException("Retry interrupted", ie);
                }
            }
        }
    }
    
    private boolean isRetryable(Exception e) {
        // 判断异常是否可重试
        // 例如网络超时异常可重试，参数错误异常不可重试
    }
}
```

### 4.5 异步支持

```java
public class DefaultBtClient implements BtClient {
    // 线程池用于异步执行
    private final ExecutorService executorService;
    
    // 构造函数初始化线程池
    public DefaultBtClient(BtConfig config) {
        this.config = config;
        // 初始化线程池
        this.executorService = Executors.newFixedThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors())
        );
        // 初始化其他组件
    }
    
    @Override
    public <T> CompletableFuture<T> executeAsync(BtApi<T> api) {
        return CompletableFuture.supplyAsync(() -> execute(api), executorService);
    }
    
    // 关闭方法释放资源
    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

### 4.6 向后兼容性支持

为了保持向后兼容性，我们可以保留原有的BtSdk类，并让其内部使用新的架构：

```java
@Slf4j
public class BTSDK {
    // 使用新的客户端架构
    private static volatile BtClient internalClient;
    private static final Object lock = new Object();
    
    // 原有方法保留，内部使用新的客户端实现
    public static BTSDK initSDK(String baseUrl, String apiKey, String apiToken) {
        // 内部创建BtClient并初始化
        BtConfig config = new BtConfig.Builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .apiToken(apiToken)
            .build();
        
        internalClient = BtClientFactory.createClient(config);
        return new BTSDK();
    }
    
    // 原有API方法保留，内部调用新的API接口
    public String getSystemTotal() {
        SystemApi systemApi = new DefaultSystemApi(internalClient);
        return systemApi.getSystemTotal();
    }
    
    // 其他原有方法...
}
```

## 5. 改进的错误处理

### 5.1 异常分类

```java
public class BtApiException extends RuntimeException {
    private final String errorCode;
    private final Object errorData;
    private final HttpStatus httpStatus;
    
    // 构造函数...
    
    // 工厂方法创建特定类型的异常
    public static BtApiException authenticationError(String message) {
        return new BtApiException(message, null, "AUTH_ERROR");
    }
    
    public static BtApiException networkError(String message, Throwable cause) {
        return new BtApiException(message, cause, "NETWORK_ERROR");
    }
    
    // 其他类型的异常工厂方法...
}
```

### 5.2 错误恢复策略

```java
public class ErrorRecoveryStrategy {
    public static boolean canRetry(BtApiException e) {
        // 根据错误类型决定是否可以重试
        return "NETWORK_ERROR".equals(e.getErrorCode()) || 
               "SERVER_ERROR".equals(e.getErrorCode());
    }
    
    public static long getRetryDelay(BtApiException e, int attempt) {
        // 根据错误类型和重试次数计算延迟时间
        // 可以使用指数退避算法
        return Math.min(1000 * (long) Math.pow(2, attempt), 30000);
    }
}
```

## 6. 测试策略

### 6.1 单元测试
- 为每个组件编写单元测试
- 使用Mockito模拟外部依赖
- 确保测试覆盖率达到80%以上

### 6.2 集成测试
- 测试组件之间的交互
- 使用测试服务器验证HTTP请求和响应

### 6.3 性能测试
- 测试在高并发场景下的性能表现
- 测量响应时间和吞吐量

## 7. 文档和示例

### 7.1 Javadoc文档
- 为所有公共接口和类添加详细的Javadoc
- 生成API文档网站

### 7.2 使用示例
- 提供详细的使用示例代码
- 为每个功能模块提供单独的示例
- 提供多面板管理的示例

### 7.3 最佳实践指南
- 提供SDK使用的最佳实践
- 安全使用指南
- 性能优化建议

## 8. 实现路线图

1. **阶段一**：设计和实现核心接口（BtClient、BtApi等）
2. **阶段二**：实现基础组件（配置、拦截器、异常等）
3. **阶段三**：实现API模块（站点、文件、FTP等）
4. **阶段四**：实现向后兼容性层
5. **阶段五**：编写测试和文档
6. **阶段六**：性能优化和安全审计

通过以上重新设计，BTPanel-API-Java-SDK将变得更加灵活、易用和可扩展，同时保持向后兼容性，满足不同开发者的需求。