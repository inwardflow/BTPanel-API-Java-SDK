# BT Panel Live Validation Matrix

Panel: BTPanel 9.0.0 (Linux, free edition), Nginx 1.24, MySQL 8.0.35, Pure-FTPd 1.0.49, PHP 8.1.

Source of truth: the panel's own UI traffic. Each SDK action was compared with what the logged-in 9.0
web UI sends; [docs.bt.cn](https://docs.bt.cn/api/) is a secondary reference and does not list many of
the actions the UI uses.

Evidence:

- **UI capture (2026-10-03)**: requests recorded with an `XMLHttpRequest.prototype` hook in the panel
  SPA (and in the legacy `/software` iframe) while clicking through the UI on throwaway resources. The
  parameter names in the tables below come from that capture; the raw capture
  (`ui-capture-validation-2026-10-03.json`) is a local, git-ignored artifact like the other
  `docs/openapi/*-validation-*.json` files.
- **UI bundle**: the endpoint is called by the 9.0 Vite bundle (`post("module/Action", ...)`; 598
  chunks, 710 distinct endpoints), but was not exercised in the capture.
- **SSL capture (2026-03-29)**: DevTools capture of the SSL flow (local artifact
  `ssl-devtools-capture-validation-2026-03-29.json`).
- **Docs**: listed on docs.bt.cn; not re-captured.

All endpoints are `POST /<module>?action=<Action>` with form-encoded parameters. Only resources created
for the capture were modified, and all of them were deleted afterwards.

Status legend:

| Status | Meaning |
|---|---|
| ✅ Matches UI | The SDK sends the same action and parameters as the UI capture. |
| 🔧 Fixed | The SDK was wrong; it now sends what the UI sends (old form deprecated where the signature changed). |
| ⚠️ Deprecated | The 9.0 UI never calls this action; the SDK API is deprecated for removal and points to a replacement. |
| 📄 Docs only | Documented but not re-captured in this round. |

## Website (`site`, `data`)

| SDK action | SDK entry point | Evidence | Status | Notes |
|---|---|---|---|---|
| `site?action=AddSite` | `CreateWebsiteApi`, `WebsiteOperations.create` | UI capture | ✅ Matches UI | UI additionally sends `need_index=0`, `need_404=0`. |
| `site?action=DeleteSite` | `DeleteWebsiteApi`, `WebsiteOperations.delete` | UI capture `{id, webname, path=1}` | ✅ Matches UI | |
| `/datalist/data/get_data_list` (`table=sites`) | `GetWebsiteListApi` | UI capture `{table, p, limit, search, type}` | ✅ Matches UI | 9.0 UI lists sites, FTP and databases here. |
| `data?action=getData` (`table=sites`) | `GetWebsitesApi` | UI bundle | ✅ Matches UI | Passed the 2026-03-29 live run. |
| `data?action=getData` (`table=domain`) | `GetWebsiteDomainsApi`, `listDomains` | UI capture `{table=domain, list=True, search=<siteId>}` | ✅ Matches UI | |
| `data?action=getData` (`table=backup`) | `GetWebsiteBackupsApi`, `listBackups` | UI capture `{table=backup, search=<siteId>, type=0, p, limit}` | ✅ Matches UI | |
| `data?action=getKey` (`table=sites&key=path`) | `GetWebsiteRootPathApi`, `getRootPath` | UI capture `{table, key, id}` | ✅ Matches UI | |
| `data?action=setPs` (`table=sites`) | `SetWebsitePsApi`, `updateRemark` | UI capture `{table, id, ps}` | ✅ Matches UI | |
| `site?action=AddDomain` | `AddWebsiteDomainApi`, `addDomain` | UI capture `{id, webname, domain}` | ✅ Matches UI | |
| `site?action=DelDomain` | `DeleteWebsiteDomainApi`, `removeDomain` | UI capture `{id, webname, domain, port}` | ✅ Matches UI | |
| `site?action=SiteStart` | `StartWebsiteApi`, `start(int, String)` | UI capture `{id, name}`; live: `{id}` alone returns HTTP 404 | 🔧 Fixed | The SDK sent only `id`. |
| `site?action=SiteStop` | `StopWebsiteApi`, `stop(int, String)` | UI capture `{id, name}` | 🔧 Fixed | Was `site?action=StopSite {id}`, which is not in the 9.0 UI. |
| `site?action=GetSitePHPVersion` | `GetWebsitePhpVersionApi`, `getPhpVersion(String)` | UI capture `{siteName}` → `{"phpversion": "81", ...}` | 🔧 Fixed | Was `site?action=getPhpVersion {id}`. |
| `site?action=SetPHPVersion` | `SetWebsitePhpVersionApi`, `updatePhpVersion(String, String)` | UI capture `{siteName, version, other}` | 🔧 Fixed | Was `site?action=SetPhpVersion {id, php_version}`. |
| `site?action=GetPHPVersion` | `GetPhpVersionsApi`, `listPhpVersions` | UI capture `{s_type=1, all=1}` | ✅ Matches UI | The SDK omits the `s_type`/`all` filters. |
| `ajax?action=GetPHPConfig` | `GetPhpRuntimeConfigApi`, `getPhpRuntimeConfig(String)` | UI capture `{version=81}` (software store › PHP › 安装扩展) | 🔧 Fixed (new) | Replacement for the per-site PHP extension APIs. |
| `site?action=GetPHPModules` | `GetWebsitePhpExtensionsApi`, `listPhpExtensions` | Not in UI bundle | ⚠️ Deprecated | Extensions belong to a PHP version; use `getPhpRuntimeConfig`. |
| `site?action=SetPHPModules` | `SetWebsitePhpExtensionsApi`, `updatePhpExtension` | Not in UI bundle | ⚠️ Deprecated | No per-site equivalent; extensions are installed per PHP version in the software store. |
| `site?action=GetDirUserINI` | `GetWebsiteConfigApi`, `getConfig` | UI capture `{id, path}` | ✅ Matches UI | |
| `site?action=SetDirUserINI` | `SetWebsiteUserIniApi`, `toggleUserIni(int, String)` | UI capture `{id, path}` | 🔧 Fixed | The SDK sent only `path`. Each call toggles the flag. |
| `site?action=SetPath` | `SetWebsiteRootPathApi`, `updateRootPath` | UI capture `{id, path}` | ✅ Matches UI | |
| `site?action=SetSiteRunPath` | `SetWebsiteRunPathApi`, `updateRunPath` | UI capture `{id, runPath}` | ✅ Matches UI | |
| `files?action=GetFileBody` on `vhost/rewrite/<site>.conf` | `getRewriteRules(String)` | UI capture (伪静态 tab) | 🔧 Fixed | Was `site?action=getRewrite {id}`, not in the 9.0 UI. |
| `files?action=SaveFileBody` on `vhost/rewrite/<site>.conf` | `updateRewriteRules(String, String)` | UI capture `{path, data, encoding=utf-8}` | 🔧 Fixed | Was `site?action=setRewrite {id, name, content}`. |
| `files?action=GetFileBody` on `vhost/nginx/<site>.conf` | `getNginxConfig(String)` | UI capture (配置文件 tab) | 🔧 Fixed | Was `site?action=getConf {id, domain}`. |
| `files?action=SaveFileBody` on `vhost/nginx/<site>.conf` | `updateNginxConfig(String, String)` | UI capture `{path, data, encoding=utf-8}` | 🔧 Fixed | Was `site?action=setConf {id, domain, content}`. |
| `site?action=getRewrite` / `setRewrite` / `getConf` / `setConf` | `GetWebsiteRewriteRulesApi`, `SetWebsiteRewriteRulesApi`, `GetWebsiteNginxConfigApi`, `SetWebsiteNginxConfigApi` | Not in UI bundle | ⚠️ Deprecated | Replaced by the vhost-file methods above. |
| `site?action=ToBackup` | `CreateWebsiteBackupApi`, `createBackup` | UI capture `{id, backstage=1}`; live `{id}` → 备份成功 | ✅ Matches UI | Without `backstage=1` the panel backs up synchronously; the UI uses `backstage=1` to queue a background task. |
| `site?action=DelBackup` | `DeleteWebsiteBackupApi`, `deleteBackup` | UI capture `{id}` | ✅ Matches UI | |
| `site?action=GetSiteStatus` | `GetWebsiteDetailApi`, `getDetail` | Not in UI bundle | ⚠️ Deprecated | Site details come from the site list (`listRaw`) and `getConfig`. |
| `site?action=get_site_types` | `GetWebsiteTypesApi`, `listTypes` | UI capture (site list load) | ✅ Matches UI | |
| `site?action=SetHasPwd` / `CloseHasPwd` | `SetWebsitePasswordApi`, `CloseWebsitePasswordApi` | Docs | 📄 Docs only | |
| `site?action=GetLimitNet` / `SetLimitNet` | `GetWebsiteLimitNetApi`, `SetWebsiteLimitNetApi` | Docs | 📄 Docs only | |
| `site?action=logsOpen` | `SetWebsiteLogsApi`, `toggleLogs` | Docs | 📄 Docs only | |

## SSL (`site`, `ssl`)

| SDK action | SDK entry point | Evidence | Status | Notes |
|---|---|---|---|---|
| `site?action=SetSSL` | `SetWebsiteSslApi`, `InstallSslCertificateApi` | Docs + SSL live tests | ✅ Live | Covered by `SslIntegrationTest`. |
| `site?action=GetSSL` | `GetWebsiteSslStatusApi`, `SslOperations.getWebsiteStatus` | SSL capture `{siteName}` | ✅ Matches UI | |
| `site?action=CloseSSLConf` | `CloseWebsiteSslApi`, `disableSsl` | Live (0.1.0) `{siteName, updateOf=1}` | ✅ Live | Was `CloseSSL {id}`, fixed in 0.1.0. |
| `site?action=GetSSLCertList` | `GetWebsiteSslListApi`, `listSslCertificates` | Not in UI bundle; live run 2026-03-29 returned 指定参数无效 | ⚠️ Deprecated | Use `SslOperations.getWebsiteStatus` / `SslOperations.list`. |
| `ssl?action=get_cert_list` | `GetSslCertificatesApi`, `SslOperations.list` | SSL capture | ✅ Matches UI | Returns a bare JSON array. |
| `ssl?action=get_order_list` | `GetSslOrderListApi` | SSL capture | ✅ Matches UI | |
| `ssl?action=GetSiteDomain` | `GetSslDeployableSitesApi` | SSL capture | ✅ Matches UI | |
| `ssl?action=SetBatchCertToSite` | `SetBatchSslCertificateToSiteApi` | SSL capture + UI bundle | ✅ Matches UI | |
| `ssl?action=remove_cloud_cert` | `DeleteSslCertificateApi`, `SslOperations.delete` | UI bundle + live (0.1.0) | ✅ Live | Only `ssl_id` + `local=1` deletes; `ssl_hash` reports success without deleting. |

## Files (`files`)

| SDK action | SDK entry point | Evidence | Status | Notes |
|---|---|---|---|---|
| `files?action=GetFileBody` | `GetFileContentApi`, `getContent` | UI capture `{path}` | ✅ Matches UI | |
| `files?action=SaveFileBody` | `SaveFileContentApi`, `saveContent` | UI capture `{path, data, encoding}` | ✅ Matches UI | |
| `files?action=CreateFile` | `CreateFileApi`, `createFile` | UI capture `{path}` | ✅ Matches UI | |
| `files?action=CreateDir` | `CreateFileDirectoryApi`, `createDirectory` | UI capture `{path}` | ✅ Matches UI | |
| `files?action=DeleteFile` | `DeleteFileApi`, `delete` | UI bundle + live | ✅ Live | Cannot delete directories. |
| `files?action=DeleteDir` | `DeleteFileDirectoryApi`, `deleteDirectory` | UI bundle + live | ✅ Live | |
| `files?action=MvFile` (rename) | `RenameFileApi`, `rename` | UI capture `{sfile, dfile, rename=true}` | 🔧 Fixed | Was `files?action=RenameFile {oldpath, newname}`. |
| `files?action=MvFile` (move) | `MoveFileApi`, `move(String, String)` | UI capture `{sfile, dfile}` (cut/paste) | 🔧 Fixed | Was `files?action=MoveFile {source, target, type, moveType}`. |
| `files?action=CopyFile` | `CopyFileApi`, `copy` | UI bundle `{sfile, dfile}` | 🔧 Fixed (new) | Previously `MoveFile` with `type=copy`. |
| `files?action=Zip` | `CompressFileApi`, `compressTo` | UI capture `{path=<dir>/, sfile, dfile, z_type}` | 🔧 Fixed | Was `files?action=Compress {path, filename, format}`. |
| `files?action=UnZip` | `UncompressFileApi`, `uncompress` | UI capture `{sfile, dfile, type, coding, password, power}` | 🔧 Fixed | Was `files?action=UnCompress {path, target}`. The UI sends `type=zip` for `.tar.gz` too. |
| `files?action=GetDirNew` | `GetDirectoryListingApi`, `list` | UI capture `{path, p, showRow, sort, reverse}` + live | 🔧 Fixed (new) | Response has no `status`: `{path, dir[], files[], page}`; entries use `nm, sz, mt, acc, user, lnk, rmk, fav, top`. Directories and files are paged together, directories first; `page` is an HTML fragment. A missing path lists `/www/wwwroot` and a file path lists its parent, so the SDK fails when the returned `path` differs from the request. `files?action=GetDir` returns empty lists on 9.0. |

## Database (`database`)

| SDK action | SDK entry point | Evidence | Status | Notes |
|---|---|---|---|---|
| `database?action=AddDatabase` | `CreateDatabaseApi`, `DatabaseOperations.create` | UI capture (11 keys) | ✅ Matches UI | Same keys as the UI. |
| `database?action=DeleteDatabase` | `DeleteDatabaseApi`, `delete` | UI capture `{id, name}` | ✅ Matches UI | |
| `database?action=ResDatabasePassword` | `ChangeDatabasePasswordApi`, `updatePassword(int, request)` | UI capture `{id, name=<user>, password, data_name=<db>}` | 🔧 Fixed | Was `database?action=ChangeDBPassword {name, username, password}`. |
| `/datalist/data/get_data_list` (`table=databases`) | `GetDatabasesApi` | UI bundle + live | ✅ Live | |

## FTP (`ftp`)

| SDK action | SDK entry point | Evidence | Status | Notes |
|---|---|---|---|---|
| `ftp?action=AddUser` | `CreateFtpAccountApi`, `FtpOperations.create` | UI capture `{ftp_username, ftp_password, path, ps}` | ✅ Matches UI | |
| `ftp?action=SetUser` | `ChangeFtpPasswordApi`, `updatePassword` | UI capture `{id, ftp_username, new_password, path}` | ✅ Matches UI | |
| `ftp?action=DeleteUser` | `DeleteFtpAccountApi`, `delete` | UI capture `{id, username}` | ✅ Matches UI | |
| `/datalist/data/get_data_list` (`table=ftps`) | `GetFtpAccountsApi` | UI bundle + live | ✅ Live | |

## System (`system`, `ajax`)

| SDK action | SDK entry point | Evidence | Status | Notes |
|---|---|---|---|---|
| `system?action=GetSystemTotal` | `GetSystemInfoApi` | Docs + live | ✅ Live | |
| `system?action=GetDiskInfo` | `GetDiskInfoApi` | Docs + live | ✅ Live | |
| `system?action=GetNetWork` | `GetNetworkStatusApi` | UI capture (home page, no parameters) | ✅ Matches UI | |
| `ajax?action=GetTaskCount` | `GetTaskCountApi` | UI bundle (`instance.post("ajax/GetTaskCount")`, no parameters) | ✅ Matches UI | |
| `ajax?action=UpdatePanel` | `CheckPanelUpdateApi` | UI capture `{check=true}` (home page) | ✅ Matches UI | |

## Live API verification

Run on 2026-10-03 against the same panel with the developer API key (`request_time`/`request_token`):

- `./mvnw -Pintegration-tests verify`: all 35 integration tests pass (Database 5, File 7, FTP 4,
  SSL 3, System 7, Website 9), including every 🔧 row above. Temporary sites, databases, FTP users and
  files are removed afterwards.
- Every replaced action returns `{"status": false, "msg": "指定参数无效!"}` through the API, the
  panel's response to an unknown action: `StopSite`, `getPhpVersion`, `SetPhpVersion`, `getRewrite`,
  `getConf`, `GetSiteStatus`, `GetPHPModules`, `GetSSLCertList`, `RenameFile`, `MoveFile`,
  `Compress`, `UnCompress`, `ChangeDBPassword`.
- `SiteStart` with only `{id}` returns HTTP 404; `SiteStop` accepts `{id}` alone as well as
  `{id, name}`.
