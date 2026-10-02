# 宝塔面板开发者 API 文档

> 文档状态：持续维护  
> 最后校验日期：2026-03-29  
> 目标面板版本：BTPanel Linux 9.0.0  
> 所有示例均使用脱敏域名、占位密钥和占位参数

## 1. 文档范围

本文只讨论宝塔面板“开发者 API”这一条主线：

- 认证方式：`request_time + request_token`
- 访问入口：面板根地址，例如 `https://your-panel-host:port`
- 主要用途：SDK、服务端集成、自动化脚本、Apifox/OpenAPI 导入

本文不把以下内容当作主线能力说明：

- 登录态 `Cookie`
- `x-http-token`
- 前端页面自己的状态初始化请求

页面抓包仍然非常有价值，但它的用途是“发现接口”，不是替代开发者 API。最佳实践是：

1. 先在 DevTools 中观察页面真实调用。
2. 再用开发者 API 的签名方式复测。
3. 只有复测通过的接口，才标记为“已验证可用”。

## 2. 开发者 API 基础约定

### 2.1 Base URL

开发者 API 的 `baseUrl` 应该是面板根地址，而不是安全入口路径。

正确示例：

```text
https://your-panel-host:port
```

错误示例：

```text
https://your-panel-host:port/panel-entry
```

### 2.2 认证参数

开发者 API 主要使用两个参数：

- `request_time`
- `request_token`

计算公式：

```text
request_token = md5(String(request_time) + md5(apiKey))
```

Java 示例：

```java
long requestTime = System.currentTimeMillis() / 1000;
String requestToken = BtUtils.generateRequestToken(apiKey, requestTime);
```

安全要求：

- `apiKey` 不得写入示例文档、提交记录或公开仓库。
- `request_time` 和 `request_token` 也视为敏感信息，不应出现在公开示例中。
- 示例中统一使用 `<unix_timestamp>`、`<request_token>`、`<api-key>` 之类占位符。

### 2.3 请求格式

当前面板实测的主流调用方式如下：

- 方法：`POST`
- 参数编码：`application/x-www-form-urlencoded`
- 业务动作：通常通过 `?action=...` 指定
- 列表接口：新面板大量使用 `/datalist/data/get_data_list` 配合 `table=...`

### 2.4 TLS 说明

当前验证面板使用的证书链不是公共信任链。直接使用标准 HTTPS 客户端时，往往会先遇到 TLS 校验失败，而不是 API 认证失败。

因此测试环境通常需要显式控制：

- Java SDK：`verifySsl=false`
- 脚本探测：使用允许自签证书的模式

相关说明见 [testing.md](/E:/Stable/BTPanel-API-Java-SDK/docs/testing.md)。

## 3. 当前验证结论

### 3.1 已验证可用

- `POST /system?action=GetSystemTotal`
- `POST /system?action=GetDiskInfo`
- `POST /system?action=GetNetWork`
- `POST /ajax?action=GetTaskCount`
- `POST /site?action=get_site_types`
- `POST /datalist/data/get_data_list` with `table=ftps`
- `POST /ftp?action=AddUser`
- `POST /ftp?action=SetUser`
- `POST /ftp?action=DeleteUser`
- `POST /files?action=CreateDir`
- `POST /files?action=DeleteDir`
- `POST /datalist/data/get_data_list` with `table=databases`
- `POST /database` with `action=AddDatabase`
- `POST /database?action=DeleteDatabase`
- `POST /site?action=AddSite`
- `POST /datalist/data/get_data_list` with `table=sites`
- `POST /site?action=DeleteSite`
- `POST /ssl?action=get_cert_list`
- `POST /site?action=GetSSL`
- `POST /ssl?action=get_order_list`
- `POST /ssl?action=GetSiteDomain`
- `POST /ssl?action=SetBatchCertToSite`

### 3.2 已验证失败或不推荐

- `POST /ftp?action=AddFtp`
- `POST /ftp?action=ChangeFtpPassword`
- `POST /ftp?action=DeleteFtp`
- `POST /ssl?action=getData`
- `POST /datalist/data/get_data_list` with `table=ssl`
- `POST /database?action=ChangeDBPassword`
- `POST /site?action=GetSSLCertList`
- `POST /site?action=CloseSSL`
- `POST /site?action=SetSSL`
- `POST /ssl?action=SaveSSL`
- `POST /ssl/cert/get_cert_list`

