# BTPanel API Java SDK

[![CI](https://github.com/inwardflow/BTPanel-API-Java-SDK/actions/workflows/ci.yml/badge.svg)](https://github.com/inwardflow/BTPanel-API-Java-SDK/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[English](README.md) | 简体中文

`BTPanel API Java SDK` 是宝塔 Linux 面板 API 的 Java 17 客户端库。它既提供灵活的底层 `BtApi<T>` 接口模型，也提供面向常见自动化场景的高层门面（facade），覆盖网站管理、数据库、文件、FTP、SSL 和系统信息查询。

## 特性

- 基于 Java 17 `HttpClient` 实现。
- 不可变的 `BtSdkConfig`，构建时校验超时、重试和 SSL 配置。
- 通过 `RetryMode` 显式控制重试：`NONE`、`SAFE_REQUESTS_ONLY`、`ALL_REQUESTS`。
- 多种证书信任方式：系统信任库、自定义信任库、面板公钥固定（适用于宝塔默认的自签名证书），以及仅限测试环境使用的“信任全部”。
- 以 `BtApiManager` 作为稳定的高层入口。
- 按模块划分的门面：`system()`、`website()`、`database()`、`file()`、`ftp()`、`ssl()`。
- 可扩展的底层接口模型，便于接入 SDK 尚未封装或新发现的面板接口。
- 基于 Maven 的质量门禁：代码格式、风格检查、单元测试、独立的集成测试和覆盖率报告。

## 安装

需要 Java 17 或更高版本。SDK 目前尚未发布到 Maven Central，可任选以下方式：

- 从 [GitHub Releases](https://github.com/inwardflow/BTPanel-API-Java-SDK/releases) 下载 jar（以及 `-sources`、`-javadoc` 包）。
- 或者从发布 tag 构建并安装到本地 Maven 仓库：

  ```bash
  git clone --branch v0.1.0 https://github.com/inwardflow/BTPanel-API-Java-SDK.git
  cd BTPanel-API-Java-SDK
  ./mvnw install -DskipTests
  ```

  然后声明依赖：

  ```xml
  <dependency>
    <groupId>net.heimeng</groupId>
    <artifactId>btpanel-api-java-sdk</artifactId>
    <version>0.1.0</version>
  </dependency>
  ```

SDK 通过 SLF4J 输出日志，但不自带日志实现。如需查看 SDK 日志，请在应用中自行引入一个实现（例如 Logback）。

## 快速开始

### 构建

Linux 或 macOS：

```bash
./mvnw verify
```

Windows PowerShell：

```powershell
.\mvnw.cmd verify
```

### 创建客户端

```java
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.client.BtClientFactory;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.SystemInfo;

BtSdkConfig config = BtClientFactory.configBuilder()
    .baseUrl("http://your-bt-panel-url:8888")
    .apiKey("your-api-key")
    .connectTimeout(10)
    .readTimeout(30)
    .retryMode(BtSdkConfig.RetryMode.SAFE_REQUESTS_ONLY)
    .retryCount(3)
    .build();

try (BtApiManager apiManager = BtClientFactory.createApiManager(config)) {
  BtResult<SystemInfo> result = apiManager.system().getSystemInfo();
  System.out.println(result.getData().getOs());
}
```

`connectTimeout(int)` 和 `readTimeout(int)` 的单位是秒，也可以直接传入 `Duration`。

### 常见用法

```java
import net.heimeng.sdk.btapi.facade.DatabaseCreateRequest;
import net.heimeng.sdk.btapi.facade.FtpCreateRequest;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;
import net.heimeng.sdk.btapi.model.website.CreateWebsiteResult;
import net.heimeng.sdk.btapi.model.website.WebsiteInfo;
import net.heimeng.sdk.btapi.facade.WebsiteCreateRequest;

BtResult<java.util.List<WebsiteInfo>> websites = apiManager.website().list(1, 20);
BtResult<Integer> taskCount = apiManager.system().getTaskCount();
BtResult<String> nginxConfig = apiManager.website().getNginxConfig(1, "example.com");
BtResult<java.util.List<SslCertificate>> certificates = apiManager.ssl().list();
BtResult<Boolean> createdDatabase =
    apiManager.database().create(DatabaseCreateRequest.builder("demo_db", "demo_user", "secret").build());
BtResult<Boolean> createdFtp =
    apiManager.ftp().create(FtpCreateRequest.of("demo_ftp", "secret", "/www/wwwroot/demo"));

BtResult<CreateWebsiteResult> createdWebsite =
    apiManager.website()
        .create(
            WebsiteCreateRequest.builder("demo.example.com", "/www/wwwroot/demo")
                .phpVersion("82")
                .remark("Demo website")
                .build());
```

## 网站接口的设计说明

宝塔网站模块的接口会返回多种互不兼容的响应结构。为了让各个接口实现保持简洁一致，SDK 使用了一组共享的解析抽象：

- `AbstractWebsiteBooleanApi`
- `AbstractWebsiteTextQueryApi`
- `AbstractWebsiteMapQueryApi`
- `AbstractWebsiteMapListQueryApi`
- `WebsiteApiResponseSupport`

具体的网站接口实现只需关注：

- 接口路径；
- 参数校验；
- 载荷字段命名；
- 以及该接口特有的成功或失败提示。

## 门面方法命名约定

项目正朝稳定的 `1.0` 公共 API 演进。网站模块的读取类操作统一使用面向集合的命名：

- `listRaw(...)`
- `listTypes()`
- `listPhpVersions()`
- `listDomains(int siteId)`
- `listPhpExtensions(int siteId)`
- `listSslCertificates(int siteId)`
- `listBackups(Integer siteId, Integer page, Integer limit, String callback)`

写入类操作统一使用动作导向的动词，例如：

- `removeDomain(...)`
- `updateRemark(...)`
- `updateRootPath(...)`
- `updateRunPath(...)`
- `toggleUserIni(...)`
- `updatePhpVersion(...)`
- `updatePhpExtension(...)`
- `updateRewriteRules(...)`
- `updateNginxConfig(...)`
- `enablePasswordProtection(...)`
- `disablePasswordProtection(...)`
- `installSslCertificate(...)`
- `disableSsl(...)`
- `toggleLogs(...)`
- `updateLimitNet(...)`

参数较多的操作优先使用类型化的请求对象，而不是长参数列表：

- `create(WebsiteCreateRequest request)`
- `database().create(DatabaseCreateRequest request)`
- `database().delete(DatabaseDeleteRequest request)`
- `database().updatePassword(DatabasePasswordUpdateRequest request)`
- `ftp().create(FtpCreateRequest request)`
- `ftp().delete(FtpDeleteRequest request)`
- `ftp().updatePassword(FtpPasswordUpdateRequest request)`
- `delete(int siteId, String websiteName, WebsiteDeleteOptions options)`
- `addDomain(int siteId, WebsiteDomainBinding binding)`
- `removeDomain(int siteId, WebsiteDomainRemoval removal)`
- `enablePasswordProtection(int siteId, WebsitePasswordProtectionOptions options)`
- `updateRewriteRules(int siteId, WebsiteRewriteRulesOptions options)`
- `updateNginxConfig(int siteId, WebsiteNginxConfigOptions options)`
- `installSslCertificate(int siteId, WebsiteSslCertificateOptions options)`
- `updateLimitNet(int siteId, WebsiteLimitNetOptions options)`

`WebsiteCreateRequest` 使用 Builder 构建，可选的附带资源（FTP、数据库）需要显式声明：

```java
WebsiteCreateRequest request =
    WebsiteCreateRequest.builder("demo.example.com", "/www/wwwroot/demo")
        .typeId(0)
        .phpVersion("82")
        .port(80)
        .remark("Demo website")
        .ftpAccount("demo_ftp", "ftp-secret")
        .database("demo_db", "db-secret", "utf8mb4")
        .build();

apiManager.website().create(request);
```

数据库和 FTP 的写入操作采用同样的类型化请求方式：

```java
DatabaseCreateRequest databaseRequest =
    DatabaseCreateRequest.builder("demo_db", "demo_user", "secret")
        .remark("Demo database")
        .build();

apiManager.database().create(databaseRequest);
apiManager.database().updatePassword(
    new DatabasePasswordUpdateRequest("demo_db", "demo_user", "new-secret"));

FtpCreateRequest ftpRequest =
    new FtpCreateRequest("demo_ftp", "secret", "/www/wwwroot/demo", "Demo FTP");

apiManager.ftp().create(ftpRequest);
apiManager.ftp().updatePassword(
    new FtpPasswordUpdateRequest(12, "demo_ftp", "/www/wwwroot/demo", "new-secret"));
apiManager.ftp().delete(new FtpDeleteRequest(12, "demo_ftp"));
```

## 通过 HTTPS 连接面板

宝塔面板默认使用自签名证书，默认的 `SYSTEM_TRUST` 模式会拒绝连接，并给出明确的错误提示。请按推荐顺序选择以下方式之一：

1. **为面板配置受信任的证书**（面板“设置 → 安全设置 → 面板 SSL”中可以申请受信任的 IP 证书，详见[官方教程](https://docs.bt.cn/user-guide/ai/mcp-installation)）。配置完成后，SDK 使用默认配置即可连接。
2. **固定面板的公钥**。这是继续使用自签名证书时的安全做法：

   ```java
   BtSdkConfig config =
       BtSdkConfig.builder()
           .baseUrl("https://your-panel-host:8888")
           .apiKey(System.getenv("BT_PANEL_API_KEY"))
           .pinnedPublicKeys("sha256/<base64-of-spki-sha256>")
           .build();
   ```

   请在面板服务器本机（或其他你信任的渠道）上获取指纹：

   ```bash
   openssl s_client -connect 127.0.0.1:8888 </dev/null 2>/dev/null | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
   ```

   指纹不匹配时，错误信息会显示服务端实际出示的公钥指纹。只要密钥对不变，证书续期后指纹也不变。需要轮换密钥时，可以同时配置新旧两个指纹。
3. **使用自定义信任库**：如果你自己管理 CA，可以使用 `trustStore(path, password[, type])`。

`INSECURE_TRUST_ALL` 会关闭证书校验，并在启动时输出警告日志。只应在隔离的测试环境中使用。

证书错误和无法解析的主机永远不会重试。其他失败按 `RetryMode` 重试，采用带随机抖动的指数退避，最长等待时间由 `maxRetryInterval` 限制；服务端返回的 `Retry-After` 头也会被遵守。

## SSL 说明

- `install(domain, key, cert)` 中的 `domain` 对应面板接口里的站点名参数。面板安装证书时也会把它保存到证书夹，之后可以部署到其他站点。
- `SslCertificate.domains` 来自证书的 `CN` 和 `SAN`，因此可能与安装时使用的站点名不同。

## 删除文件和目录

- `file().delete(path)` 删除文件，`file().deleteDirectory(path)` 删除目录及其内容。面板开启回收站时，二者都会移入回收站。
- 路径必须是绝对路径。包含 `..` 的路径，以及 `/`、`/etc`、`/www/wwwroot` 等系统或面板目录本身，会在发出请求前被拒绝。
- API 密钥拥有面板的全部权限，面板无法限制它的操作范围。如果路径来自终端用户，请先用 `RemotePaths.isWithin(授权目录, 路径)` 进行校验。

## 接口依据

SDK 以面板实际发出的请求为准。面板前端调用的就是同一套 API，因此浏览器开发者工具里抓到的网络请求是最终依据。[官方 API 文档](https://docs.bt.cn/api/)可作为参考，但并不完整。SDK 中的接口均通过可选的集成测试，在真实的 BTPanel 9.0.0 上验证过。

## 构建与测试

- 完整校验：`./mvnw verify`
- 仅运行单元测试：`./mvnw test`
- 包含集成测试：`./mvnw verify -Pintegration-tests`

集成测试通过环境变量或 `src/test/resources/application-test.properties` 配置。请以 `src/test/resources/application-test.properties.example` 为模板，不要提交真实的凭据。

`docs/openapi` 下的在线校验输出属于本地工作区产物，除非经过整理、有意纳入正式文档，否则不应提交。

## 项目结构

```text
src/main/java/net/heimeng/sdk/btapi
|- api
|- client
|- config
|- exception
|- facade
|- interceptor
`- model
```

## 文档

- [架构说明](docs/architecture.md)
- [测试指南](docs/testing.md)
- [快速上手示例](docs/examples/quickstart.md)
- [宝塔 API 开发者指南](docs/btpanel-api-developer-guide.md)
- [OpenAPI 工作区说明](docs/openapi/README.md)
- [发布清单](docs/release-checklist.md)
- [贡献指南](CONTRIBUTING.md)
- [安全策略](SECURITY.md)
- [更新日志](CHANGELOG.md)

部分文档目前只有英文版。

## 版本策略

项目仍处于 `0.x` 阶段，在发布稳定的 `1.0` 之前，公共 API 仍可能调整。

## 许可证

[MIT](LICENSE)
