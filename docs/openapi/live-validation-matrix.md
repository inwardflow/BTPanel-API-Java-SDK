# BT Panel Live Validation Matrix (2026-03-29)

Environment:
- Base URL: `https://your-panel-host:port`
- Auth mode: developer API signature (`request_time` + `request_token`)
- Rule: temporary resources are created and then deleted

## PASS

- `POST /system?action=GetSystemTotal`
- `POST /datalist/data/get_data_list` (`table=ftps`)
- `POST /files?action=CreateDir`
- `POST /ftp?action=AddUser`
- `POST /ftp?action=SetUser`
- `POST /ftp?action=DeleteUser`
- `POST /files?action=DeleteDir`
- `POST /datalist/data/get_data_list` (`table=databases`)
- `POST /database` (`action=AddDatabase`)
- `POST /database?action=DeleteDatabase`
- `POST /site?action=AddSite`
- `POST /datalist/data/get_data_list` (`table=sites`)
- `POST /site?action=DeleteSite`
- `POST /ssl?action=get_cert_list`
- `POST /site?action=GetSSL`
- `POST /ssl?action=get_order_list`
- `POST /ssl?action=GetSiteDomain`
- `POST /ssl?action=SetBatchCertToSite`

## FAIL

- `POST /ftp?action=AddFtp`
  - Result: `{"status":false,"msg":"指定参数无效!"}`
- `POST /ssl?action=getData`
  - Result: `{"status":false,"msg":"指定参数无效!"}`
- `POST /datalist/data/get_data_list` (`table=ssl`)
  - Result: HTTP `404`
- `POST /database?action=ChangeDBPassword`
  - Result: `{"status":false,"msg":"指定参数无效!"}`
- `POST /site?action=GetSSLCertList`
  - Result: `{"status":false,"msg":"指定参数无效!"}`
- `POST /site?action=CloseSSL`
  - Result: `{"status":false,"msg":"指定参数无效!"}`
- `POST /site?action=SetSSL`
  - Result: HTTP `404`
- `POST /ssl?action=SaveSSL`
  - Result: HTTP `404`
- `POST /ssl/cert/get_cert_list`
  - Result: `{"status":false,"msg":"没有在模型中找到指定模块"}`

## SKIP (dependent on failed prerequisite)

- `POST /ftp?action=ChangeFtpPassword` (legacy flow)
- `POST /ftp?action=DeleteFtp` (legacy flow)

## Notes

- FTP legacy actions (`AddFtp/ChangeFtpPassword/DeleteFtp`) are not reliable on this panel; use `AddUser/SetUser/DeleteUser`.
- SSL global list is available via `POST /ssl?action=get_cert_list` and currently returns a JSON array instead of a `{status,data}` wrapper.
- Site-level SSL state/query + deployment chain is available via `GetSSL -> GetSiteDomain -> SetBatchCertToSite`.
- Site-level legacy candidates (`GetSSLCertList`, `CloseSSL`, `SetSSL`) should not be marked as validated on this panel version.
- Database password change action appears changed or session-bound on this panel version.
- Full raw run details are in [live-validation-report.json](/E:/Stable/BTPanel-API-Java-SDK/docs/openapi/live-validation-report.json).
- Dedicated SSL probe details are in [ssl-live-validation-2026-03-29.json](/E:/Stable/BTPanel-API-Java-SDK/docs/openapi/ssl-live-validation-2026-03-29.json).
- DevTools capture + signature revalidation details are in [ssl-devtools-capture-validation-2026-03-29.json](/E:/Stable/BTPanel-API-Java-SDK/docs/openapi/ssl-devtools-capture-validation-2026-03-29.json).
