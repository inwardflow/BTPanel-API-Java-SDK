# 宝塔 Linux 面板 API 文档

> 入口索引文档
> 最后整理：2026-03-29
> 所有示例均已脱敏

仓库内的主文档已经迁移到更清晰、可维护的目录结构中。请优先阅读以下文件：

- 开发者 API 主文档：[docs/btpanel-api-developer-guide.md](/E:/Stable/BTPanel-API-Java-SDK/docs/btpanel-api-developer-guide.md)
- OpenAPI 主文件：[docs/openapi/btpanel-developer-api.openapi-3.1.json](/E:/Stable/BTPanel-API-Java-SDK/docs/openapi/btpanel-developer-api.openapi-3.1.json)
- 测试说明：[docs/testing.md](/E:/Stable/BTPanel-API-Java-SDK/docs/testing.md)
- 快速示例：[docs/examples/quickstart.md](/E:/Stable/BTPanel-API-Java-SDK/docs/examples/quickstart.md)

当前最重要的结论如下：

- 宝塔官方旧 PDF 已停止更新，但开发者 API 并未消失。
- 页面抓包抓到的大多数业务能力，仍然可以回到开发者 API 方式验证。
- 当前 BTPanel 9.0.0 已验证通过的站点 SSL 链路是：

```text
GetSSL -> get_order_list -> GetSiteDomain -> SetBatchCertToSite
```

- 历史上的 `SetSSL / SaveSSL / CloseSSL / GetSSLCertList` 不应再默认视为“当前面板稳定可用接口”。

安全提醒：

- 不要在仓库、截图或示例中暴露真实面板地址、API Key、`request_time`、`request_token`。
- 本仓库中的示例与 OpenAPI 文档统一使用占位值。