### 3.3 重点结论

- FTP 模块在当前面板上应优先使用 `AddUser / SetUser / DeleteUser`，而不是旧的 `AddFtp / ChangeFtpPassword / DeleteFtp`。
- 数据库模块的“列表、新建、删除”已验证通过，但“修改密码”这条旧接口目前仍返回“指定参数无效”。
- SSL 模块当前真正可用的站点链路，不是历史上的 `SetSSL / SaveSSL`，而是：

```text
GetSSL -> get_order_list -> GetSiteDomain -> SetBatchCertToSite
```

## 4. 模块文档

### 4.1 System

#### 获取系统总览

- 接口：`POST /system?action=GetSystemTotal`
- 认证：开发者 API 签名
- 状态：已验证可用

请求示例：

```http
POST /system?action=GetSystemTotal
Content-Type: application/x-www-form-urlencoded

request_time=<unix_timestamp>
request_token=<request_token>
```

响应示例：

```json
{
  "memTotal": 1882,
  "memRealUsed": 1067,
  "cpuNum": 2,
  "cpuRealUsed": 5.6,
  "system": "Ubuntu 22.04.5 LTS (Jammy Jellyfish) x86_64(Py3.7.8)",
  "version": "9.0.0",
  "time": "236天"
}
```

测试用例：

| 用例 | 输入 | 预期输出 |
| --- | --- | --- |
| 获取系统总览 | 仅传签名参数 | HTTP 200，返回 `system`、`version`、`cpuRealUsed` 等字段 |

### 4.2 FTP

#### 获取 FTP 列表

- 接口：`POST /datalist/data/get_data_list`
- 关键参数：`table=ftps`
- 状态：已验证可用

请求参数示例：

```text
table=ftps
p=1
limit=100
search=
```

#### 创建 FTP 账户

- 接口：`POST /ftp?action=AddUser`
- 状态：已验证可用

请求参数示例：

```text
ftp_username=demoftp
ftp_password=<ftp-password>
path=/www/wwwroot/demoftp
ps=demoftp
```

响应示例：

```json
{
  "status": true,
  "msg": "添加成功"
}
```

#### 修改 FTP 账户

- 接口：`POST /ftp?action=SetUser`
- 状态：已验证可用

请求参数示例：

```text
id=22
ftp_username=demoftp
new_password=<new-password>
path=/www/wwwroot/demoftp
```

#### 删除 FTP 账户

- 接口：`POST /ftp?action=DeleteUser`
- 状态：已验证可用

请求参数示例：

```text
id=22
username=demoftp
```

#### 当前不推荐的旧接口

- `POST /ftp?action=AddFtp`
- `POST /ftp?action=ChangeFtpPassword`
- `POST /ftp?action=DeleteFtp`

当前复测结果中，旧接口至少会出现以下问题之一：

- 返回 `{"status":false,"msg":"指定参数无效!"}`
- 无法与页面当前真实写入链路对齐

测试用例：

| 用例 | 输入 | 预期输出 |
| --- | --- | --- |
| 新建 FTP 成功 | `ftp_username`、`ftp_password`、`path`、`ps` | `status=true` |
| 修改 FTP 成功 | `id`、`ftp_username`、`new_password`、`path` | `status=true` |
| 删除 FTP 成功 | `id`、`username` | `status=true` |
| 旧接口 AddFtp 复测 | `name`、`password`、`path` | `status=false`，`msg=指定参数无效!` |

### 4.3 Database

#### 获取数据库列表

- 接口：`POST /datalist/data/get_data_list`
- 关键参数：`table=databases`
- 状态：已验证可用

请求参数示例：

```text
table=databases
p=1
limit=200
search=
order=
```

#### 创建数据库

- 接口：`POST /database`
- 关键参数：`action=AddDatabase`
- 状态：已验证可用

请求参数示例：

