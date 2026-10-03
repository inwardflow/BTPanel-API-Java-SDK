package net.heimeng.sdk.btapi.api.website;

/** 设置站点 PHP 版本的 API。 */
public class SetWebsitePhpVersionApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SetPhpVersion";

  public SetWebsitePhpVersionApi() {
    super(ENDPOINT, "PHP 版本设置成功", "PHP 版本设置失败");
  }

  public SetWebsitePhpVersionApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public SetWebsitePhpVersionApi setPhpVersion(String phpVersion) {
    requireNonBlank(phpVersion, "phpVersion");
    addParam("php_version", phpVersion);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasRequiredParams("php_version");
  }
}
