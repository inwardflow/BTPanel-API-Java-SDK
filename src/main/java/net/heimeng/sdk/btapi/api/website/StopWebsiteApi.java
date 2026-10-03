package net.heimeng.sdk.btapi.api.website;

/**
 * 停止站点的 API。
 *
 * <p>对应面板 9.0 网站列表中“运行中”开关的停用操作：{@code site?action=SiteStop}，参数为站点 {@code id} 和站点名 {@code name}。旧版本
 * SDK 使用的 {@code site?action=StopSite} 并不存在于 9.0 面板。
 */
public class StopWebsiteApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SiteStop";

  public StopWebsiteApi() {
    super(ENDPOINT, "站点停止成功", "站点停止失败");
  }

  /**
   * 设置站点 ID。
   *
   * @param id 站点 ID，必须为正数
   * @return 当前 API 实例
   */
  public StopWebsiteApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  /**
   * 设置站点名（主域名），与面板 UI 发送的 {@code name} 参数一致。
   *
   * @param name 站点名，例如 {@code example.com}
   * @return 当前 API 实例
   */
  public StopWebsiteApi setName(String name) {
    requireNonBlank(name, "name");
    addParam("name", name);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && (!hasParam("name") || hasNonBlankStringParam("name"));
  }
}
