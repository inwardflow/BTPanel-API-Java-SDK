package net.heimeng.sdk.btapi.api.website;

/** 关闭站点 SSL 配置的 API。 */
public class CloseWebsiteSslApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=CloseSSL";

  public CloseWebsiteSslApi() {
    super(ENDPOINT, "SSL 已关闭", "关闭 SSL 失败");
  }

  public CloseWebsiteSslApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
