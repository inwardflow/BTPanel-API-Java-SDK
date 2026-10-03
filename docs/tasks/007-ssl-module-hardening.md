# Task 007: SSL 模块契约化重构与测试补齐

## 状态
已完成

## 背景

`ssl` 模块此前存在以下问题：

- 2 个布尔型 API 各自维护重复的 `status/msg` 解析逻辑
- `GetSslCertificatesApi` 对成功、失败和缺失 `data` 的响应契约不够清晰
- `SslOperations`、`BtApiManager` 与模型辅助方法缺少独立测试覆盖

## 目标

- 统一 SSL 布尔型接口的响应解析逻辑
- 明确证书列表接口对成功、失败与缺失载荷的判定规则
- 补齐 API 层、门面层、manager 层与模型层的核心测试覆盖

## 实施内容

- 新增 `AbstractSslBooleanApi`，统一处理标准 `status/msg` JSON 响应
- 重写以下 API：
  - `InstallSslCertificateApi`
  - `DeleteSslCertificateApi`
  - `GetSslCertificatesApi`
- 调整 `SslOperations`，统一采用 fluent setter 风格构造请求
- 新增测试：
  - `SslBooleanApisTest`
  - `GetSslCertificatesApiTest`
  - `SslOperationsTest`
  - `SslCertificateTest`
  - `BtApiManagerTest` 中的 SSL facade 委托覆盖

## 验收标准

- SSL 布尔型 API 统一复用公共响应解析逻辑
- `GetSslCertificatesApi` 能正确区分成功列表、失败包装响应与缺失 `data` 的异常场景
- `ssl` 模块 API、门面、manager 与模型辅助方法通过单元测试覆盖主要路径
- 主构建与单元测试验证通过
