# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- Simplified Chinese README (`README.zh-CN.md`) with language links in both READMEs.

### Fixed

- The SSL integration test now also deletes the panel's certificate directory
  (`/www/server/panel/vhost/cert/<domain>`) during cleanup. Deleting a site leaves that directory
  behind, and the panel later re-imports the certificates from it, so removing only the store entry
  was not durable. `DeleteSslCertificateApi` now documents this behaviour.

## [0.1.0] - 2026-10-02

First tagged release of the redesigned SDK. The public API may still change before 1.0.

### Added

- Unit tests for shared website parsing paths, including backups, PHP extensions, SSL certificate
  lists, and facade delegation coverage for the preferred read-side method names.
- Public repository metadata and community files such as contributing, security, CI, and release
  documentation.
- `FileOperations`, `SslOperations`, and `SystemOperations` facades, plus SSL deployment helpers
  (deployable sites, site SSL status, order list, and batch certificate deployment).
- GitHub Actions workflows: CI on JDK 17 and 21, CodeQL, manual integration tests, and a
  tag-driven release workflow that publishes the jar, sources, and Javadoc to GitHub Releases.
- Opt-in integration suites for the FTP, SSL, and system modules.
- `DeleteFileDirectoryApi` and `FileOperations.deleteDirectory(path)`. The panel's `DeleteFile`
  action cannot remove directories; they require `DeleteDir`.
- `SslMode.PINNED_PUBLIC_KEY` and `BtSdkConfig.Builder.pinnedPublicKeys(...)`. These trust BT Panel's
  default self-signed certificate by its public-key pin instead of disabling verification.
  `CertificatePins.sha256(certificate)` computes a pin.
- `RemotePaths` with `requireSafeAbsolutePath`, `requireDeletablePath`, and `isWithin`.
- `BtSdkConfig.Builder.maxRetryInterval(...)` (default 30 s).

### Changed

- Refactored the SDK around `BtApiManager` and domain facades for a more stable public entry point.
- Hardened `BtSdkConfig` into an immutable configuration object with stricter build-time validation.
- Upgraded the Maven quality gate pipeline with `Spotless`, `Checkstyle`, `Surefire`, `Failsafe`, and `JaCoCo`.
- Defaulted integration tests to opt-in execution to avoid accidental calls against a live BT Panel instance.
- Aligned `DefaultBtClient` retry behavior with `RetryMode`, including clearer handling for safe-request retries.
- Added support for `CUSTOM_TRUST_STORE` SSL configuration and clearer SSL startup failures.
- Split transport responsibilities into `RetryPolicy`, `SslContextConfigurer`, and `RequestEncodingUtils`.
- Consolidated duplicated website response parsing into shared helpers:
  `WebsiteApiResponseSupport`, `AbstractWebsiteBooleanApi`, `AbstractWebsiteTextQueryApi`,
  `AbstractWebsiteMapQueryApi`, and `AbstractWebsiteMapListQueryApi`.
- Migrated multiple website query endpoints to the shared parsers, including website detail, config,
  domains, raw list, backups, PHP extensions, SSL certificate list, and limit-net configuration.
- Standardized `WebsiteOperations` read-side naming toward `list*` methods and action-oriented
  write methods. Legacy aliases were removed instead of deprecated, since the SDK is pre-1.0.
- Replaced long parameter lists in the website, database, and FTP facades with typed request
  objects such as `WebsiteCreateRequest`, `DatabaseCreateRequest`, and `FtpCreateRequest`.
- Changed the Maven coordinates to `net.heimeng:btpanel-api-java-sdk` and reset the version line to
  `0.x` to signal that the public API may still change between minor releases.

- **Behaviour change:** POST/PUT/PATCH requests now send every parameter, including the signature,
  only in the form body. Earlier builds also copied short parameters into the URL query string.
  Body-only requests were verified against BTPanel 9.0.0.
- Certificate validation failures and unresolvable hosts are no longer retried, because retrying
  cannot fix them. Failure logs now include the exception type.
- Retries use exponential backoff with jitter. `retryInterval` is the base and `maxRetryInterval` the
  cap. A `Retry-After` response header is honoured up to the cap.
- TLS failures now explain how to trust a self-signed panel (trusted panel certificate, or a pin)
  instead of reporting a bare "Network error".
- `INSECURE_TRUST_ALL` now logs a warning when a client is created.
- **Breaking:** `WebsiteOperations.disableSsl` now takes the site name instead of the site ID,
  matching the panel's `CloseSSLConf` action.
- `InstallSslCertificateApi` (`SetSSL`) is no longer deprecated. It is a documented panel action, and
  it also saves the certificate into the panel's store.
- Integration tests verify TLS by default. Set `BT_PANEL_TLS_PIN` for self-signed panels.
  `BT_PANEL_TEST_SSL_CERT_*` is gone, because the SSL test now generates its own certificate.

### Removed

- Unused legacy API enum and outdated example classes that no longer matched the current SDK design.
- Library-level binding to a concrete logging implementation.

### Fixed

- Javadoc generation for the `release` profile (unescaped generic types and heading levels).
- `SslCertificateTest` failing when the JVM default time zone is west of UTC.
- `BtUtils.generateRequestTime()` returned milliseconds; the panel expects Unix seconds, so tokens
  built from it were rejected. It now returns seconds and `DefaultBtClient` uses it directly.
- Request signing now hashes with an explicit UTF-8 charset instead of the platform default.
- The invalid-API-key integration test now uses the suite's SSL settings and expects
  `BtAuthenticationException`. Before, it passed because of a certificate error and never
  reached the authentication check.
- The file and FTP integration tests now clean up directories with `DeleteDir`, so they no longer
  leave directories behind on the panel.
- `CloseWebsiteSslApi` called `site?action=CloseSSL` with `id`. On BTPanel 9.0.0 the panel answers
  "指定参数无效", so disabling SSL never worked. It now calls `CloseSSLConf` with `siteName` and
  `updateOf=1`.

### Security

- Debug logging no longer writes the full request URL unmasked. URL masking now uses the same
  substring rules as parameter masking, so `request_token`, `ftp_password`, `new_password`, and
  similar keys are redacted in both places.
- Passwords and request signatures no longer appear in request URLs, where panel access logs and
  proxies could record them.
- File and directory deletion rejects relative paths, `..` segments, control characters, and
  system or panel directories such as `/`, `/etc`, and `/www/wwwroot` before sending the request.

[Unreleased]: https://github.com/inwardflow/BTPanel-API-Java-SDK/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/inwardflow/BTPanel-API-Java-SDK/releases/tag/v0.1.0
