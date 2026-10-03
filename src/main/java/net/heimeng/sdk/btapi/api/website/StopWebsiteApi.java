package net.heimeng.sdk.btapi.api.website;

/** 停止站点的 API。 */
public class StopWebsiteApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=StopSite";

  public StopWebsiteApi() {
    super(ENDPOINT, "站点停止成功", "站点停止失败");
  }

  public StopWebsiteApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
