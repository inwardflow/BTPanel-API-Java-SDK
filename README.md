# BTPanel API Java SDK

[![CI](https://github.com/inwardflow/BTPanel-API-Java-SDK/actions/workflows/ci.yml/badge.svg)](https://github.com/inwardflow/BTPanel-API-Java-SDK/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

English | [简体中文](README.zh-CN.md)

`BTPanel API Java SDK` is a Java 17 client library for the BT Panel API. It combines a flexible low-level `BtApi<T>` model with higher-level facades for common automation tasks such as website management, database operations, file handling, FTP, SSL, and system inspection.

## Features

- Java 17 `HttpClient` implementation.
- Immutable `BtSdkConfig` with validation for timeouts, retries, and SSL mode.
- Explicit retry strategy via `RetryMode`: `NONE`, `SAFE_REQUESTS_ONLY`, and `ALL_REQUESTS`.
- Multiple certificate trust modes: system trust, custom trust store, public-key pinning (for BT Panel's default self-signed certificate), and trust-all for isolated test environments only.
- Stable high-level entry point through `BtApiManager`.
- Facade-based access for common modules: `system()`, `website()`, `database()`, `file()`, `ftp()`, and `ssl()`.
- Extensible low-level endpoint model for unsupported or newly discovered panel APIs.
- Maven-based quality gates with formatting, style checks, unit tests, integration-test separation, and coverage reporting.

## Installation

Requires Java 17 or later. The SDK is not published to Maven Central yet. Use one of these options:

- Download the jar (plus `-sources` and `-javadoc` jars) from
  [GitHub Releases](https://github.com/inwardflow/BTPanel-API-Java-SDK/releases).
- Or install it into your local Maven repository from a release tag:

  ```bash
  git clone --branch v0.2.0 https://github.com/inwardflow/BTPanel-API-Java-SDK.git
  cd BTPanel-API-Java-SDK
  ./mvnw install -DskipTests
  ```

  Then declare the dependency:

  ```xml
  <dependency>
    <groupId>net.heimeng</groupId>
    <artifactId>btpanel-api-java-sdk</artifactId>
    <version>0.2.0</version>
  </dependency>
  ```

The SDK logs through SLF4J and does not ship a logging backend. Add one (for example Logback)
to your application if you want to see SDK logs.

## Quick Start

### Build

Linux or macOS:

```bash
./mvnw verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd verify
```

### Create a Client

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

`connectTimeout(int)` and `readTimeout(int)` take seconds. Overloads that take a `Duration` are also available.

### Typical Usage

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
BtResult<String> nginxConfig = apiManager.website().getNginxConfig("example.com");
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

## Website API Design Notes

The BT Panel website module returns several incompatible payload shapes. To keep endpoint implementations small and consistent, the SDK now uses shared parser abstractions:

- `AbstractWebsiteBooleanApi`
- `AbstractWebsiteTextQueryApi`
- `AbstractWebsiteMapQueryApi`
- `AbstractWebsiteMapListQueryApi`
- `WebsiteApiResponseSupport`

Concrete website endpoints should focus on:

- endpoint path,
- parameter validation,
- payload field naming,
- and domain-specific success or failure messages.

## Facade Naming Conventions

The repository is moving toward a stable `1.0` public API. For read-side website operations, the preferred collection-oriented names are:

- `listRaw(...)`
- `listTypes()`
- `listPhpVersions()`
- `listDomains(int siteId)`
- `listPhpExtensions(int siteId)`
- `listSslCertificates(int siteId)`
- `listBackups(Integer siteId, Integer page, Integer limit, String callback)`

For write-side website operations, the preferred names now follow action-oriented verbs such as:

- `removeDomain(...)`
- `updateRemark(...)`
- `updateRootPath(...)`
- `updateRunPath(...)`
- `toggleUserIni(...)`
- `updatePhpVersion(...)`
- `updateRewriteRules(...)`
- `updateNginxConfig(...)`
- `enablePasswordProtection(...)`
- `disablePasswordProtection(...)`
- `installSslCertificate(...)`
- `disableSsl(...)`
- `toggleLogs(...)`
- `updateLimitNet(...)`

For more complex commands, prefer the typed option objects over long parameter lists:

- `create(WebsiteCreateRequest request)`
- `database().create(DatabaseCreateRequest request)`
- `database().delete(DatabaseDeleteRequest request)`
- `database().updatePassword(int databaseId, DatabasePasswordUpdateRequest request)`
- `ftp().create(FtpCreateRequest request)`
- `ftp().delete(FtpDeleteRequest request)`
- `ftp().updatePassword(FtpPasswordUpdateRequest request)`
- `delete(int siteId, String websiteName, WebsiteDeleteOptions options)`
- `addDomain(int siteId, WebsiteDomainBinding binding)`
- `removeDomain(int siteId, WebsiteDomainRemoval removal)`
- `enablePasswordProtection(int siteId, WebsitePasswordProtectionOptions options)`
- `updateNginxConfig(int siteId, WebsiteNginxConfigOptions options)`
- `installSslCertificate(int siteId, WebsiteSslCertificateOptions options)`
- `updateLimitNet(int siteId, WebsiteLimitNetOptions options)`

`WebsiteCreateRequest` uses a builder so optional provisioning stays explicit:

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

Database and FTP write flows now follow the same typed-request approach:

```java
DatabaseCreateRequest databaseRequest =
    DatabaseCreateRequest.builder("demo_db", "demo_user", "secret")
        .remark("Demo database")
        .build();

apiManager.database().create(databaseRequest);
apiManager.database().updatePassword(
    71,
    new DatabasePasswordUpdateRequest("demo_db", "demo_user", "new-secret"));

FtpCreateRequest ftpRequest =
    new FtpCreateRequest("demo_ftp", "secret", "/www/wwwroot/demo", "Demo FTP");

apiManager.ftp().create(ftpRequest);
apiManager.ftp().updatePassword(
    new FtpPasswordUpdateRequest(12, "demo_ftp", "/www/wwwroot/demo", "new-secret"));
apiManager.ftp().delete(new FtpDeleteRequest(12, "demo_ftp"));
```

## Connecting to a Panel over HTTPS

BT Panel ships with a self-signed certificate, so the default `SYSTEM_TRUST` mode rejects it with a
clear error. Pick one of these, in order of preference:

1. **Install a trusted certificate on the panel** (Settings > Security > Panel SSL, which can issue a
   trusted IP certificate; see the
   [official guide](https://docs.bt.cn/user-guide/ai/mcp-installation)). The default configuration
   then works unchanged.
2. **Pin the panel's public key.** This is how to keep the self-signed certificate safely:

   ```java
   BtSdkConfig config =
       BtSdkConfig.builder()
           .baseUrl("https://your-panel-host:8888")
           .apiKey(System.getenv("BT_PANEL_API_KEY"))
           .pinnedPublicKeys("sha256/<base64-of-spki-sha256>")
           .build();
   ```

   Read the pin on the panel server itself (or another channel you trust):

   ```bash
   openssl s_client -connect 127.0.0.1:8888 </dev/null 2>/dev/null | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
   ```

   If the pin does not match, the error message shows the key the server actually presented. The
   pin survives certificate renewal as long as the key pair stays the same. To rotate keys, configure
   both the old and the new pin.
3. **Use a custom trust store** with `trustStore(path, password[, type])` if you manage your own CA.

`INSECURE_TRUST_ALL` disables verification and logs a warning on startup. Use it only in isolated
test environments.

Certificate errors and unresolvable hosts are never retried. Other failures are retried according
to `RetryMode`, with exponential backoff and jitter capped by `maxRetryInterval`. A `Retry-After`
header is honoured.

## SSL Notes

- `install(domain, key, cert)` maps `domain` to the panel's site-name style API parameter. The panel
  also saves the certificate into its certificate store, so it can later be deployed to other sites.
- `SslCertificate.domains` comes from the certificate `CN` and `SAN` values, so it may differ from the original site name used during installation.

## Deleting Files and Directories

- `file().delete(path)` removes a file and `file().deleteDirectory(path)` removes a directory with its
  contents. The panel moves both into its recycle bin when the bin is enabled.
- Paths must be absolute. Paths containing `..`, and system or panel directories such as `/`, `/etc`,
  or `/www/wwwroot` themselves, are rejected before any request is sent.
- The API key has full control of the panel, and the panel cannot scope it. If paths come from
  end users, check them with `RemotePaths.isWithin(allowedBase, path)` first.

## API Sources

The SDK follows what the panel actually sends. The panel UI uses the same API, so the browser's
network inspector is the source of truth. The [official API reference](https://docs.bt.cn/api/) is
useful, but it is not complete. Endpoints in this SDK are verified against a live BTPanel 9.0.0
instance by the opt-in integration tests.

## Build and Test

- Full verification: `./mvnw verify`
- Unit tests only: `./mvnw test`
- Include integration tests: `./mvnw verify -Pintegration-tests`

Integration tests should be configured through environment variables or `src/test/resources/application-test.properties`. Use `src/test/resources/application-test.properties.example` as the template, and do not commit real credentials.

OpenAPI live-validation outputs under `docs/openapi` are treated as local workspace artifacts unless they are intentionally curated into stable documentation.

## Project Structure

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

## Documentation

- [Architecture](docs/architecture.md)
- [Testing](docs/testing.md)
- [Quickstart Example](docs/examples/quickstart.md)
- [BT Panel API Developer Guide](docs/btpanel-api-developer-guide.md) (Chinese)
- [OpenAPI Workspace Notes](docs/openapi/README.md)
- [Release Checklist](docs/release-checklist.md)
- [Contributing](CONTRIBUTING.md)
- [Security Policy](SECURITY.md)
- [Changelog](CHANGELOG.md)

## Versioning

The project is still in the `0.x` stage. Public APIs may continue to evolve while the SDK moves toward a stable `1.0` release.

## License

[MIT](LICENSE)
