package net.heimeng.sdk.btapi.api.website;

/** 设置站点流量限制的 API。 */
public class SetWebsiteLimitNetApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SetLimitNet";

  public SetWebsiteLimitNetApi() {
    super(ENDPOINT, "流量限制设置成功", "流量限制设置失败");
  }

  public SetWebsiteLimitNetApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public SetWebsiteLimitNetApi setEnabled(Boolean enabled) {
    requireNonNull(enabled, "enabled");
    addParam("enabled", enabled ? 1 : 0);
    return this;
  }

  public SetWebsiteLimitNetApi setPerserver(Integer perserver) {
    putOptionalNonNegativeInteger("perserver", perserver);
    return this;
  }

  public SetWebsiteLimitNetApi setPerip(Integer perip) {
    putOptionalNonNegativeInteger("perip", perip);
    return this;
  }

  public SetWebsiteLimitNetApi setLimitRate(Integer limitRate) {
    putOptionalNonNegativeInteger("limit_rate", limitRate);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id")
        && hasBooleanFlagIntParam("enabled")
        && hasOptionalNonNegativeNumberParam("perserver")
        && hasOptionalNonNegativeNumberParam("perip")
        && hasOptionalNonNegativeNumberParam("limit_rate");
  }
}