```text
action=AddDatabase
name=test_db
db_user=test_user
password=<db-password>
codeing=utf8mb4
dataAccess=127.0.0.1
address=127.0.0.1
dtype=MySQL
ps=test_db
sid=0
listen_ip=0.0.0.0/0
host=%
```

#### 删除数据库

- 接口：`POST /database?action=DeleteDatabase`
- 状态：已验证可用

请求参数示例：

```text
name=test_db
id=71
```

#### 修改数据库密码

- 接口：`POST /database?action=ChangeDBPassword`
- 状态：已验证失败

当前复测：

```json
{
  "status": false,
  "msg": "指定参数无效!"
}
```

测试用例：

| 用例 | 输入 | 预期输出 |
| --- | --- | --- |
| 新建数据库成功 | `action=AddDatabase` + 标准参数 | `status=true` |
| 删除数据库成功 | `name` + `id` | `status=true` |
| 修改数据库密码 | `name` + `username` + `password` | 当前返回 `status=false` |

### 4.4 Site

#### 获取站点列表

- 接口：`POST /datalist/data/get_data_list`
- 关键参数：`table=sites`
- 状态：已验证可用

请求参数示例：

```text
table=sites
p=1
limit=200
search=example.com
type=-1
order=
```

#### 创建站点

- 接口：`POST /site?action=AddSite`
- 状态：已验证可用

请求参数示例：

```text
webname={"domain":"example.com","domainlist":[],"count":0}
path=/www/wwwroot/example.com
type_id=0
type=PHP
version=81
port=80
ps=example.com
ftp=0
sql=0
```

#### 删除站点

- 接口：`POST /site?action=DeleteSite`
- 状态：已验证可用

请求参数示例：

```text
id=208
webname=example.com
ftp=0
database=0
path=1
```

测试用例：

| 用例 | 输入 | 预期输出 |
| --- | --- | --- |
| 新建 PHP 站点 | `webname`、`path`、`type=PHP`、`version=81`、`ftp=0`、`sql=0` | `siteStatus=true` |
| 删除站点并删除目录 | `id`、`webname`、`path=1` | `status=true` |

### 4.5 SSL

#### 全局证书列表

- 接口：`POST /ssl?action=get_cert_list`
- 状态：已验证可用
- 重要说明：当前返回的是 JSON 数组，不是历史上的 `{status,data}` 包装对象

请求参数示例：

```text
table=ssl
p=1
limit=100
search=
```

响应示例：

```json
[
  {
    "id": 14,
    "hash": "cac030df9fd0ce86c411095827e97ac4",
    "subject": "integration-test.example.com",
    "dns": [
      "integration-test.example.com"
    ],
    "use_for_panel": 0,
    "use_for_site": []
  }
]
```

#### 查询站点 SSL 状态

- 接口：`POST /site?action=GetSSL`
- 状态：已验证可用
- 说明：`status=false` 表示站点当前未部署证书，不代表接口不可用

请求参数示例：

```text
siteName=example.com
```

#### 查询站点关联订单

- 接口：`POST /ssl?action=get_order_list`
- 状态：已验证可用

请求参数示例：

```text
siteName=example.com
```

#### 计算证书可部署站点

- 接口：`POST /ssl?action=GetSiteDomain`
- 状态：已验证可用

请求参数示例：

```text
cert_list=["integration-test.example.com"]
```

#### 将证书部署到站点

- 接口：`POST /ssl?action=SetBatchCertToSite`
- 状态：已验证可用
- 说明：这是当前 BTPanel 9.0.0 面板上已验证通过的 SSL 写接口

请求参数示例：

```text
BatchInfo=[{"ssl_hash":"<ssl-hash>","siteName":"example.com","certName":"integration-test.example.com"}]
```

响应示例：

```json
{
  "total": 1,
  "success": 1,
  "faild": 0,
  "successList": [
    {
      "status": true,
      "certName": "integration-test.example.com",
      "siteName": "example.com"
    }
  ],
  "faildList": []
}
```

#### 当前已验证失败的 SSL 候选接口

- `POST /ssl?action=getData`
  - 返回：`{"status":false,"msg":"指定参数无效!"}`
