# BTPanel-API-Java-SDK API概览

本文档提供了BTPanel-API-Java-SDK项目中API类的组织结构和使用指南，帮助开发者快速了解和使用本SDK。

## 项目结构

SDK采用清晰的包结构组织代码，按照功能模块进行分类：

```
src/main/java/net/heimeng/sdk/btapi/
├── api/                 # API接口和实现类
│   ├── website/         # 网站管理相关API
│   ├── system/          # 系统管理相关API
│   ├── database/        # 数据库管理相关API
│   ├── file/            # 文件管理相关API
│   ├── ftp/             # FTP管理相关API
│   └── ssl/             # SSL证书相关API
├── client/              # API客户端实现
├── config/              # 配置类
├── exception/           # 异常类
├── model/               # 数据模型
└── interceptor/         # 请求拦截器
```

## API类设计

### 基础结构

所有API类都继承自`BaseBtApi<T>`抽象类，并实现了`BtApi<T>`接口。API类的基本结构如下：

```java
public class SomeApi extends BaseBtApi<BtResult<SomeType>> {
    // API端点路径
    private static final String ENDPOINT = "module?action=SomeAction";
    
    // 构造函数
    public SomeApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    // 参数设置方法（支持链式调用）
    public SomeApi setSomeParam(Type param) {
        addParam("param_name", param);
        return this;
    }
    
    // 参数验证
    @Override
    protected boolean validateParams() {
        // 验证逻辑
    }
    
    // 响应解析
    @Override
    public BtResult<SomeType> parseResponse(String response) {
        // 解析逻辑
    }
}
```

### 响应模型

所有API调用都返回`BtResult<T>`对象，包含以下字段：

- `status`: 布尔值，表示API调用是否成功
- `msg`: 字符串，包含API调用的消息或错误描述
- `data`: 泛型T，包含API调用返回的具体数据

## 主要API分类

### 网站管理API

网站管理API位于`net.heimeng.sdk.btapi.api.website`包下，提供网站的创建、删除、管理等功能。

#### 常用API类

| API类名 | 功能描述 | 端点 |
|---------|---------|------|
| `GetWebsiteListApi` | 获取网站列表 | `site?action=GetSiteList` |
| `CreateWebsiteApi` | 创建新网站 | `site?action=AddSite` |
| `DeleteWebsiteApi` | 删除网站 | `site?action=DeleteSite` |
| `GetWebsiteDetailApi` | 获取网站详细信息 | `site?action=GetSiteStatus` |
| `StartWebsiteApi` | 启动网站 | `site?action=StartSite` |
| `StopWebsiteApi` | 停止网站 | `site?action=StopSite` |
| `GetWebsitePhpVersionApi` | 获取网站PHP版本 | `site?action=getPhpVersion` |
| `SetWebsitePhpVersionApi` | 设置网站PHP版本 | `site?action=SetPhpVersion` |
| `GetWebsiteRewriteRulesApi` | 获取网站伪静态规则 | `site?action=getRewrite` |
| `SetWebsiteRewriteRulesApi` | 设置网站伪静态规则 | `site?action=setRewrite` |

### 系统管理API

系统管理API位于`net.heimeng.sdk.btapi.api.system`包下，提供系统状态查询等功能。

#### 常用API类

| API类名 | 功能描述 | 端点 |
|---------|---------|------|
| `GetSystemInfoApi` | 获取系统信息 | `/system?action=GetSystemTotal` |
| `GetDiskInfoApi` | 获取磁盘分区信息 | `/system?action=GetDiskInfo` |
| `GetNetworkStatusApi` | 获取网络状态 | `/system?action=GetNetWork` |
| `CheckPanelUpdateApi` | 检查面板更新 | `/ajax?action=UpdatePanel` |

### 数据库管理API

数据库管理API位于`net.heimeng.sdk.btapi.api.database`包下，提供数据库的创建、删除等功能。

#### 常用API类

| API类名 | 功能描述 | 端点 |
|---------|---------|------|
| `GetDatabasesApi` | 获取数据库列表 | `database?action=GetDatabases` |
| `CreateDatabaseApi` | 创建数据库 | `database?action=AddDatabase` |
| `DeleteDatabaseApi` | 删除数据库 | `database?action=DeleteDatabase` |
| `ChangeDatabasePasswordApi` | 修改数据库密码 | `database?action=ChangePassword` |

### 文件管理API

文件管理API位于`net.heimeng.sdk.btapi.api.file`包下，提供文件和目录的操作功能。

#### 常用API类

| API类名 | 功能描述 | 端点 |
|---------|---------|------|
| `GetFileContentApi` | 获取文件内容 | `files?action=GetFileBody` |
| `SaveFileContentApi` | 保存文件内容 | `files?action=SaveFileBody` |
| `CreateFileDirectoryApi` | 创建目录 | `files?action=AddFolder` |
| `DeleteFileApi` | 删除文件/目录 | `files?action=DeleteFile` |
| `RenameFileApi` | 重命名文件/目录 | `files?action=RenameFile` |
| `MoveFileApi` | 移动/复制文件 | `files?action=MoveFile` |
| `CompressFileApi` | 压缩文件/目录 | `files?action=Compress` |
| `UncompressFileApi` | 解压文件 | `files?action=UnCompress` |

## API调用流程

### 基本步骤

1. 创建`BtSdkConfig`配置对象
2. 使用`BtClientFactory`创建`BtClient`实例
3. 创建`BtApiManager`实例
4. 创建具体的API类实例并设置参数
5. 使用`BtApiManager.execute()`方法执行API调用
6. 处理返回的`BtResult<T>`对象

### 示例代码

```java
// 创建配置
BtSdkConfig config = BtSdkConfig.builder()
        .baseUrl("http://localhost:8888")
        .apiKey("your_api_key")
        .connectTimeout(5)
        .readTimeout(10)
        .build();

// 创建客户端和API管理器
BtClient client = BtClientFactory.createClient(config);
BtApiManager apiManager = new BtApiManager(client);

// 创建并配置API
GetWebsiteListApi websiteListApi = new GetWebsiteListApi()
        .setPage(1)
        .setLimit(10);

// 执行API调用
BtResult<List<Map<String, Object>>> result = apiManager.execute(websiteListApi);

// 处理结果
if (result.isSuccess()) {
    List<Map<String, Object>> websites = result.getData();
    // 处理网站列表数据
} else {
    // 处理失败情况
    System.out.println("API调用失败: " + result.getMsg());
}

// 关闭资源
apiManager.close();
```

## 异常处理

SDK定义了多种异常类来表示不同类型的错误：

- `BtApiException`: 所有API相关异常的基类
- `BtAuthenticationException`: 认证失败异常
- `BtNetworkException`: 网络相关异常
- `BtSdkException`: SDK配置或内部错误异常

在调用API时，应当捕获并适当处理这些异常。

## 测试指南

SDK包含单元测试和集成测试：

- 单元测试位于`src/test/java`目录下对应的包中
- 集成测试位于`src/test/java/net/heimeng/sdk/btapi/integration`包中
- 集成测试默认被禁用，避免不必要的API调用

要运行集成测试，需要：
1. 配置正确的宝塔面板URL和API密钥
2. 移除测试类上的`@Disabled`注解
3. 运行测试

## 版本兼容性

- 支持宝塔面板6.x及以上版本
- 使用Java 8及以上版本开发
- 依赖Hutool工具库处理JSON和HTTP请求

## 贡献指南

1. Fork本仓库
2. 创建特性分支
3. 提交代码变更
4. 推送到远程分支
5. 创建Pull Request

## 许可证

本项目采用MIT许可证，详情请查看LICENSE文件。