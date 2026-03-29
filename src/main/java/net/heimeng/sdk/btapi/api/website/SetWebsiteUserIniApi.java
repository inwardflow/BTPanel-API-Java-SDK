package net.heimeng.sdk.btapi.api.website;

/** 切换站点防跨站配置的 API。 */
public class SetWebsiteUserIniApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SetDirUserINI";

  public SetWebsiteUserIniApi() {
    super(ENDPOINT, "防跨站配置切换成功", "防跨站配置切换失败");
  }

  public SetWebsiteUserIniApi setPath(String path) {
    requireNonBlank(path, "path");
    addParam("path", path);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path");
  }
}
