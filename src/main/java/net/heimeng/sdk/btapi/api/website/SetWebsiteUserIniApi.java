package net.heimeng.sdk.btapi.api.website;

/**
 * 切换站点防跨站（{@code open_basedir} / {@code .user.ini}）配置的 API。
 *
 * <p>对应面板 9.0 站点设置“网站目录”页签的“防跨站攻击”开关：{@code site?action=SetDirUserINI}，参数为站点 {@code id} 和站点根目录
 * {@code path}。每次调用切换一次开关状态，当前状态可通过 {@link GetWebsiteConfigApi} 返回的 {@code userini} 字段获取。
 */
public class SetWebsiteUserIniApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SetDirUserINI";

  public SetWebsiteUserIniApi() {
    super(ENDPOINT, "防跨站配置切换成功", "防跨站配置切换失败");
  }

  /**
   * 设置站点 ID，与面板 UI 发送的 {@code id} 参数一致。
   *
   * @param id 站点 ID，必须为正数
   * @return 当前 API 实例
   */
  public SetWebsiteUserIniApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  /**
   * 设置站点根目录。
   *
   * @param path 站点根目录，例如 {@code /www/wwwroot/example.com}
   * @return 当前 API 实例
   */
  public SetWebsiteUserIniApi setPath(String path) {
    requireNonBlank(path, "path");
    addParam("path", path);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path") && (!hasParam("id") || hasPositiveIntegerParam("id"));
  }
}
