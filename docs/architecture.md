# Architecture

## Goals

This SDK is designed around two complementary layers:

1. Transport and execution layer
   `BtClient` owns request signing, HTTP transport, retry behavior, SSL handling, interceptor hooks, and raw response delivery.
2. Domain and facade layer
   `BtApi<T>` implementations model individual BT Panel endpoints, while `BtApiManager` and the `facade` package provide task-oriented entry points such as `system()`, `website()`, `database()`, `file()`, `ftp()`, and `ssl()`.

This split keeps the low-level contract extensible without forcing every new endpoint through a facade first, while still giving application code a stable, business-friendly API surface.

## Package Responsibilities

- `api`
  Endpoint definitions, parameter validation, and response parsing.
- `client`
  HTTP client implementation and SDK entry points.
- `config`
  Immutable SDK configuration with build-time validation.
- `exception`
  Unified exception hierarchy for transport, authentication, and API contract failures.
- `facade`
  High-level domain operations grouped by module.
- `interceptor`
  Request interception hooks for tracing, diagnostics, or customization.
- `model`
  Parsed domain models and generic result wrappers.

## Website Module Parsing Strategy

The website module has the widest variety of response shapes in the BT Panel API. Instead of letting each endpoint reimplement the same branching logic, the SDK now centralizes these contracts in shared parsing helpers:

- `WebsiteApiResponseSupport`
  Shared JSON parsing, recursive `Map` and `List` conversion, and `BtResult` construction helpers.
- `AbstractWebsiteBooleanApi`
  For endpoints that only return `status` and `msg`.
- `AbstractWebsiteTextQueryApi`
  For endpoints that may return plain text, wrapped text, or non-wrapped JSON text payloads.
- `AbstractWebsiteMapQueryApi`
  For endpoints that return a single object, either directly or inside `data`.
- `AbstractWebsiteMapListQueryApi`
  For endpoints that return a list of object payloads, either directly or inside a named array field.

This design gives us three benefits:

- Lower duplication across `api/website`.
- More consistent failure handling and error messages.
- Easier addition of new endpoints because only endpoint-specific validation and field names remain in concrete classes.

## Facade Design

Facade methods are optimized for readability and gradual public API stabilization:

- Prefer verb-first collection methods such as `listRaw`, `listTypes`, `listDomains`, `listPhpExtensions`, `listSslCertificates`, and `listBackups`.
- Keep deprecated aliases temporarily when they are thin compatibility shims.
- Normalize parameter order to place the primary resource identifier first when feasible.

This allows the SDK to evolve toward a stable `1.0` surface without forcing downstream users into a breaking migration all at once.

## Quality Gates

The repository is prepared for public open-source maintenance with the following defaults:

- Maven Wrapper as the standard build entry point.
- `Spotless` for formatting.
- `Checkstyle` for style validation.
- `Surefire` for unit tests.
- `Failsafe` for integration tests.
- `JaCoCo` for coverage reporting.

Integration tests are opt-in and should not run by default against a real panel.

## Key Constraints

- The SDK is a library and should not force a concrete logging backend on consumers.
- Validation should happen as early as possible, ideally when building config objects or endpoint parameter objects.
- Parsing code should fail loudly when the panel returns a contract shape we do not support, because silent coercion hides upstream API drift.
