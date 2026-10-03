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
Prefer environment variables for the API key, and keep them outside the repository.

### Running from Git Bash on Windows

Git Bash rewrites environment values that look like Unix paths before they reach Windows
programs. For example, `/www/wwwroot` becomes `C:/Program Files/Git/www/wwwroot`, and the panel
then rejects the website or FTP path. Exclude the SDK variables from this conversion:

```bash
export MSYS2_ENV_CONV_EXCL='BT_PANEL_'
```

PowerShell, `cmd`, macOS, and Linux are not affected.

### Mockito fails to initialize on some Windows machines

If every Mockito-based unit test fails with `Could not initialize plugin: interface
org.mockito.plugins.MockMaker` caused by `Could not self-attach to current VM`, allow the test JVM to
attach its own agent:

```bash
export JAVA_TOOL_OPTIONS='-Djdk.attach.allowAttachSelf=true'
```

On machines that are short of memory (`Native memory allocation (mmap) failed`), also cap the heap,
for example by adding `-XX:+UseSerialGC -Xmx384m` to the same variable.

### Running in GitHub Actions

The `Integration Tests` workflow reads the same variables from the `integration-tests`
environment secrets. GitHub-hosted runners do not have fixed IP addresses, so a panel with an API
IP whitelist rejects them and the suite is skipped. Use a self-hosted runner with a whitelisted
IP, or run the suite locally.

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
- No certificate needs to exist in the panel beforehand. The test generates its own.

TLS settings (certificate verification is on by default):

- `BT_PANEL_TLS_PIN`: the panel's public-key pin (`sha256/<base64>`). Use it for BT Panel's default
  self-signed certificate. See README > Connecting to a Panel over HTTPS for how to read it.
- `BT_PANEL_VERIFY_SSL=false`: disables verification. Use it only in an isolated test environment.

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

- Installing a certificate on a site via `site?action=SetSSL`
- Turning site SSL off via `site?action=CloseSSLConf`
- Deleting a saved certificate via `ssl?action=remove_cloud_cert`
- Querying the saved certificate list via `ssl?action=get_cert_list`
- Querying per-site SSL status via `site?action=GetSSL`
- Querying commercial order data via `ssl?action=get_order_list`
- Resolving deployable sites via `ssl?action=GetSiteDomain`
- Deploying a saved certificate via `ssl?action=SetBatchCertToSite`

Key behavior:

1. The test creates an isolated temporary site.
2. It generates a throwaway self-signed certificate for that site's domain (BouncyCastle, test scope
   only), so it does not depend on anything already in the panel and nothing is committed.
3. `SetSSL` installs it. The panel also saves it into the certificate store.
4. The test turns SSL off, then deploys the saved certificate with `SetBatchCertToSite`. This proves
   that the deployment, not the earlier install, turned SSL back on.
5. Cleanup deletes only the certificate issued for the temporary domain and the temporary site.

Validated flow on BTPanel 9.0.0:

`SetSSL -> get_cert_list -> CloseSSLConf -> GetSiteDomain -> SetBatchCertToSite -> GetSSL`

## Current Coverage

The repository currently includes:

- `SystemIntegrationTest`
- `DatabaseIntegrationTest`
- `FileIntegrationTest`
- `FtpIntegrationTest`
- `WebsiteIntegrationTest`
- `SslIntegrationTest`

Endpoints added or corrected after the 2026-10-03 UI capture have their own integration tests:

- `WebsiteIntegrationTest`: stop/start (`SiteStop`/`SiteStart`), PHP version switching
  (`GetSitePHPVersion`/`SetPHPVersion`), rewrite rules and Nginx config through the vhost files,
  `.user.ini` toggling (`SetDirUserINI`), and PHP runtime config (`GetPHPConfig`).
- `FileIntegrationTest`: rename, copy and move (`MvFile`/`CopyFile`), and compress/extract
  (`Zip`/`UnZip`).
- `DatabaseIntegrationTest`: password change (`ResDatabasePassword`).

See `docs/openapi/live-validation-matrix.md` for the UI evidence behind each endpoint.

### Last live run

Run on 2026-10-02 against BTPanel 9.0.0 on Ubuntu 22.04: all 26 integration tests passed (Database 4, File 5,
FTP 4, SSL 3, System 7, Website 3), with no skips. The panel's site, database, and FTP counts were
unchanged afterwards, which confirms that cleanup works.

## Recommendations

- Add at least one response-parsing test for every new `BtApi` implementation.
- Add at least one facade delegation test for every new facade method.
- Keep integration tests self-cleaning and isolated from execution order.
- Do not make one `@Test` method call another `@Test` method directly.
