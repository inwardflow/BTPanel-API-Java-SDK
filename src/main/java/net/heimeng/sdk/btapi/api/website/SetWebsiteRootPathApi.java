package net.heimeng.sdk.btapi.api.website;

/** 修改站点根目录的 API。 */
public class SetWebsiteRootPathApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SetPath";

  public SetWebsiteRootPathApi() {
    super(ENDPOINT, "站点根目录修改成功", "站点根目录修改失败");
  }

  public SetWebsiteRootPathApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public SetWebsiteRootPathApi setPath(String path) {
    requireNonBlank(path, "path");
    addParam("path", path);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasRequiredParams("path");
  }
}
