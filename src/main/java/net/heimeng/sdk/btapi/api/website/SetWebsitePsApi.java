package net.heimeng.sdk.btapi.api.website;

/** 修改站点备注的 API。 */
public class SetWebsitePsApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "data?action=setPs&table=sites";

  public SetWebsitePsApi() {
    super(ENDPOINT, "站点备注修改成功", "站点备注修改失败");
  }

  public SetWebsitePsApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public SetWebsitePsApi setPs(String ps) {
    requireNonNull(ps, "ps");
    addParam("ps", ps);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasParam("ps");
  }
}
