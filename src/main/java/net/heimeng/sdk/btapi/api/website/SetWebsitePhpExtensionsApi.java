package net.heimeng.sdk.btapi.api.website;

/** 切换站点 PHP 扩展状态的 API。 */
public class SetWebsitePhpExtensionsApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SetPHPModules";

  public SetWebsitePhpExtensionsApi() {
    super(ENDPOINT, "PHP 扩展开关设置成功", "PHP 扩展开关设置失败");
  }

  public SetWebsitePhpExtensionsApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public SetWebsitePhpExtensionsApi setModuleName(String moduleName) {
    requireNonBlank(moduleName, "moduleName");
    addParam("module_name", moduleName);
    return this;
  }

  public SetWebsitePhpExtensionsApi setEnabled(Boolean enabled) {
    requireNonNull(enabled, "enabled");
    addParam("enabled", enabled ? 1 : 0);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id")
        && hasNonBlankStringParam("module_name")
        && hasBooleanFlagIntParam("enabled");
  }
}
