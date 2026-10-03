# Task 003: System 模块观测类 API 契约化重构

## 状态

已完成

## 背景

系统模块中仍有多处旧式解析实现：

- `GetDiskInfoApi` 仅做宽松 JSON 判断，并使用大量默认空字符串兜底
- `GetNetworkStatusApi` 对成功与失败包装格式处理不统一
- `CheckPanelUpdateApi` 对返回契约的约束较弱，缺少参数行为与异常路径测试
- 上述接口均缺少独立单元测试，覆盖率偏低

## 目标

- 明确磁盘、网络、面板更新接口的成功/失败契约
- 对有效包装响应和直出响应都给出稳定解析结果
- 对缺失关键字段的响应 fail-fast
- 为系统模块补齐解析器与门面层测试

## 实施内容

- 重写 `GetDiskInfoApi`
- 重写 `GetNetworkStatusApi`
- 重写 `CheckPanelUpdateApi`
- 新增：
  - `GetDiskInfoApiTest`
  - `GetNetworkStatusApiTest`
  - `CheckPanelUpdateApiTest`
- 任务计数任务之外，继续补齐系统门面委托测试

## 验收标准

- 文档中的系统接口返回样例均可被正确解析
- 失败 JSON 的状态和消息可被稳定保留
- 缺少关键载荷或状态字段时抛出异常，而不是静默回退
- 主构建与单元测试通过
