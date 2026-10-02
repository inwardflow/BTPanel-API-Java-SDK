# Task 001: DatabaseUtils 现代化重构

## 状态

已完成

## 背景

`DatabaseUtils` 是旧阶段遗留的工具类，存在以下问题：

- 通过 `null` 和 `false` 吞掉请求失败，无法区分“数据库不存在”和“接口调用失败”
- 直接依赖底层 `execute(new GetDatabasesApi())`，没有对齐新的 `BtApiManager.database()` 门面风格
- 超时参数使用 `long timeoutMs`，可读性和类型安全较差
- 缺少针对性的单元测试

## 目标

- 将数据库辅助查询收敛为更现代的 `Optional + 异常` 风格
- 对齐高层门面调用方式，减少工具类与底层 API 细节的耦合
- 将超时参数改为 `Duration`
- 为工具类补充单元测试，保证轮询和删除确认逻辑可回归

## 实施内容

- 将 `DatabaseUtils` 重构为 `final` 工具类，并增加私有构造方法
- 新增 `exists`、`findByName`、`findById`、`waitForCreation`、`waitForDeletion`、`deleteIfExists`
- 用 `Optional<DatabaseInfo>` 代替 `null` 返回
- 对接口失败使用 `BtApiException` fail-fast，而不是静默返回 `false/null`
- 将轮询和删除确认改为 `Duration` 参数
- 新增 `DatabaseUtilsTest` 覆盖查询、失败传播、轮询创建、确认删除和超时场景

## 验收标准

- 数据库不存在时返回 `Optional.empty()` 或 `true/false` 的业务结果，而不是靠 `null` 表达状态
- 接口调用失败时抛出异常，调用方可以明确感知错误
- 重构后单元测试与仓库主构建通过
