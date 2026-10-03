# Task 012: 集成测试模块覆盖补全

## 状态

已完成。

## 背景

Task 011 补齐了集成测试基础设施，并让现有测试在配置不完整或 IP 白名单受限时能够稳定跳过。
但当时只覆盖了 `database`、`file`、`website` 与部分 `system` 场景，仍存在以下缺口：

- `ftp` 模块缺少真实集成测试
- `ssl` 模块缺少真实集成测试
- `system` 模块其余公开接口缺少集成覆盖
- 示例配置与测试文档未体现 FTP / SSL 的执行约束

## 目标

- 为 `ftp` 模块补齐创建、改密、删除、列表查询的真实集成测试
- 为 `ssl` 模块补齐证书列表、安装、删除的真实集成测试
- 为 `system` 模块补齐模块级集成测试覆盖
- 保持“缺配置时可跳过、配置齐备时可直接执行”的测试体验

## 实施内容

- 新增 `FtpIntegrationTest`
  - 覆盖 FTP 账号列表查询
  - 覆盖 FTP 账号创建
  - 覆盖 FTP 密码修改
  - 覆盖 FTP 账号删除
  - 支持 `BT_PANEL_TEST_FTP_ROOT`
  - 当未配置 FTP 专用根目录时，自动回退到 `test.webroot` 或 `test.filePath` 的父目录

- 新增 `SslIntegrationTest`
  - 覆盖 SSL 证书列表查询
  - 覆盖 SSL 证书安装
  - 覆盖 SSL 证书删除
  - 自动创建并清理测试站点资源
  - 内置仅用于测试的自签名证书和私钥资源
  - 对齐当前面板 SSL 路由行为：
    - 列表查询：`ssl?action=get_cert_list`
    - 安装证书：`site?action=SetSSL`
    - 删除证书：`ssl?action=remove_cloud_cert`
  - 测试匹配逻辑改为按证书指纹 / 证书域名（CN/SAN）识别安装结果，避免将随机站点域名误当作证书域名

- 新增 `SystemIntegrationTest`
  - 覆盖 `GetSystemInfoApi`
  - 覆盖 `GetDiskInfoApi`
  - 覆盖 `GetNetworkStatusApi`
  - 覆盖 `GetTaskCountApi`
  - 覆盖 `CheckPanelUpdateApi`

- 扩展 `AbstractIntegrationTestSupport`
  - 增加 FTP 配置常量
  - 增加父目录解析辅助方法
  - 增加测试资源文本加载辅助方法

- 更新 `application-test.properties.example`
  - 补充 `test.ftpRoot`
  - 明确 SSL 测试复用网站配置

- 更新 `docs/testing.md`
  - 补齐 FTP 与 SSL 集成测试说明
  - 明确当前集成测试覆盖范围
  - 增加 SSL 集成测试的真实行为说明（站点名与证书域名的区别）

## 验收标准

- `database`、`file`、`ftp`、`ssl`、`system`、`website` 六个模块均有集成测试类
- `system` 模块全部公开 API 均有对应集成测试覆盖
- FTP 与 SSL 测试在环境不足时可明确跳过并给出原因
- SSL 测试不再依赖“证书域名必须等于随机站点域名”这一错误假设
- 示例配置与文档可指导本地或 CI 执行完整集成测试