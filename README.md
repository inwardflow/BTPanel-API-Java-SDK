# BTPanel-API-Java-SDK

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![GitHub issues](https://img.shields.io/github/issues/inwardflow/BTPanel-API-Java-SDK.svg)](https://github.com/inwardflow/BTPanel-API-Java-SDK/issues)
[![GitHub stars](https://img.shields.io/github/stars/inwardflow/BTPanel-API-Java-SDK.svg)](https://github.com/inwardflow/BTPanel-API-Java-SDK/stargazers)

> 🚧**注意：这是一个正在开发中的项目，尚未发布到 Maven Central。**

BTPanel-API-Java-SDK 是一个功能完善的 Java 开发工具包，用于与宝塔 Linux 面板 API 进行交互，提供简单、优雅、安全的方式来管理和监控您的服务器。

## 功能特点

### 核心特性
- **支持管理多个宝塔面板实例**：可以同时连接并管理多个不同的宝塔面板
- **基于 Builder 模式的灵活配置**：通过流式接口配置客户端参数
- **同步和异步 API 调用支持**：支持 CompletableFuture 异步调用
- **自动重试机制**：配置化的重试策略，提高请求成功率
- **完善的错误处理**：详细的异常信息和状态码
- **优雅的 API 设计**：符合 Java 语言习惯的接口设计
- **轻量级依赖**：基于 Hutool 工具包，减少第三方依赖
- **类型安全**：提供数据模型类，支持类型安全的 API 调用
- **易于扩展**：模块化设计，便于添加新的 API 功能

### 支持的功能模块
- **系统信息**：获取服务器硬件信息、操作系统信息、宝塔面板版本等
- **服务管理**：查看和管理服务器上运行的各种服务状态
- **网站管理**：创建、查询、修改和删除网站
- **文件管理**：文件上传、下载、删除、目录操作等
- **数据库管理**：数据库创建、用户管理、权限配置
- **FTP管理**：FTP用户创建、权限设置
- **安全管理**：防火墙设置、SSH配置等

## 快速开始

### 安装依赖

> **注意：项目尚未发布到 Maven Central，当前只能通过源码构建使用。**

**本地构建安装**

```bash
# 克隆仓库
git clone https://github.com/inwardflow/BTPanel-API-Java-SDK.git
cd BTPanel-API-Java-SDK

# 构建并安装到本地 Maven 仓库
mvn clean install
```

然后在您的项目中添加依赖：

**Maven**
```xml
<dependency>
    <groupId>net.heimeng</groupId>
    <artifactId>BTPanel-API-Java-SDK</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Gradle**
```gradle
implementation 'net.heimeng:BTPanel-API-Java-SDK:1.0.0-SNAPSHOT'
```

### 2. 初始化 SDK 客户端

```java
import net.heimeng.sdk.btapi.core.BtClient;
import net.heimeng.sdk.btapi.core.BtClientFactory;
import net.heimeng.sdk.btapi.core.BtConfig;

// 替换为实际的宝塔面板信息
String baseUrl = "http://your-bt-panel-url:8888";
String apiKey = "your-api-key";
String apiToken = "your-api-token"; // 如果需要

// 方式1：快速创建客户端
BtClient client = BtClientFactory.createClient(baseUrl, apiKey, apiToken);

// 方式2：使用Builder模式创建自定义配置
BtConfig config = BtClientFactory.configBuilder()
        .baseUrl(baseUrl)
        .apiKey(apiKey)
        .apiToken(apiToken)
        .connectTimeout(15) // 连接超时15秒
        .readTimeout(45)   // 读取超时45秒
        .retryCount(3)     // 重试3次
        .build();

BtClient customClient = BtClientFactory.createClient(config);
```

### 3. 创建API管理器并调用API

```java
import net.heimeng.sdk.btapi.core.BtApiManager;
import net.heimeng.sdk.btapi.model.system.SystemInfo;
import net.heimeng.sdk.btapi.exception.BtApiException;

// 创建API管理器
BtApiManager apiManager = new BtApiManager(client);

try {
    // 同步调用API获取系统信息
    SystemInfo systemInfo = apiManager.system().getSystemInfo();
    
    // 处理结果
    if (systemInfo != null && systemInfo.isSuccess()) {
        System.out.println("系统信息获取成功");
        System.out.println("操作系统: " + systemInfo.getOsName());
        System.out.println("宝塔版本: " + systemInfo.getBtVersion());
        System.out.println("CPU使用率: " + systemInfo.getCpuUsage() + "%");
        System.out.println("内存使用率: " + systemInfo.getMemoryUsage() + "%");
    } else {
        System.err.println("系统信息获取失败: " + systemInfo.getMsg());
    }
} catch (BtApiException e) {
    System.err.println("API调用异常: " + e.getMessage());
}
```

## 高级功能

### 异步API调用

```java
import java.util.concurrent.CompletableFuture;
import net.heimeng.sdk.btapi.model.system.ServiceStatusList;

// 异步获取服务状态列表
CompletableFuture<ServiceStatusList> future = apiManager.system().getServiceStatusListAsync();

// 处理异步结果
future.thenAccept(serviceStatusList -> {
    if (serviceStatusList != null && serviceStatusList.isSuccess()) {
        System.out.println("服务状态列表获取成功");
        serviceStatusList.getServices().forEach(service -> {
            System.out.println("服务名称: " + service.getDisplayName() + ", 状态: " + service.getStatus());
        });
    } else {
        System.err.println("服务状态列表获取失败: " + serviceStatusList.getMsg());
    }
}).exceptionally(e -> {
    System.err.println("异步调用异常" + e.getMessage());
    return null;
});

// 等待异步任务完成（实际应用中可能不需要）
// future.join();
```

### 使用拦截器

```java
import net.heimeng.sdk.btapi.core.Interceptor;
import net.heimeng.sdk.btapi.core.RequestContext;

// 添加自定义日志拦截器
client.addInterceptor(new Interceptor() {
    @Override
    public void intercept(RequestContext context) {
        // 请求前日志
        System.out.println("请求API: " + context.getApi().getEndpoint());
        
        // 继续请求处理
        context.proceed();
        
        // 响应后日志
        if (context.getException() == null) {
            System.out.println("API请求成功");
        } else {
            System.out.println("API请求失败: " + context.getException().getMessage());
        }
    }
});

// 添加重试拦截器
client.addInterceptor(new RetryInterceptor(3, 1000));
```

### 完整的SDK示例

请参考 `src/main/java/net/heimeng/sdk/btapi/example/NewSdkExample.java` 文件获取更完整的使用示例，包括：
- 同步和异步API调用
- 自定义配置创建客户端
- 实现和使用拦截器
- 异常处理最佳实践

## 配置说明

`BtConfig` 支持以下配置项：

| 配置项 | 描述 | 默认值 |
|-------|------|-------|
| baseUrl | 宝塔面板的基础URL | 必填项 |
| apiKey | API密钥 | 必填项 |
| apiToken | API令牌（如果需要） | 可选 |
| connectTimeout | 连接超时时间（秒） | 10秒 |
| readTimeout | 读取超时时间（秒） | 30秒 |
| retryCount | 请求重试次数 | 3次 |
| retryInterval | 重试间隔时间（毫秒） | 1000毫秒 |
| retryableStatusCodes | 可重试的HTTP状态码 | [408, 429, 500, 502, 503, 504] |
| extraHeaders | 额外的HTTP请求头 | 空Map |
| sslVerify | 是否验证SSL证书 | true |

## 异常处理

SDK 使用 `BtApiException` 来表示 API 调用过程中的错误，包含以下信息：

- `message`: 错误消息
- `cause`: 原始异常
- `errorCode`: 错误代码
- `statusCode`: HTTP状态码
- `errorData`: 原始错误数据

异常分类判断方法：
- `isClientError()`: 判断是否为客户端错误（4xx状态码）
- `isServerError()`: 判断是否为服务器错误（5xx状态码）
- `isNetworkError()`: 判断是否为网络错误

## 注意事项

1. **安全警告**：请妥善保管您的 API 密钥和令牌，不要在公开场合泄露
2. **版本兼容性**：确保您的宝塔面板版本与 SDK 支持的 API 版本兼容
3. **请求频率**：避免频繁调用 API，以免触发宝塔面板的请求频率限制
4. **错误处理**：API 调用可能会失败，请确保实现适当的错误处理和重试机制
5. **HTTPS 配置**：建议使用 HTTPS 协议访问宝塔面板，确保数据传输安全
6. **资源释放**：完成操作后，记得调用 `close()` 方法关闭客户端，释放资源
7. **多线程支持**：SDK 支持多线程环境下使用，但请注意线程安全问题

## 安全使用指南

### 环境变量管理敏感信息

为了避免在代码中硬编码敏感信息（如API密钥和面板地址），SDK提供了通过环境变量加载配置的机制。推荐使用以下环境变量：

| 环境变量名 | 描述 | 示例值 |
|-----------|------|--------|
| BT_PANEL_URL | 宝塔面板的URL地址 | `http://your-bt-panel-url:8888` |
| BT_PANEL_API_KEY | API密钥 | `your-actual-api-key` |
| BT_PANEL_API_TOKEN | API令牌（如果需要） | `your-actual-api-token` |

### 在不同环境中设置环境变量

**Linux/MacOS**
```bash
# 临时设置环境变量（当前终端会话）
export BT_PANEL_URL="http://your-bt-panel-url:8888"
export BT_PANEL_API_KEY="your-actual-api-key"
export BT_PANEL_API_TOKEN="your-actual-api-token"

# 永久设置环境变量（将以下内容添加到 ~/.bashrc 或 ~/.zshrc）
echo 'export BT_PANEL_URL="http://your-bt-panel-url:8888"' >> ~/.bashrc
echo 'export BT_PANEL_API_KEY="your-actual-api-key"' >> ~/.bashrc
echo 'export BT_PANEL_API_TOKEN="your-actual-api-token"' >> ~/.bashrc
source ~/.bashrc
```

**Windows**
```powershell
# 临时设置环境变量（当前命令提示符会话）
set BT_PANEL_URL=http://your-bt-panel-url:8888
set BT_PANEL_API_KEY=your-actual-api-key
set BT_PANEL_API_TOKEN=your-actual-api-token

# 永久设置环境变量（需要管理员权限）
setx BT_PANEL_URL "http://your-bt-panel-url:8888" /M
setx BT_PANEL_API_KEY "your-actual-api-key" /M
setx BT_PANEL_API_TOKEN "your-actual-api-token" /M
```

### 使用配置文件

除了环境变量外，您也可以使用配置文件来管理敏感信息。例如，在项目中创建一个`application.properties`文件，并确保将其添加到`.gitignore`中：

**application.properties**
```properties
# 宝塔面板配置
bt.panel.url=http://your-bt-panel-url:8888
bt.panel.api.key=your-actual-api-key
bt.panel.api.token=your-actual-api-token
```

然后在代码中加载配置文件：

```java
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// 加载配置文件
Properties properties = new Properties();
try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
    if (input != null) {
        properties.load(input);
        String baseUrl = properties.getProperty("bt.panel.url", "http://your-bt-panel-url:8888");
        String apiKey = properties.getProperty("bt.panel.api.key", "your-api-key");
        String apiToken = properties.getProperty("bt.panel.api.token", "your-api-token");
        
        // 创建客户端
        BtClient client = BtClientFactory.createClient(baseUrl, apiKey, apiToken);
    }
} catch (IOException ex) {
    ex.printStackTrace();
}
```

### .gitignore 配置

请确保您的项目中包含适当的`.gitignore`文件，以防止敏感信息被提交到版本控制系统。以下是一个推荐的Java项目.gitignore文件示例：

```
# IDE 配置文件
.idea/
*.iml
.vscode/
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?

# 构建产物
target/
build/
*.class

# 依赖
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# 日志文件
*.log
logs/

# 环境变量和配置文件
.env
.env.local
.env.development.local
.env.test.local
.env.production.local
application.properties
application.yml

# 操作系统文件
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db

# 临时文件
*.tmp
*.temp
*.cache

# 测试覆盖率
.nyc_output/
coverage/

# 包管理
.npm
.yarn-integrity

# 其他
*.pid
*.seed
*.pid.lock
```

## 技术栈

- **Java 17+**
- **Hutool** - 工具包
- **Lombok** - 简化代码
- **SLF4J + Logback** - 日志框架
- **Jackson** - JSON处理
- **Maven** - 项目构建

## 开发指南

### 本地构建

```bash
# 克隆仓库
git clone https://github.com/inwardflow/BTPanel-API-Java-SDK.git
cd BTPanel-API-Java-SDK

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn package
```

### 代码规范

项目使用 Checkstyle、Spotless、P3C 插件确保代码质量和一致性：
- 遵循阿里巴巴 Java 开发规范
- 统一的导入顺序
- 自动移除未使用的导入
- 详细的 Javadoc 注释

## 贡献指南

我们非常欢迎社区贡献！如果您有任何问题或建议，请：

1. 提交 Issue 报告 bug 或提出新功能请求
2. 提交 Pull Request 改进代码
3. 分享您的使用经验和建议

在提交代码前，请确保：
- 代码符合项目的代码规范
- 添加了适当的测试用例
- 更新了相关文档

## 许可证

本项目采用 MIT 许可证 - 详情请查看 [LICENSE](LICENSE) 文件

## 联系方式

如有任何问题，请联系我们：

- GitHub Issues: [https://github.com/inwardflow/BTPanel-API-Java-SDK/issues](https://github.com/inwardflow/BTPanel-API-Java-SDK/issues)
- Email: admin@2wxk.com

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=inwardflow/BTPanel-API-Java-SDK&type=Date)](https://star-history.com/#inwardflow/BTPanel-API-Java-SDK&Date)

## Roadmap

### 近期目标 (v1.0.0 - 首次发布)
- 完善核心API功能实现
- 编写完整的单元测试和集成测试
- 完成API文档生成
- 优化错误处理机制
- 修复已知bug
- 发布到Maven Central

### 中期计划 (v1.1.0 - v1.5.0)
- 支持更多宝塔面板API功能
- 增强安全性和性能
- 添加缓存机制
- 支持WebSocket实时通知
- 增加更多示例代码

## 开发进度

### 已完成功能
- 基础客户端和配置系统
- 系统信息API
- 服务状态管理API
- 同步和异步调用支持
- 拦截器机制
- 基本的错误处理

### 待完成功能
- 网站管理API
- 文件管理API
- 数据库管理API
- FTP管理API
- 安全管理API
- 更多高级配置选项
- 完整的测试套件

## Todo List

- [ ] 完善网站管理功能
- [ ] 实现文件管理API
- [ ] 添加数据库管理功能
- [ ] 完成FTP管理API
- [ ] 实现安全管理功能
- [ ] 编写完整的单元测试
- [ ] 添加集成测试
- [ ] 优化文档和示例
- [ ] 修复所有已知bug
- [ ] 发布正式版本到Maven Central

## Milestones

| 版本 | 目标日期 | 主要功能 | 状态 |
|------|---------|---------|------|
|v1.0.0-alpha | 2025年12月 | 核心功能实现 | 进行中 |
|v1.0.0-beta | 2026年1月 | 功能完善和测试 | 计划中 |
|v1.0.0 | 2026年2月 | 正式发布 | 计划中 |
|v1.1.0 | 2026年4月 | 扩展功能 | 计划中 |

## Acknowledgements

感谢所有为这个项目做出贡献的开发者和用户！

特别鸣谢：
- [宝塔面板](https://www.bt.cn/) 提供的API接口
- [Hutool](https://hutool.cn/) 工具库
- 所有测试和反馈的用户