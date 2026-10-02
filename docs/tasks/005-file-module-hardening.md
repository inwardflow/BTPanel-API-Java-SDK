# Task 005: File 模块契约化重构与测试补齐

## 状态

已完成

## 背景

`file` 模块中的多个 API 仍停留在旧式实现：

- 7 个布尔型 API 重复维护几乎相同的 JSON 解析逻辑
- `GetFileContentApi` 会把 JSON 文件内容误判成包装响应
- `FileOperations` 几乎没有独立门面测试
- `api/file` 包此前没有单元测试目录

## 目标

- 收敛文件模块重复逻辑，统一布尔型响应解析
- 明确文件内容接口对“原始内容”和“包装 JSON”的判定规则
- 补齐 API 层与门面层测试

## 实施内容

- 新增 `AbstractFileBooleanApi`，统一处理标准 `status/msg` 响应
- 重写以下 API：
  - `CreateFileDirectoryApi`
  - `DeleteFileApi`
  - `RenameFileApi`
  - `MoveFileApi`
  - `SaveFileContentApi`
  - `CompressFileApi`
  - `UncompressFileApi`
  - `GetFileContentApi`
- 新增测试：
  - `FileBooleanApisTest`
  - `GetFileContentApiTest`
  - `FileOperationsTest`

## 验收标准

- 文件布尔型接口统一复用公共响应解析逻辑
- JSON 文件内容不会被误判为包装响应
- `file` 模块 API 与门面层通过单元测试覆盖主要路径
- 主构建与单元测试通过
