# Task 006: FTP 模块契约化重构与测试补齐

## 状态
已完成

## 背景

`ftp` 模块此前存在以下问题：

- 3 个布尔型 API 分别维护近似相同的 `status/msg` 解析逻辑
- `GetFtpAccountsApi` 对成功、失败和缺失 `data` 的返回契约不够清晰
- `FtpOperations` 与 `BtApiManager` 缺少 FTP 门面委托测试
- `api/ftp` 目录此前没有独立的单元测试覆盖

## 目标

- 统一 FTP 布尔型接口的响应解析逻辑
- 明确 FTP 列表接口对成功、失败与缺失载荷的判定规则
- 补齐 API 层、门面层与 manager 层的核心测试覆盖

## 实施内容

- 新增 `AbstractFtpBooleanApi`，统一处理标准 `status/msg` JSON 响应
- 重写以下 API：
  - `CreateFtpAccountApi`
  - `DeleteFtpAccountApi`
  - `ChangeFtpPasswordApi`
  - `GetFtpAccountsApi`
- 调整 `FtpOperations`，统一采用 fluent setter 风格构造请求
- 新增测试：
  - `FtpBooleanApisTest`
  - `GetFtpAccountsApiTest`
  - `FtpOperationsTest`
  - `BtApiManagerTest` 中的 FTP facade 委托覆盖

## 验收标准

- FTP 布尔型 API 统一复用公共响应解析逻辑
- `GetFtpAccountsApi` 能正确区分成功列表、失败包装响应与缺失 `data` 的异常场景
- `ftp` 模块 API、门面与 manager 层通过单元测试覆盖主要路径
- 主构建与单元测试验证通过
