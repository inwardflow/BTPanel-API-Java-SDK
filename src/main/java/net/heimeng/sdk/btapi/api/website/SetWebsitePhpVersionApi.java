package net.heimeng.sdk.btapi.api.website;

/**
 * 设置站点 PHP 版本的 API。
 *
 * <p>对应面板 9.0 站点设置“PHP”页签的“切换”操作：{@code site?action=SetPHPVersion}，参数为站点名 {@code siteName}、 版本号
 * {@code version}（例如 {@code 81}，纯静态为 {@code 00}）以及自定义 PHP 的 {@code other}（未使用时为空字符串）。
 */
public class SetWebsitePhpVersionApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SetPHPVersion";

  public SetWebsitePhpVersionApi() {
    super(ENDPOINT, "PHP 版本设置成功", "PHP 版本设置失败");
    addParam("other", "");
  }

  /**
   * 设置站点名（主域名）。
   *
   * @param siteName 站点名，例如 {@code example.com}
   * @return 当前 API 实例
   */
  public SetWebsitePhpVersionApi setSiteName(String siteName) {
    requireNonBlank(siteName, "siteName");
    addParam("siteName", siteName);
    return this;
  }

  /**
   * 设置目标 PHP 版本，对应请求参数 {@code version}。
   *
   * @param phpVersion 版本号，例如 {@code 81}；{@code 00} 表示纯静态
   * @return 当前 API 实例
   */
  public SetWebsitePhpVersionApi setPhpVersion(String phpVersion) {
    requireNonBlank(phpVersion, "phpVersion");
    addParam("version", phpVersion);
    return this;
  }

  /**
   * 设置自定义 PHP 的连接配置，对应 UI 中版本选择“自定义”时的 {@code other} 参数。默认为空字符串。
   *
   * @param other 自定义 PHP 配置，不使用时传空字符串
   * @return 当前 API 实例
   */
  public SetWebsitePhpVersionApi setOther(String other) {
    addParam("other", other == null ? "" : other);
    return this;
  }

  /**
   * 设置站点 ID。
   *
   * @param id 站点 ID
   * @return 当前 API 实例
   * @deprecated 面板 9.0 按站点名切换 PHP 版本，旧的 {@code site?action=SetPhpVersion} 按 ID 设置的接口不在 9.0 UI 中。只设置
   *     ID 而不设置 {@code siteName} 时，客户端在发送请求前抛出 {@link
   *     net.heimeng.sdk.btapi.exception.BtApiException}（错误代码 {@code INVALID_PARAMETERS}）。请改用 {@link
   *     #setSiteName(String)}。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public SetWebsitePhpVersionApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("siteName", "version");
  }
}
