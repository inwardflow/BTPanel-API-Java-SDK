package net.heimeng.sdk.btapi.api.website;

/** 创建站点备份的 API。 */
public class CreateWebsiteBackupApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=ToBackup";

  public CreateWebsiteBackupApi() {
    super(ENDPOINT, "站点备份创建成功", "站点备份创建失败");
  }

  public CreateWebsiteBackupApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
