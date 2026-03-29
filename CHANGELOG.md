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
- Standardized `WebsiteOperations` read-side naming toward `list*` methods and kept compatibility
  aliases deprecated where appropriate.

### Added

- Unit tests for shared website parsing paths, including backups, PHP extensions, SSL certificate
  lists, and facade delegation coverage for the preferred read-side method names.
- Public repository metadata and community files such as contributing, security, CI, and release
  documentation.

### Removed

- Unused legacy API enum and outdated example classes that no longer matched the current SDK design.
- Library-level binding to a concrete logging implementation.
