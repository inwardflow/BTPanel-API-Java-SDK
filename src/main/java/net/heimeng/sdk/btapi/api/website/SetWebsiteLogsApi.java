package net.heimeng.sdk.btapi.api.website;

/**
 * 切换站点访问日志状态的 API。
 *
 * <p>宝塔面板的该接口为切换语义，因此只需传入站点 ID。
 */
public class SetWebsiteLogsApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=logsOpen";

  public SetWebsiteLogsApi() {
    super(ENDPOINT, "日志状态切换成功", "日志状态切换失败");
  }

  public SetWebsiteLogsApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
