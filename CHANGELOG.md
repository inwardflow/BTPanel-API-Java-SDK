# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

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

### Fixed

- Javadoc generation for the `release` profile (unescaped generic types and heading levels).
- `SslCertificateTest` failing when the JVM default time zone is west of UTC.

### Removed

- Unused legacy API enum and outdated example classes that no longer matched the current SDK design.
- Library-level binding to a concrete logging implementation.
