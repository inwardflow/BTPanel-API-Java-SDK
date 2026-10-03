# Task 009: Website 配置与文本查询接口契约化重构

## 状态
已完成

## 背景

`website` 模块中仍有一批读取型接口保持旧式实现：

- 文本类接口对“原始文本响应”和“包装 JSON 响应”的兼容策略不一致
- 配置类接口对原始对象响应和失败包装响应的处理方式分散
- `WebsiteOperations` 对这批方法缺少独立门面测试

## 目标

- 统一 website 文本类读取接口的响应解析逻辑
- 统一 website 配置类接口对原始对象与失败包装响应的判定规则
- 补齐 API 层与门面层测试

## 实施内容

- 新增 `AbstractWebsiteTextQueryApi`，统一处理：
  - 包装 JSON 响应 `status/msg/data`
  - 原始文本响应
  - 非包装 JSON 文本内容
- 增强 `WebsiteApiResponseSupport`，补充字符串结果构建与通用辅助方法
- 重写以下接口：
  - `GetWebsiteConfigApi`
  - `GetWebsiteLimitNetApi`
  - `GetWebsiteRootPathApi`
  - `GetWebsitePhpVersionApi`
  - `GetWebsiteRewriteRulesApi`
  - `GetWebsiteNginxConfigApi`
- 新增测试：
  - `WebsiteTextQueryApisTest`
  - `GetWebsiteConfigApiTest`
  - `GetWebsiteLimitNetApiTest`
  - 扩展 `WebsiteOperationsTest`

## 验收标准

- 文本类接口对原始文本和包装 JSON 的处理规则一致
- 配置类接口能正确处理原始对象响应、包装成功和包装失败
- `WebsiteOperations` 对配置与文本查询方法具备单元测试覆盖
- 主构建与单元测试验证通过
