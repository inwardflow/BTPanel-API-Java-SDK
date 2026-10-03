package net.heimeng.sdk.btapi.api.website;

/** 关闭站点访问密码保护的 API。 */
public class CloseWebsitePasswordApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=CloseHasPwd";

  public CloseWebsitePasswordApi() {
    super(ENDPOINT, "密码访问已关闭", "关闭密码访问失败");
  }

  public CloseWebsitePasswordApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
