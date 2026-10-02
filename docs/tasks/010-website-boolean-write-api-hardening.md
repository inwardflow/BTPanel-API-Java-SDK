# Task 010: Website 布尔写操作接口契约化重构

## 状态：已完成
## 背景

`website` 模块中的大量写操作接口仍保留旧式实现，主要问题包括：

- 多个 API 分别维护重复的 `status/msg` JSON 解析逻辑
- 参数校验口径不一致，部分接口只判断 `containsKey`，缺少对正整数、非空字符串的约束
- 布尔型参数在不同接口中有的直接传 `Boolean`，有的依赖调用方隐式处理，契约不够明确
- `DeleteWebsiteApi` 等接口对可选删除标志位缺少“取消时移除参数”的收敛处理
- `WebsiteOperations` 对这批写操作缺少独立门面测试，Jacoco 覆盖长期偏低

## 目标

- 为 website 布尔写操作接口建立统一的公共抽象与解析契约
- 收敛参数校验规则，显式区分正整数、非空字符串、允许空字符串和可选参数
- 统一布尔标志位参数的序列化方式，避免请求参数语义分散
- 补齐 API 层与 facade 层单元测试覆盖

## 实施内容

- 新增 `AbstractWebsiteBooleanApi`，统一处理：
  - 标准 `status/msg` JSON 响应解析
  - `id`、字符串、可选数值、布尔标志位等通用校验辅助方法
  - 可选布尔/整数参数的移除与收敛逻辑
- 重构以下布尔写操作 API：
  - `AddWebsiteDomainApi`
  - `DeleteWebsiteDomainApi`
  - `CloseWebsitePasswordApi`
  - `CloseWebsiteSslApi`
  - `CreateWebsiteBackupApi`
  - `DeleteWebsiteBackupApi`
  - `StartWebsiteApi`
  - `StopWebsiteApi`
  - `SetWebsiteLogsApi`
  - `SetWebsitePsApi`
  - `SetWebsiteRootPathApi`
  - `SetWebsiteRunPathApi`
  - `SetWebsiteUserIniApi`
  - `SetWebsitePhpVersionApi`
  - `SetWebsitePasswordApi`
  - `SetWebsiteRewriteRulesApi`
  - `SetWebsiteNginxConfigApi`
  - `SetWebsiteLimitNetApi`
  - `SetWebsitePhpExtensionsApi`
  - `SetWebsiteSslApi`
  - `DeleteWebsiteApi`
- 参数行为统一如下：
  - 必填 `id` 使用正整数校验
  - 必填字符串参数使用非空白校验
  - `runPath`、备注、配置内容等保留“允许空字符串”的业务语义
  - `enabled`、`force_https` 等布尔标志位统一转换为 `0/1`
  - 可选参数在传入 `null` 或关闭状态时显式移除，避免复用对象时残留旧值
- 新增测试：
  - `WebsiteBooleanApisTest`
  - `WebsiteOperationsWriteTest`

## 验收标准

- website 布尔写操作接口统一通过公共抽象解析 `status/msg` 响应
- 各 API 的参数校验规则清晰、一致，关键边界场景具备单元测试覆盖
- facade 写操作对底层 API 的委托路径具备单元测试覆盖
- 格式化、单元测试和主构建验证通过
