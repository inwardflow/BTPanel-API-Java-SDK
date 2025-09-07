# 宝塔面板 API Java SDK v2

本项目是宝塔面板API的Java SDK的v2版本，采用现代Java开发最佳实践进行重构，提供更简洁、更灵活、更强大的API访问功能。

## 架构设计

v2版本SDK采用了清晰的分层架构设计，主要包括以下几个核心模块：

1. **配置模块**：提供SDK的配置功能，支持灵活的配置选项
2. **异常模块**：提供统一的异常处理机制，便于错误处理和调试
3. **模型模块**：定义各种API响应的数据模型
4. **API模块**：定义和实现各种宝塔API接口
5. **客户端模块**：提供与宝塔面板通信的客户端功能
6. **拦截器模块**：提供请求和响应的拦截处理机制

## 核心功能

- **类型安全的API调用**：所有API调用都返回类型安全的结果
- **同步和异步API调用**：支持阻塞式同步调用和非阻塞式异步调用
- **灵活的配置选项**：支持自定义超时时间、重试策略等配置
- **统一的异常处理**：提供层次清晰的异常体系，便于错误处理
- **拦截器机制**：支持自定义请求拦截器，实现请求和响应的处理和转换
- **Builder模式**：使用Builder模式创建配置和API实例，提升代码可读性

## 快速开始

### 基本配置

使用Builder模式创建SDK配置：

```java
BtSdkConfig config = BtSdkConfig.builder()
        .baseUrl("http://your-panel-url:8888/")
        .apiKey("your-api-key")
        .connectTimeout(30000)
        .readTimeout(60000)
        .enableRetry(true)
        .maxRetries(3)
        .build();
```

### 创建客户端

使用BtClientFactory创建客户端实例：

```java
// 使用try-with-resources确保资源正确关闭
try (BtClient client = BtClientFactory.createClient(config)) {
    // 使用client调用API
}
```

也可以直接使用静态工厂方法快速创建客户端：

```java
try (BtClient client = BtClientFactory.createClient("http://your-panel-url:8888/", "your-api-key")) {
    // 使用client调用API
}
```

### 调用API

#### 直接使用BtClient调用（基础用法）

```java
// 创建获取系统信息的API实例
GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();

// 执行API调用
BtResult<SystemInfo> result = client.execute(systemInfoApi);

// 处理响应结果
if (result.isSuccess()) {
    SystemInfo systemInfo = result.getData();
    System.out.println("服务器主机名: " + systemInfo.getHostname());
    // 处理其他数据...
} else {
    System.err.println("获取系统信息失败: " + result.getMsg());
}
```

#### 使用BtApiManager调用（推荐用法）

```java
// 创建API管理器
BtApiManager apiManager = new BtApiManager(client);

// 创建获取网站列表的API实例（第1页，每页5条记录）
GetWebsitesApi websitesApi = new GetWebsitesApi(1, 5);

// 执行API调用
BtResult<List<WebsiteInfo>> result = apiManager.execute(websitesApi);

// 处理响应结果
if (result.isSuccess()) {
    List<WebsiteInfo> websites = result.getData();
    // 处理网站列表数据...
} else {
    System.err.println("获取网站列表失败: " + result.getMsg());
}
```

#### 异步API调用

```java
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
        // 处理系统信息...
    }
}).exceptionally(e -> {
    System.err.println("异步API调用异常: " + e.getMessage());
    return null;
});

// 或者等待异步操作完成（阻塞当前线程）
BtResult<SystemInfo> result = future.join();
```

## 异常处理

v2版本SDK提供了层次清晰的异常体系，便于进行错误处理：

- **BtSdkException**：所有SDK异常的基类，继承自RuntimeException
  - **BtApiException**：API调用异常，表示API返回了错误响应
    - **BtNetworkException**：网络异常，表示API请求过程中出现了网络问题
    - **BtAuthenticationException**：认证异常，表示API认证失败

在使用SDK时，可以根据不同类型的异常进行针对性处理：

```java
try {
    // API调用代码
} catch (BtAuthenticationException e) {
    // 处理认证失败
    System.err.println("API认证失败: " + e.getMessage());
} catch (BtNetworkException e) {
    // 处理网络问题
    System.err.println("网络错误: " + e.getMessage() + ", 主机: " + e.getHost());
} catch (BtApiException e) {
    // 处理API错误
    System.err.println("API错误: " + e.getMessage() + ", 状态码: " + e.getStatusCode());
} catch (BtSdkException e) {
    // 处理其他SDK错误
    System.err.println("SDK错误: " + e.getMessage());
} catch (Exception e) {
    // 处理其他未预期的错误
    System.err.println("未知错误: " + e.getMessage());
}
```

## 拦截器机制

v2版本SDK支持自定义请求拦截器，可以用于实现请求日志记录、请求修改、响应处理等功能：

```java
// 创建自定义拦截器
RequestInterceptor logInterceptor = (context, chain) -> {
    // 请求处理前
    System.out.println("请求URL: " + context.getRequestUrl());
    System.out.println("请求参数: " + context.getParams());
    
    try {
        // 执行下一个拦截器或实际的API请求
        chain.proceed(context);
        
        // 响应处理后
        System.out.println("响应状态码: " + context.getStatusCode());
        System.out.println("响应耗时: " + context.getExecutionTime() + "ms");
        
    } catch (Exception e) {
        // 异常处理
        System.err.println("请求异常: " + e.getMessage());
        throw e;
    }
};

// 创建客户端时添加拦截器
BtClient client = BtClientFactory.createClient(config)
        .addInterceptor(logInterceptor);
```

## API列表

目前v2版本SDK实现了以下常用API：

### 系统相关
- **GetSystemInfoApi**：获取系统信息

### 网站相关
- **GetWebsitesApi**：获取网站列表

## 从v1迁移到v2

v2版本SDK与v1版本相比有较大的变化，主要包括：

1. **包结构变化**：v2版本使用新的包结构，所有类都位于`net.heimeng.sdk.btapi.v2`包下
2. **API调用方式变化**：v2版本采用更现代的API调用方式，所有API都实现了BtApi接口
3. **异常处理变化**：v2版本提供了更完善的异常处理机制
4. **配置方式变化**：v2版本使用Builder模式创建配置

从v1迁移到v2，需要更新导入的包名，并按照v2的API调用方式修改代码。详细的迁移指南请参考示例代码。

## 示例代码

SDK提供了完整的示例代码，位于`net.heimeng.sdk.btapi.example.V2SdkExample`类中，展示了如何使用v2版本SDK的各种功能。

## 依赖

v2版本SDK主要依赖以下库：

- **Hutool**：Java工具包，用于JSON解析、HTTP请求等功能
- **SLF4J**：日志接口，用于日志记录
- **Lombok**：减少样板代码，如getter、setter等

## 许可证

本项目采用MIT许可证。

## 贡献

欢迎提交问题和代码，帮助改进SDK。在提交代码前，请确保代码风格与项目一致，并通过所有测试。