- `POST /datalist/data/get_data_list` with `table=ssl`
  - 返回：HTTP `404`
- `POST /site?action=GetSSLCertList`
  - 返回：`{"status":false,"msg":"指定参数无效!"}`
- `POST /site?action=CloseSSL`
  - 返回：`{"status":false,"msg":"指定参数无效!"}`
- `POST /site?action=SetSSL`
  - 返回：HTTP `404`
- `POST /ssl?action=SaveSSL`
  - 返回：HTTP `404`
- `POST /ssl/cert/get_cert_list`
  - 返回：`{"status":false,"msg":"没有在模型中找到指定模块"}`

测试用例：

| 用例 | 输入 | 预期输出 |
| --- | --- | --- |
| 查询全局证书列表 | `table=ssl`、`p=1`、`limit=100` | HTTP 200，返回 JSON 数组 |
| 查询站点 SSL 状态 | `siteName=<站点名>` | 未部署时 `status=false`，已部署时 `status=true` |
| 计算可部署站点 | `cert_list=["integration-test.example.com"]` | 返回 `all` 和 `site` |
| 部署已保存证书 | `BatchInfo=[...]` | HTTP 200，`success=1` |
| 旧接口 SetSSL 复测 | 传历史 PEM 参数 | HTTP `404` |

## 5. SDK 对齐说明

当前 SDK 已对齐到最新实测链路，推荐用法如下：

```java
import java.util.List;

import net.heimeng.sdk.btapi.model.ssl.SslBatchDeploymentResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;
import net.heimeng.sdk.btapi.model.ssl.SslDeployableSites;
import net.heimeng.sdk.btapi.model.ssl.SslSiteStatus;

String siteName = "example.com";

SslCertificate cert =
    apiManager.ssl().list().getData().stream()
        .filter(item -> "integration-test.example.com".equals(item.getName()))
        .findFirst()
        .orElseThrow();

SslSiteStatus status = apiManager.ssl().getWebsiteStatus(siteName).getData();
SslDeployableSites deployableSites =
    apiManager.ssl().getDeployableSites(List.of(cert.getName())).getData();
SslBatchDeploymentResult result =
    apiManager.ssl()
        .deploySavedCertificate(cert.getHash(), siteName, cert.getName())
        .getData();
```

说明：

- `SslCertificate.hash` 对应面板中的 `ssl_hash`
- `SslCertificate.name` 通常可直接作为 `certName`
- `apiManager.ssl().install(...)` 仍保留为兼容入口，但不再是当前面板版本的首选路径

## 6. OpenAPI、测试与示例

主要文档产物：

- 主 OpenAPI：[btpanel-developer-api.openapi-3.1.json](/E:/Stable/BTPanel-API-Java-SDK/docs/openapi/btpanel-developer-api.openapi-3.1.json)
- Strict OpenAPI：[btpanel-developer-api.strict.openapi-3.1.json](/E:/Stable/BTPanel-API-Java-SDK/docs/openapi/btpanel-developer-api.strict.openapi-3.1.json)
- 观察稿：[btpanel-developer-api.observed.openapi-3.1.json](/E:/Stable/BTPanel-API-Java-SDK/docs/openapi/btpanel-developer-api.observed.openapi-3.1.json)
- 验证矩阵：[live-validation-matrix.md](/E:/Stable/BTPanel-API-Java-SDK/docs/openapi/live-validation-matrix.md)

配套说明：

- 测试说明：[testing.md](/E:/Stable/BTPanel-API-Java-SDK/docs/testing.md)
- 快速示例：[quickstart.md](/E:/Stable/BTPanel-API-Java-SDK/docs/examples/quickstart.md)

## 7. 安全与脱敏规范

为了避免泄露面板访问能力，以下信息都不应直接出现在公开文档、截图、Issue、PR 和仓库提交中：

- 面板真实地址
- 面板安全入口路径
- API Key
- `request_time`
- `request_token`
- 真实证书私钥
- 登录态 Cookie 和会话 Token

如果必须展示请求示例，请统一使用以下形式：

```text
https://your-panel-host:port
request_time=<unix_timestamp>
request_token=<request_token>
apiKey=<api-key>
```
