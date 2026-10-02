# Testing

## Test Layers

The project uses two test layers:

1. Unit tests
   Run with Surefire and cover parameter validation, response parsing, facade delegation, and
   client behavior.
2. Integration tests
   Run with Failsafe or a targeted integration profile. They are skipped by default and only talk
   to a real BTPanel when explicitly enabled.

## Common Commands

Run unit tests:

```bash
./mvnw test
```

Run the regular verification pipeline:

```bash
./mvnw verify
```

Enable integration tests:

```bash
./mvnw verify -Pintegration-tests
```

Run only the SSL integration suite:

```bash
./mvnw -Pintegration-tests "-Dit.test=SslIntegrationTest" verify
```

In Windows PowerShell:

```powershell
$env:ENABLE_INTEGRATION_TESTS = 'true'
./mvnw.cmd -Pintegration-tests "-Dit.test=SslIntegrationTest" verify
```

## Integration Test Prerequisites

All integration tests require:

- `ENABLE_INTEGRATION_TESTS=true`
- `BT_PANEL_BASE_URL`
- `BT_PANEL_API_KEY`

If a module-specific configuration is missing, that module's integration test is skipped instead of
failing the whole suite. If the panel enforces an IP whitelist and the current machine is not
allowed, the corresponding tests are skipped with an explicit reason.

You can also provide the same configuration in `src/test/resources/application-test.properties`.

## Optional Module Configuration

Database tests:

- `BT_PANEL_TEST_DB_NAME`
- `BT_PANEL_TEST_DB_USER`
- `BT_PANEL_TEST_DB_PASSWORD`

File tests:

- `BT_PANEL_TEST_FILE_PATH`

FTP tests:

- `BT_PANEL_TEST_FTP_ROOT`

If `BT_PANEL_TEST_FTP_ROOT` is missing, the test fallback order is:

1. `BT_PANEL_TEST_WEBROOT_BASE`
2. The parent directory of `BT_PANEL_TEST_FILE_PATH`

Website tests:

- `BT_PANEL_TEST_DOMAIN_SUFFIX`
- `BT_PANEL_TEST_WEBROOT_BASE`

Configuration notes:

- `test.domain` can be a domain suffix such as `example.com`.
- It can also be a full test domain such as `test.example.com`.
- `test.webroot` can be a base directory such as `/www/wwwroot`.
- It can also point to an existing test site directory such as `/www/wwwroot/test.example.com`.

SSL tests:

- Reuse the website test configuration:
  - `BT_PANEL_TEST_DOMAIN_SUFFIX`
  - `BT_PANEL_TEST_WEBROOT_BASE`
- Provide metadata for an already saved certificate in the panel:
  - `BT_PANEL_TEST_SSL_CERT_COMMON_NAME`
  - `BT_PANEL_TEST_SSL_CERT_DOMAINS`
- Optional TLS verification control:
  - `BT_PANEL_VERIFY_SSL`

Timeout and retry settings:

- `BT_PANEL_CONNECT_TIMEOUT`
- `BT_PANEL_READ_TIMEOUT`
- `BT_PANEL_RETRY_COUNT`

Matching `application-test.properties` keys:

- `baseUrl`
- `apiKey`
- `verifySsl`
- `test.dbName`
- `test.dbUser`
- `test.dbPassword`
- `test.filePath`
- `test.ftpRoot`
- `test.domain`
- `test.webroot`
- `test.ssl.certificate.common-name`
- `test.ssl.certificate.domains`
- `connectTimeout`
- `readTimeout`
- `retryCount`

## SSL Integration Behavior

`SslIntegrationTest` currently covers:

- Querying the saved certificate list via `ssl?action=get_cert_list`
- Querying per-site SSL status via `site?action=GetSSL`
- Querying commercial order data via `ssl?action=get_order_list`
- Resolving deployable sites via `ssl?action=GetSiteDomain`
- Deploying a saved certificate via `ssl?action=SetBatchCertToSite`

Key behavior:

1. The test creates an isolated temporary site.
2. It does not upload PEM text directly.
3. It expects a saved certificate to already exist in the panel certificate store.
4. Certificate matching is based on configured CN/domain metadata rather than the temporary site
   name.
5. Cleanup deletes only the temporary site created by the test.

For the currently validated BTPanel 9.0.0 developer API flow, the SSL integration test follows:

`GetSSL -> get_order_list -> GetSiteDomain -> SetBatchCertToSite`

It no longer treats `site?action=SetSSL` as the primary integration path.

## Current Coverage

The repository currently includes:

- `SystemIntegrationTest`
- `DatabaseIntegrationTest`
- `FileIntegrationTest`
- `FtpIntegrationTest`
- `WebsiteIntegrationTest`
- `SslIntegrationTest`

## Recommendations

- Add at least one response-parsing test for every new `BtApi` implementation.
- Add at least one facade delegation test for every new facade method.
- Keep integration tests self-cleaning and isolated from execution order.
- Do not make one `@Test` method call another `@Test` method directly.
