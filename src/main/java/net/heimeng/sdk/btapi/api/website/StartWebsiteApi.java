package net.heimeng.sdk.btapi.api.website;

/**
 * 启动站点的 API。
 *
 * <p>对应面板 9.0 网站列表中“已停止”开关的启用操作：{@code site?action=SiteStart}，参数为站点 {@code id} 和站点名 {@code
 * name}。实测只传 {@code id} 时面板返回 HTTP 404，因此必须同时设置站点名。
 */
public class StartWebsiteApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SiteStart";

  public StartWebsiteApi() {
    super(ENDPOINT, "站点启动成功", "站点启动失败");
  }

  /**
   * 设置站点 ID。
   *
   * @param id 站点 ID，必须为正数
   * @return 当前 API 实例
   */
  public StartWebsiteApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  /**
   * 设置站点名（主域名），与面板 UI 发送的 {@code name} 参数一致。面板启动站点时必须提供。
   *
   * @param name 站点名，例如 {@code example.com}
   * @return 当前 API 实例
   */
  public StartWebsiteApi setName(String name) {
    requireNonBlank(name, "name");
    addParam("name", name);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasRequiredParams("name");
  }
}
