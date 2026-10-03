package net.heimeng.sdk.btapi.api.website;

/**
 * 关闭站点 SSL 的 API（面板 {@code site?action=CloseSSLConf}）。
 *
 * <p>官方文档要求传入站点名 {@code siteName} 和固定值 {@code updateOf=1}。旧版本使用的 {@code CloseSSL} + {@code id} 在
 * BTPanel 9.0.0 上会返回“指定参数无效”。
 *
 * @see <a href="https://docs.bt.cn/api/site/actions/">宝塔官方文档：网站管理</a>
 */
public class CloseWebsiteSslApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=CloseSSLConf";

  public CloseWebsiteSslApi() {
    super(ENDPOINT, "SSL 已关闭", "关闭 SSL 失败");
    addParam("updateOf", 1);
  }

  /**
   * 设置要关闭 SSL 的站点。
   *
   * @param siteName 站点名称（主域名）
   * @return 当前 API 实例
   */
  public CloseWebsiteSslApi setSiteName(String siteName) {
    requireNonBlank(siteName, "siteName");
    addParam("siteName", siteName);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasNonBlankStringParam("siteName");
  }
}
