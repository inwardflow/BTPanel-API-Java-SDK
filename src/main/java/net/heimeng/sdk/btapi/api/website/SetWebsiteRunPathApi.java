package net.heimeng.sdk.btapi.api.website;

/**
 * 修改站点运行目录的 API。
 *
 * <p>运行目录允许传空字符串，用于恢复到根目录执行。
 */
public class SetWebsiteRunPathApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SetSiteRunPath";

  public SetWebsiteRunPathApi() {
    super(ENDPOINT, "运行目录设置成功", "运行目录设置失败");
  }

  public SetWebsiteRunPathApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public SetWebsiteRunPathApi setRunPath(String runPath) {
    requireNonNull(runPath, "runPath");
    addParam("runPath", runPath);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasParam("runPath");
  }
}
