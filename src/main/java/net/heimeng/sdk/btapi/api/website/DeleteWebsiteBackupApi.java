package net.heimeng.sdk.btapi.api.website;

/** 删除站点备份的 API。 */
public class DeleteWebsiteBackupApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=DelBackup";

  public DeleteWebsiteBackupApi() {
    super(ENDPOINT, "站点备份删除成功", "站点备份删除失败");
  }

  public DeleteWebsiteBackupApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
