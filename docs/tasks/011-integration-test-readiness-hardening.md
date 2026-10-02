# Task 011: 集成测试就绪性与基础设施加固

## 状态：已完成
## 背景

项目已经接入 Failsafe，并具备基础的集成测试类，但当前实现仍存在若干阻碍真实执行的问题：

- 多个集成测试类重复加载配置、重复创建客户端，维护成本高
- `application-test.properties.example` 未覆盖文件与网站测试所需字段
- 测试专用环境变量命名混用属性风格，如 `test.filePath`、`test.dbName`，不利于跨平台使用
- 个别测试通过直接调用其他 `@Test` 方法复用逻辑，导致执行路径脆弱
- 当时独立的系统信息集成测试使用默认假地址和假密钥兜底，容易把配置缺失误判成真实失败

## 目标

- 抽取统一的集成测试公共支撑，明确配置来源与失败信息
- 统一集成测试环境变量命名，补齐示例配置
- 消除集成测试中的跨测试调用与默认假配置
- 让项目在真实配置齐备时可直接执行集成测试，在配置不全时能快速定位缺失项

## 实施内容

- 新增 `AbstractIntegrationTestSupport`
  - 统一加载 `application-test.properties`
  - 统一读取环境变量与属性文件配置
  - 统一创建 `BtApiManager`
  - 提供路径拼接、随机后缀、关闭资源等测试辅助方法
- 重构以下集成测试：
  - `DatabaseIntegrationTest`
  - `FileIntegrationTest`
  - `WebsiteIntegrationTest`
  - `SystemIntegrationTest` 中的系统信息相关用例
- 统一使用规范化环境变量：
  - `BT_PANEL_BASE_URL`
  - `BT_PANEL_API_KEY`
  - `BT_PANEL_TEST_DB_NAME`
  - `BT_PANEL_TEST_DB_USER`
  - `BT_PANEL_TEST_DB_PASSWORD`
  - `BT_PANEL_TEST_FILE_PATH`
  - `BT_PANEL_TEST_DOMAIN_SUFFIX`
  - `BT_PANEL_TEST_WEBROOT_BASE`
  - `BT_PANEL_CONNECT_TIMEOUT`
  - `BT_PANEL_READ_TIMEOUT`
  - `BT_PANEL_RETRY_COUNT`
- 更新 `application-test.properties.example`
  - 补齐数据库、文件、网站集成测试示例字段
- 更新 `docs/testing.md`
  - 明确集成测试启用方式
  - 说明环境变量与属性文件映射关系
  - 补充执行建议与约束

## 验收标准

- 集成测试初始化逻辑不再在多个测试类中重复维护
- 本地示例配置能够覆盖数据库、文件、网站三类真实测试所需字段
- 集成测试不再直接调用其他 `@Test` 方法
- 常规构建验证通过，后续只需补齐真实面板连接信息即可执行集成测试
