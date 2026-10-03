package net.heimeng.sdk.btapi.api.website;

/**
 * 切换站点 PHP 扩展状态的 API。 *
 *
 * @deprecated 面板 9.0 没有按站点切换 PHP 扩展的接口，{@code site?action=SetPHPModules} 不在 UI 中。扩展需在软件商店的 PHP 设置中按
 *     PHP 版本安装或卸载，SDK 暂不提供替代接口。
 */
@Deprecated(since = "0.2.0", forRemoval = true)
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
