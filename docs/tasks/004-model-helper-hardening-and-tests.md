# Task 004: 模型辅助方法健壮化与测试补齐

## 状态

已完成

## 背景

多个模型类已经承担了一部分辅助逻辑，但仍存在以下问题：

- `DatabaseInfo` 对状态字符串中的空白不够宽容
- `DiskInfo` 的容量辅助方法存在重复逻辑，且空白值会原样返回
- `NetworkStatus` 仅支持 `Number` 类型，遇到字符串数值时会退化为默认值
- `DatabaseInfo`、`DiskInfo`、`NetworkStatus`、`PanelUpdateInfo` 之前都缺少独立单元测试

## 目标

- 提升模型辅助方法在空值、空白值和字符串数值场景下的健壮性
- 固化模型层的行为契约，避免后续重构破坏辅助方法

## 实施内容

- `DatabaseInfo`
  - `isNormal` 支持忽略前后空白
  - `getFormattedSize` 对非正数统一返回 `0 KB`
- `DiskInfo`
  - 抽取统一的容量段读取逻辑
  - 对 `null` 或空白字符串统一返回 `Unknown`
- `NetworkStatus`
  - `getMemoryUsage`、`getLoad1Min`、`getLoad5Min`、`getLoad15Min` 支持字符串数值
- 新增测试：
  - `DatabaseInfoTest`
  - `DiskInfoTest`
  - `NetworkStatusTest`
  - `PanelUpdateInfoTest`

## 验收标准

- 模型辅助方法在边界值场景下返回稳定结果
- 字符串数值可被正确解析
- 主构建与单元测试通过
