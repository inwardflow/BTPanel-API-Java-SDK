# BTPanel API Java SDK

[![CI](https://github.com/inwardflow/BTPanel-API-Java-SDK/actions/workflows/ci.yml/badge.svg)](https://github.com/inwardflow/BTPanel-API-Java-SDK/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

`BTPanel API Java SDK` is a Java 17 client library for the BT Panel API. It combines a flexible low-level `BtApi<T>` model with higher-level facades for common automation tasks such as website management, database operations, file handling, FTP, SSL, and system inspection.

## Features

- Java 17 `HttpClient` implementation.
- Immutable `BtSdkConfig` with validation for timeouts, retries, and SSL mode.
- Explicit retry strategy via `RetryMode`: `NONE`, `SAFE_REQUESTS_ONLY`, and `ALL_REQUESTS`.
- Multiple SSL modes, including system trust, custom trust store, and insecure trust-all for controlled environments.
- Stable high-level entry point through `BtApiManager`.
- Facade-based access for common modules: `system()`, `website()`, `database()`, `file()`, `ftp()`, and `ssl()`.
- Extensible low-level endpoint model for unsupported or newly discovered panel APIs.
- Maven-based quality gates with formatting, style checks, unit tests, integration-test separation, and coverage reporting.

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

### Typical Usage

```java
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;
import net.heimeng.sdk.btapi.model.website.WebsiteInfo;

BtResult<java.util.List<WebsiteInfo>> websites = apiManager.website().list(1, 20);
BtResult<Integer> taskCount = apiManager.system().getTaskCount();
BtResult<String> nginxConfig = apiManager.website().getNginxConfig(1, "example.com");
BtResult<java.util.List<SslCertificate>> certificates = apiManager.ssl().list();
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
- `updatePhpExtension(...)`
- `updateRewriteRules(...)`
- `updateNginxConfig(...)`
- `enablePasswordProtection(...)`
- `disablePasswordProtection(...)`
- `installSslCertificate(...)`
- `disableSsl(...)`
- `toggleLogs(...)`
- `updateLimitNet(...)`

## SSL Notes

- `install(domain, key, cert)` maps `domain` to the panel's site-name style API parameter.
- `SslCertificate.domains` comes from the certificate `CN` and `SAN` values, so it may differ from the original site name used during installation.
- If your panel uses a private CA, prefer `CUSTOM_TRUST_STORE` with `trustStore(path, password[, type])`.
- Use `INSECURE_TRUST_ALL` only in tightly controlled test environments.

## Build and Test

- Full verification: `./mvnw verify`
- Unit tests only: `./mvnw test`
- Include integration tests: `./mvnw verify -Pintegration-tests`

Integration tests should be configured through environment variables or `src/test/resources/application-test.properties`. Use `src/test/resources/application-test.properties.example` as the template, and do not commit real credentials.

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
- [Release Checklist](docs/release-checklist.md)
- [Contributing](CONTRIBUTING.md)
- [Security Policy](SECURITY.md)
- [Changelog](CHANGELOG.md)

## Versioning

The project is still in the `0.x` stage. Public APIs may continue to evolve while the SDK moves toward a stable `1.0` release.

## License

[MIT](LICENSE)
