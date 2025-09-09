```markdown
# 宝塔 Linux面板 API文档

## 前言
通过宝塔 API，可以完全控制宝塔 Linux面板的所有功能，包括第三方插件应用功能。事实上，用户登录面板后使用的所有功能也是通过相同的接口对接的。这意味着，如果你熟悉使用浏览器调试器，就可以轻松对照宝塔 Linux面板的操作参数完成一个第三方的前端对接。

## 签名算法
*   `api_sk` = 接口密钥 (在面板设置页面 - API接口中获取)
*   `request_time` = 当前请求时间的 unix 时间戳 (php: `time()` / python: `time.time()`)
*   `request_token` = `md5(string(request_time) + md5(api_sk))`

**PHP示例：**
```php
$request_token = md5($request_time . '' . md5($api_sk));
```

**签名参数：**

| 参数名称      | 参数值                                      | 说明                     |
| ------------- | ------------------------------------------- | ------------------------ |
| request_time  |                                             | 当前 unix 时间戳 [必传]  |
| request_token | md5(string(request_time)+ md5(api_sk))      | [必传]                   |
| 其它参数      | 功能接口需要的其它参数                      | [可选]                   |

**注意事项：**
1.  请统一使用 POST 方式请求 API 接口。
2.  为了确保请求效率，请保存 cookie，并在每次请求时附上 cookie。
3.  为了面板安全考虑，请务必添加 IP 白名单。
4.  所有响应内容统一为 Json 数据格式。

**DEMO下载**
*   PHP-Demo: https://www.bt.cn/api_demo_php.zip

---

## 系统状态相关接口

### 获取系统基础统计
*   **URI地址：** `/system?action=GetSystemTotal`
*   **传入参数：** 无

**响应内容：**

| 字段          | 字段值示例                 | 说明                 |
| ------------- | -------------------------- | -------------------- |
| system        | CentOS Linux 7.5.1804(Core) | 操作系统信息         |
| version       | 6.8.2                      | 面板版本             |
| time          | 0天 23小时 45分钟          | 上次开机到现在经过的时间 |
| cpuNum        | 2                          | CPU核心数            |
| cpuRealUsed   | 2.01                       | CPU使用率(百分比)    |
| memTotal      | 1024                       | 物理内存容量(MB)     |
| memRealUsed   | 300                        | 已使用的物理内存(MB) |
| memFree       | 724                        | 可用的物理内存(MB)   |
| memCached     | 700                        | 缓存化的内存(MB)     |
| memBuffers    | 100                        | 系统缓冲(MB)         |

**响应内容示例：**
```json
{
  "cpuRealUsed": 0.85,
  "memTotal": 1741,
  "system": "CentOS Linux 7.5.1804(Core)",
  "memRealUsed": 691,
  "cpuNum": 6,
  "memFree": 189,
  "version": "6.8.1",
  "time": "0天23小时57分钟",
  "memCached": 722,
  "memBuffers": 139,
  "isuser": 0
}
```

### 获取磁盘分区信息
*   **URI地址：** `/system?action=GetDiskInfo`
*   **传入参数：** 无

**响应内容：**

| 字段        | 字段值示例                          | 说明                                       |
| ----------- | ----------------------------------- | ------------------------------------------ |
| [].path     | /                                   | 分区挂载点                                 |
| [].inodes   | ["8675328","148216","8527112","2%"] | 分区 Inode使用信息[总量,已使用,可用,使用率] |
| [].size     | ["8.3G","4.0G","4.3G","49%"]        | 分区容量使用信息[总量,已使用,可用,使用率]   |

**响应内容示例：**
```json
[
  {
    "path": "/",
    "inodes": [
      "8675328",
      "148216",
      "8527112",
      "2%"
    ],
    "size": [
      "8.3G",
      "4.0G",
      "4.3G",
      "49%"
    ]
  },
  {
    "path": "/www",
    "inodes": [
      "655360",
      "295093",
      "360267",
      "46%"
    ],
    "size": [
      "9.8G",
      "3.7G",
      "5.6G",
      "40%"
    ]
  }
]
```

### 获取实时状态信息(CPU、内存、网络、负载)
*   **URI地址：** `/system?action=GetNetWork`
*   **传入参数：** 无

**响应内容：**

| 字段         | 字段值示例                                   | 说明                                  |
| ------------ | -------------------------------------------- | ------------------------------------- |
| downTotal    | 446326699                                    | 总接收(字节数)                        |
| upTotal      | 77630707                                     | 总发送(字节数)                        |
| downPackets  | 1519428                                      | 总收包(个)                            |
| upPackets    | 175326                                       | 总发包(个)                            |
| down         | 36.22                                        | 下行流量(KB)                          |
| up           | 72.81                                        | 上行流量(KB)                          |
| cpu          | [1.87, 6]                                    | CPU实时信息[使用率,核心数]            |
| mem          | {memFree: 189, memTotal: 1741, ...}          | 内存实时信息                          |
| load         | {max: 12, safe: 9, one: 0, five: 0.01, ...} | 负载实时信息 one: 1分钟 five: 5分钟 fifteen: 10分钟 |

**响应内容示例：**
```json
{
  "load": {
    "max": 12,
    "safe": 9.0,
    "one": 0.01,
    "five": 0.02,
    "limit": 12,
    "fifteen": 0.05
  },
  "down": 8.77,
  "downTotal": 453078627,
  "mem": {
    "memFree": 189,
    "memTotal": 1741,
    "memCached": 722,
    "memBuffers": 140,
    "memRealUsed": 690
  },
  "up": 4.33,
  "upTotal": 78070942,
  "upPackets": 177930,
  "downPackets": 1548192,
  "cpu": [
    0.23,
    6
  ]
}
```

### 检查是否有安装任务
*   **URI地址：** `/ajax?action=GetTaskCount`
*   **传入参数：** 无
*   **响应内容：** `0`

### 检查面板更新
*   **URI地址：** `/ajax?action=UpdatePanel`

**传入参数：**

| 参数名称 | 参数值 | 说明               |
| -------- | ------ | ------------------ |
| check    | true   | 强制检查更新[可选] |
| force    | true   | 执行更新[可选]     |

**响应内容：**

| 字段       | 字段值示例 | 说明       |
| ---------- | ---------- | ---------- |
| status     | true       | 获取状态 true\|false |
| version    | 6.5.8      | 最新版本号 |
| updateMsg  | string     | 升级说明   |

**响应内容示例：**
```json
{
  "status": true,
  "version": "6.8.2",
  "updateMsg": "升级说明"
}
```

---

## 网站管理

### 获取网站列表
*   **URI地址：** `/data?action=getData&table=sites`

**传入参数：**

| 参数名称 | 参数值示例 | 说明                                       |
| -------- | ---------- | ------------------------------------------ |
| p        | 1          | 当前分页[可选]                             |
| limit    | 15         | 取回的数据行数[必传]                       |
| type     | -1         | 分类标识,-1:分部分类 0:默认分类 [可选]     |
| order    | id desc    | 排序规则[可选]                             |
| tojs     | get_site_list | 分页 JS回调[可选]                        |
| search   | www        | 搜索内容[可选]                             |

**响应内容：**

| 字段   | 字段值示例 | 说明         |
| ------ | ---------- | ------------ |
| data   | []         | 网站列表数据 |
| page   |            | 分页数据     |
| where  | type_id=0  | 数据查询条件 |

**响应内容示例：**
```json
{
  "data": [
    {
      "status": "1",
      "ps": "bbb.com",
      "domain": 1,
      "name": "bbb.com",
      "addtime": "2018-12-14 16:14:03",
      "path": "/www/wwwroot/bbb.com",
      "backup_count": 0,
      "edate": "0000-00-00",
      "id": 64
    }
  ],
  "where": "type_id=0",
  "page": "<div><span class='Pcurrent'>1</span><span class='Pcount'>共1条数据</span></div>"
}
```

### 获取网站分类
*   **URI地址：** `/site?action=get_site_types`
*   **传入参数：** 空
*   **响应内容示例：**
```json
[
  {
    "id": 0,
    "name": "默认分类"
  }
]
```

### 获取已安装的 PHP版本列表
*   **URI地址：** `/site?action=GetPHPVersion`
*   **传入参数：** 空
*   **响应内容示例：**
```json
[
  {
    "version": "00",
    "name": "纯静态"
  },
  {
    "version": "56",
    "name": "PHP-56"
  },
  {
    "version": "72",
    "name": "PHP-72"
  }
]
```

### 创建网站
*   **URI地址：** `/site?action=AddSite`

**传入参数：**

| 参数名称       | 参数值示例                                      | 说明                                       |
| -------------- | ----------------------------------------------- | ------------------------------------------ |
| webname        | {"domain":"w1.hao.com","domainlist":[],"count":0} | 网站主域名和域名列表请传 JSON[必传]        |
| path           | /www/wwwroot/w1.hao.com                         | 根目录[必传]                               |
| type_id        | 0                                               | 分类标识[必传]                             |
| type           | PHP                                             | 项目类型请传 PHP[必传]                     |
| version        | 72                                              | PHP版本请从 PHP版本列表中选择[必传]        |
| port           | 80                                              | 网站端口[必传]                             |
| ps             | 测试                                            | 网站备注[必传]                             |
| ftp            | true\|false                                     | 是否创建 FTP[必传]                         |
| ftp_username   | w1_hao_com                                      | FTP用户名在要创建 FTP时必传                |
| ftp_password   | WCBZ6cH87raERzXc                                | FTP密码在要创建 FTP时必传                  |
| sql            | true\|false                                     | 是否创建数据库[必传]                       |
| codeing        | utf8\|utf8mb4\|gbk\|big5                        | 数据库字符集在要创建数据库时必传           |
| datauser       | w1_hao_com                                      | 数据库用户名及名称在要创建数据库时必传     |
| datapassword   | PdbNjJy5hBA346AR                                | 数据库密码在要创建数据库时必传             |

**响应内容：**

| 字段           | 字段值示例   | 说明               |
| -------------- | ------------ | ------------------ |
| siteStatus     | true\|false  | 网站是否创建成功   |
| ftpStatus      | true\|false  | FTP是否创建成功    |
| ftpUser        | w2_hao_com   | FTP用户名          |
| ftpPass        | sRxmY6xCn6zEsFtG | FTP密码          |
| databaseStatus | true\|false  | 数据库是否创建成功 |
| databaseUser   | w2_hao_com   | 数据库用户名和名称 |
| databasePass   | PdbNjJy5hBA346AR | 数据库密码       |

**响应内容示例：**
```json
{
  "ftpStatus": true,
  "databaseUser": "w2_hao_com",
  "databaseStatus": true,
  "ftpUser": "w2_hao_com",
  "databasePass": "PdbNjJy5hBA346AR",
  "siteStatus": true,
  "ftpPass": "sRxmY6xCn6zEsFtG"
}
```

### 删除网站
*   **URI地址：** `/site?action=DeleteSite`

**传入参数：**

| 参数名称 | 参数值 | 说明                                       |
| -------- | ------ | ------------------------------------------ |
| id       | 66     | 网站 ID[必传]                              |
| webname  | w2_hao_com | 网站名称[必传]                           |
| ftp      | 1      | 是否删除关联 FTP [可选]                    |
| database | 1      | 是否删除关联数据库 [可选]                  |
| path     | 1      | 是否删除网站根目录 [可选]                  |

**响应内容：**

| 字段   | 字段值示例 | 说明         |
| ------ | ---------- | ------------ |
| status | true\|false | 是否操作成功 |
| msg    | 删除成功!  | 提示内容     |

**响应内容示例：**
```json
{
  "status": true,
  "msg": "站点删除成功!"
}
```

### 停用网站
*   **URI地址：** `/site?action=SiteStop`

**传入参数：**

| 参数名称 | 参数值     | 说明                 |
| -------- | ---------- | -------------------- |
| id       | 66         | 网站 ID[必传]        |
| name     | w2.hao.com | 网站名称(主域名)[必传] |

**响应内容示例：**
```json
{
  "status": true,
  "msg": "站点删除成功!"
}
```

### 启用网站
*   **URI地址：** `/site?action=SiteStart`

**传入参数：**

| 参数名称 | 参数值     | 说明                 |
| -------- | ---------- | -------------------- |
| id       | 66         | 网站 ID[必传]        |
| name     | w2.hao.com | 网站名称(主域名)[必传] |

**响应内容示例：**
```json
{
  "status": true,
  "msg": "站点已启用"
}
```

### 网站到期时间
*   **URI地址：** `/site?action=SetEdate`

**传入参数：**

| 参数名称 | 参数值       | 说明                             |
| -------- | ------------ | -------------------------------- |
| id       | 66           | 网站 ID[必传]                    |
| edate    | 2019-01-01   | 到期时间永久：0000-00-00[必传]   |

**响应内容示例：**
```json
{
  "status": true,
  "msg": "站点已启用"
}
```

### 修改网站备注
*   **URI地址：** `/data?action=setPs&table=sites`

**传入参数：**

| 参数名称 | 参数值 | 说明         |
| -------- | ------ | ------------ |
| id       | 66     | 网站 ID[必传] |
| ps       | 测试   | 备注内容[必传] |

**响应内容示例：**
```json
{
  "status": true,
  "msg": "修改成功"
}
```

### 获取网站备份列表
*   **URI地址：** `/data?action=getData&table=backup`

**传入参数：**

| 参数名称 | 参数值示例 | 说明                                       |
| -------- | ---------- | ------------------------------------------ |
| p        | 1          | 当前分页[可选]                             |
| limit    | 5          | 每页取回的数据行数[必传]                   |
| type     | 0          | 备份类型,请固定传 0[必传]                  |
| tojs     | get_site_backup | 分页 JS回调[可选]                        |
| search   | 66         | 网站 ID[必传]                              |

**响应内容：**

| 字段   | 字段值示例 | 说明         |
| ------ | ---------- | ------------ |
| data   | []         | 备份列表数据 |
| page   |            | 分页数据     |
| where  | type_id=0  | 数据查询条件 |

**响应内容示例：**
```json
{
  "data": [],
  "where": "pid=65 and type='0'",
  "page": "<div><span class='Pcurrent'>1</span><span class='Pcount'>共0条数据</span></div>"
}
```

### 创建网站备份
*   **URI地址：** `/site?action=ToBackup`

**传入参数：**

| 参数名称 | 参数值 | 说明         |
| -------- | ------ | ------------ |
| id       | 66     | 网站 ID[必传] |

**响应内容示例：**
```json
{
  "status": true,
  "msg": "备份成功!"
}
```

### 删除网站备份
*   **URI地址：** `/site?action=DelBackup`

**传入参数：**

| 参数名称 | 参数值 | 说明             |
| -------- | ------ | ---------------- |
| id       | 121    | 备份列表 ID[必传] |

**响应内容示例：**
```json
{
  "status": true,
  "msg": "删除成功"
}
```

### 获取网站的域名列表
*   **URI地址：** `/data?action=getData&table=domain`

**传入参数：**

| 参数名称 | 参数值示例 | 说明             |
| -------- | ---------- | ---------------- |
| search   | 66         | 网站 ID[必传]    |
| list     | true       | 请固定传 true [必传] |

**响应内容示例：**
```json
[
  {
    "port": 80,
    "addtime": "2018-12-15 16:57:30",
    "pid": 65,
    "id": 73,
    "name": "w1.hao.com"
  }
]
```

### 添加域名
*   **URI地址：** `/site?action=AddDomain`

**传入参数：**

| 参数名称 | 参数值示例       | 说明                                       |
| -------- | ---------------- | ------------------------------------------ |
| id       | 66               | 网站 ID[必传]                              |
| webname  | w2.hao.com       | 网站名称 [必传]                            |
| domain   | w4.hao.com:81    | 要添加的域名:端口, 多个域名用换行符隔开[必传] |

**响应内容示例：**
```json
{
  "status": true,
  "msg": "域名添加成功!"
}
```

### 删除域名
*   **URI地址：** `/site?action=DelDomain`

**传入参数：**

| 参数名称 | 参数值示例 | 说明             |
| -------- | ---------- | ---------------- |
| id       | 66         | 网站 ID[必传]    |
| webname  | w2.hao.com | 网站名称 [必传]  |
| domain   | w4.hao.com | 要被删除的域名[必传] |
| port     | 80         | 该域名的端口[必传] |

**响应内容示例：**
```json
{
  "status": true,
  "msg": "删除成功"
}
```

### 获取可选的预定义伪静态列表
*   **URI地址：** `/site?action=GetRewriteList`

**传入参数：**

| 参数名称  | 参数值示例 | 说明         |
| --------- | ---------- | ------------ |
| siteName  | w2.hao.com | 网站名称[必传] |

**响应内容：**

| 字段     | 字段值示例 | 说明               |
| -------- | ---------- | ------------------ |
| rewrite  | []         | 预定义伪静态列表   |

**响应内容示例：**
```json
{
  "rewrite": [
    "0.当前",
    "EmpireCMS",
    "dabr",
    "dbshop",
    "dedecms",
    "default",
    "discuz",
    "discuzx",
    "discuzx2",
    "discuzx3",
    "drupal",
    "ecshop",
    "emlog",
    "laravel5",
    "maccms",
    "mvc",
    "niushop",
    "phpcms",
    "phpwind",
    "sablog",
    "seacms",
    "shopex",
    "thinkphp",
    "typecho",
    "typecho2",
    "weengine",
    "wordpress",
    "wp2",
    "zblog"
  ]
}
```

### 获取指定预定义伪静态规则内容(获取文件内容)
*   **URI地址：** `/files?action=GetFileBody`

**传入参数：**

| 参数名称 | 参数值示例                                | 说明         |
| -------- | ----------------------------------------- | ------------ |
| path     | /www/server/panel/vhost/rewrite/nginx/名称.conf | 要被获取的文件[必传] |

### 保存伪静态规则内容(保存文件内容)
*   **URI地址：** `/files?action=SaveFileBody`

**传入参数：**

| 参数名称  | 参数值示例                                | 说明         |
| --------- | ----------------------------------------- | ------------ |
| path      | /www/server/panel/vhost/rewrite/nginx/名称.conf | 保存位置[必传] |
| data      |                                           | 规则内容     |
| encoding  | utf-8                                     | 文件编码请固定传 utf-8 |

### 取回指定网站的根目录
*   **URI地址：** `/data?action=getKey&table=sites&key=path`

**传入参数：**

| 参数名称 | 参数值示例 | 说明         |
| -------- | ---------- | ------------ |
| id       | 1          | 网站 ID[必传] |

### 取回防跨站配置/运行目录/日志开关状态/可设置的运行目录列表/密码访问状态
*   **URI地址：** `/site?action=GetDirUserINI`

**传入参数：**

| 参数名称 | 参数值示例           | 说明         |
| -------- | -------------------- | ------------ |
| id       | 1                    | 网站 ID[必传] |
| path     | /www/wwwroot/w1.if22.cn | 网站根目录[必传] |

**响应内容示例：**
```json
{
  "pass": false, //是否设置密码访问
  "logs": true, //是否写访问日志
  "userini": true, //是否设置防跨站
  "runPath": {
    "dirs": [ //可用于设置运行目录的目录列表
      "/"
    ],
    "runPath": "/" //当前运行目录
  }
}
```

### 设置防跨站状态(自动取反)
*   **URI地址：** `/site?action=SetDirUserINI`

**传入参数：**

| 参数名称 | 参数值示例           | 说明         |
| -------- | -------------------- | ------------ |
| path     | /www/wwwroot/w1.if22.cn | 网站根目录[必传] |

### 设置是否写访问日志
*   **URI地址：** `/site?action=logsOpen`

**传入参数：**

| 参数名称 | 参数值示例 | 说明         |
| -------- | ---------- | ------------ |
| id       | 1          | 网站 ID[必传] |

### 修改网站根目录
*   **URI地址：** `/site?action=SetPath`

**传入参数：**

| 参数名称 | 参数值示例           | 说明             |
| -------- | -------------------- | ---------------- |
| id       | 1                    | 网站 ID[必传]    |
| path     | /www/wwwroot/w1.if22.cn | 新的网站根目录[必传] |

### 设置网站运行目录
*   **URI地址：** `/site?action=SetSiteRunPath`

**传入参数：**

| 参数名称 | 参数值示例 | 说明                     |
| -------- | ---------- | ------------------------ |
| id       | 1          | 网站 ID[必传]            |
| runPath  | /public    | 基于网站根目录的运行目录 |

### 设置密码访问
*   **URI地址：** `/site?action=SetHasPwd`

**传入参数：**

| 参数名称 | 参数值示例 | 说明         |
| -------- | ---------- | ------------ |
| id       | 1          | 网站 ID[必传] |
| username | test       | 用户名       |
| password | admin      | 密码         |

### 关闭密码访问
*   **URI地址：** `/site?action=CloseHasPwd`

**传入参数：**

| 参数名称 | 参数值示例 | 说明         |
| -------- | ---------- | ------------ |
| id       | 1          | 网站 ID[必传] |

### 获取流量限制相关配置（仅支持 nginx）
*   **URI地址：** `/site?action=GetLimitNet`

**传入参数：**

| 参数名称 | 参数值示例 | 说明         |
| -------- | ---------- | ------------ |
| id       | 1          | 网站 ID[必传] |

### 开启或保存流量限制配置（仅支持 nginx）
*   **URI地址：** `/site?action=SetLimitNet`

**传入参数：**

| 参数名称   | 参数值示例 | 说明         |
| ---------- | ---------- | ------------ |
| id         | 1          | 网站 ID[必传] |
| perserver  | 300        | 并发限制[必传] |
| perip      | 25         | 单 IP限制[必传] |
| limit_rate | 512        | 流量限制[必传] |

### 关闭流量限制（仅支持 nginx）
*   **URI地址：** `/site?action=CloseLimitNet`

**传入参数：**

| 参数名称 | 参数值示例 | 说明         |
| -------- | ---------- | ------------ |
| id       | 1          | 网站 ID[必传] |

### 取默认文档信息
*   **URI地址：** `/site?action=GetIndex`

**传入参数：**

| 参数名称 | 参数值示例 | 说明         |
| -------- | ---------- | ------------ |
| id       | 1          | 网站 ID[必传] |

### 设置默认文档
*   **URI地址：** `/site?action=SetIndex`

**传入参数：**

| 参数名称 | 参数值示例                                      | 说明                     |
| -------- | ----------------------------------------------- | ------------------------ |
| id       | 1                                               | 网站 ID[必传]            |
| Index    | index.php,index.html,index.htm,default.php,... | 默认文档，每个用逗号隔开[必传] |

### 取网站配置文件内容(获取文件内容)
*   **URI地址：** `/files?action=GetFileBody`

**传入参数：**

| 参数名称 | 参数值示例                            | 说明         |
| -------- | ------------------------------------- | ------------ |
| path     | /www/server/panel/vhost/nginx/名称.conf | 要被获取的文件[必传] |

### 保存网站配置文件(保存文件内容)
*   **URI地址：** `/files?action=SaveFileBody`

**传入参数：**

| 参数名称 | 参数值示例                            | 说明         |
| -------- | ------------------------------------- | ------------ |
| path     | /www/server/panel/vhost/nginx/名称.conf | 保存位置[必传] |
| data     |                                       | 配置文件内容 |
| encoding | utf-8                                 | 文件编码请固定传 utf-8 |