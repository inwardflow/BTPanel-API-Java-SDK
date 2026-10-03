# Task 008: Website 查询接口契约化重构与测试补齐

## 状态
已完成

## 背景

`website` 模块此前虽然已经有 `GetWebsitesApi` 单测，但其余基础查询接口仍存在以下问题：

- `GetWebsiteListApi`、`GetWebsiteDetailApi`、`GetWebsiteDomainsApi`、`GetWebsiteTypesApi`、`GetPhpVersionsApi` 的响应契约不一致
- 失败包装响应、缺失 `data`、直接数组响应等场景处理方式分散
- `GetWebsiteListApi` 的参数校验存在布尔优先级缺陷，可能误判分页参数
- `WebsiteOperations` 对这批查询型方法没有独立门面测试

## 目标

- 统一 website 查询接口对数组响应、包装响应与失败响应的判定规则
- 修复原始网站列表接口的参数校验缺陷
- 补齐 API 层与门面层的核心测试覆盖

## 实施内容

- 新增 `WebsiteApiResponseSupport`，统一处理：
  - JSON 解析
  - 失败结果构建
  - `JSONObject/JSONArray` 到 `Map/List` 的递归转换
- 重写以下查询接口：
  - `GetWebsiteListApi`
  - `GetWebsiteDetailApi`
  - `GetWebsiteDomainsApi`
  - `GetWebsiteTypesApi`
  - `GetPhpVersionsApi`
- 新增测试：
  - `GetWebsiteListApiTest`
  - `GetWebsiteDetailApiTest`
  - `GetWebsiteDomainsApiTest`
  - `GetWebsiteTypesApiTest`
  - `GetPhpVersionsApiTest`
  - `WebsiteOperationsTest`

## 验收标准

- website 查询接口对直接数组、包装成功、包装失败与缺失载荷场景有明确一致的处理
- `GetWebsiteListApi` 参数校验在缺省分页参数场景下不再误判
- `WebsiteOperations` 的查询型门面方法通过单元测试覆盖主要路径
- 主构建与单元测试验证通过
