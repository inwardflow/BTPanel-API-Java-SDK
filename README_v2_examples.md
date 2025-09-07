# BTPanel-API-Java-SDK v2 版本使用示例

本文档提供了宝塔面板API Java SDK v2版本的详细使用示例，帮助开发者快速上手并掌握SDK的核心功能。

## 示例代码结构

SDK v2版本的示例代码位于 `net.heimeng.sdk.btapi.v2.example` 包中，主要包含以下文件：

- `V2SdkExample.java` - 主示例类，展示SDK的各种使用场景
- `package-info.java` - 包文档说明

## 快速开始

### 1. 基础使用示例

以下示例展示了如何初始化SDK并执行基本的API调用：

```java
// 创建客户端
BtClient client = BtClientFactory.createClient("http://localhost:8888", "your_api_key_here");

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
```

### 2. 自定义配置

SDK支持丰富的自定义配置选项，可以根据需要进行调整：

```java
// 创建自定义配置
BtSdkConfig config = BtSdkConfig.builder()
    .baseUrl("http://localhost:8888")
    .apiKey("your_api_key_here")
    .connectTimeout(5)             // 连接超时5秒
    .readTimeout(60)               // 读取超时60秒
    .retryCount(5)                 // 重试5次
    .retryInterval(Duration.ofSeconds(2)) // 重试间隔2秒
    .enableRequestLog(true)        // 启用请求日志
    .enableResponseLog(true)       // 启用响应日志
    .extraHeaders(Map.of("User-Agent", "BTPanel-Java-SDK")) // 自定义请求头
    .build();

// 使用自定义配置创建客户端
BtClient client = BtClientFactory.createClient(config);
```

### 3. 异步API调用

SDK支持异步API调用，可以提高应用程序的响应性能：

```java
// 创建客户端和API管理器
BtClient client = BtClientFactory.createClient(BASE_URL, API_KEY);
BtApiManager apiManager = new BtApiManager(client);

// 异步执行API
GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
CompletableFuture<BtResult<SystemInfo>> future = apiManager.executeAsync(systemInfoApi);

// 添加回调处理
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

// 等待异步操作完成（可选）
future.join();
```

### 4. 带超时的异步调用

对于需要控制响应时间的场景，可以使用带超时的异步调用：

```java
// 创建客户端和API管理器
BtClient client = BtClientFactory.createClient(BASE_URL, API_KEY);
BtApiManager apiManager = new BtApiManager(client);

// 带超时执行异步API（10秒超时）
GetWebsitesApi websitesApi = new GetWebsitesApi();
try {
    BtResult<List<WebsiteInfo>> result = 
        apiManager.executeAsyncWithTimeout(websitesApi, 10, TimeUnit.SECONDS);
    System.out.println("成功获取到" + result.getData().size() + "个网站");
} catch (BtApiException e) {
    if (e.getMessage().contains("timed out")) {
        System.out.println("API调用超时");
    } else {
        System.out.println("API调用失败: " + e.getMessage());
    }
}
```

### 5. 异常处理

SDK提供了完善的异常处理机制，可以捕获和处理各种错误情况：

```java
try {
    // 执行API
    GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
    BtResult<SystemInfo> result = apiManager.execute(systemInfoApi);
    // 处理结果...
} catch (BtApiException e) {
    // 处理API异常
    System.out.println("API异常: " + e.getMessage());
    System.out.println("错误代码: " + e.getErrorCode());
    if (e.getStatusCode() != null) {
        System.out.println("HTTP状态码: " + e.getStatusCode());
    }
} catch (BtNetworkException e) {
    // 处理网络异常
    System.out.println("网络异常: " + e.getMessage());
} catch (BtAuthenticationException e) {
    // 处理认证异常
    System.out.println("认证异常: " + e.getMessage());
} catch (Exception e) {
    // 处理其他异常
    System.out.println("未知异常: " + e.getMessage());
}
```

## 常用API示例

### 获取系统信息

```java
GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
BtResult<SystemInfo> result = apiManager.execute(systemInfoApi);

if (result.isSuccess()) {
    SystemInfo systemInfo = result.getData();
    System.out.println("主机名: " + systemInfo.getHostname());
    System.out.println("操作系统: " + systemInfo.getOs());
    System.out.println("CPU使用率: " + systemInfo.getCpuUsage() + "%");
    System.out.println("内存使用: " + formatBytes(systemInfo.getMemoryUsed()) + 
                       " / " + formatBytes(systemInfo.getMemoryTotal()));
    System.out.println("宝塔版本: " + systemInfo.getPanelVersion());
}
```

### 获取网站列表

```java
// 获取第1页，每页20条记录
GetWebsitesApi websitesApi = new GetWebsitesApi(1, 20);
BtResult<List<WebsiteInfo>> result = apiManager.execute(websitesApi);

if (result.isSuccess()) {
    List<WebsiteInfo> websites = result.getData();
    System.out.println("网站列表 (共" + websites.size() + "个):");
    
    for (WebsiteInfo website : websites) {
        System.out.println("- " + website.getDomain() + " (" + website.getType() + ")");
        System.out.println("  路径: " + website.getPath());
        System.out.println("  SSL: " + (website.getSsl() == 1 ? "已启用" : "未启用"));
    }
}
```

## 运行示例程序

1. 首先确保已在本地或远程服务器上安装了宝塔面板
2. 获取宝塔面板的API密钥（在面板设置中可以找到）
3. 修改示例代码中的`BASE_URL`和`API_KEY`为实际值
4. 编译并运行示例程序

```bash
# 编译项目
mvn clean compile

# 运行示例程序
java -cp target/classes net.heimeng.sdk.btapi.v2.example.V2SdkExample
```

## 注意事项

1. API密钥包含敏感信息，请妥善保管，不要硬编码在公开的代码中
2. 生产环境中建议使用环境变量或配置文件来管理API密钥和基础URL
3. 对于高并发场景，建议使用异步API调用以提高性能
4. 合理设置超时时间和重试策略，避免因网络问题导致应用程序无响应
5. 定期更新SDK版本以获取最新功能和安全修复

## 更多资源

- [完整API文档](https://github.com/inwardflow/BTPanel-API-Java-SDK/wiki)
- [GitHub仓库](https://github.com/inwardflow/BTPanel-API-Java-SDK)
- [问题反馈](https://github.com/inwardflow/BTPanel-API-Java-SDK/issues)