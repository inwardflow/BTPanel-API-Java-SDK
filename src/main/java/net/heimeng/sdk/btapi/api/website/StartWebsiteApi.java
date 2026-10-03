package net.heimeng.sdk.btapi.api.website;

/** 启动站点的 API。 */
public class StartWebsiteApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SiteStart";

  public StartWebsiteApi() {
    super(ENDPOINT, "站点启动成功", "站点启动失败");
  }

  public StartWebsiteApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
