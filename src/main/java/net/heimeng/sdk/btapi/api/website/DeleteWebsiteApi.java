package net.heimeng.sdk.btapi.api.website;

/**
 * 删除站点的 API。
 *
 * <p>支持按需同时删除关联 FTP、数据库与站点目录。
 */
public class DeleteWebsiteApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=DeleteSite";

  public DeleteWebsiteApi(int id, String webname) {
    this();
    setId(id);
    setWebname(webname);
  }

  public DeleteWebsiteApi() {
    super(ENDPOINT, "站点删除成功", "站点删除失败");
  }

  public DeleteWebsiteApi setId(int id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public DeleteWebsiteApi setWebname(String webname) {
    requireNonBlank(webname, "webname");
    addParam("webname", webname);
    return this;
  }

  public DeleteWebsiteApi setDeleteFtp(boolean deleteFtp) {
    if (deleteFtp) {
      addParam("ftp", 1);
    } else {
      removeParam("ftp");
    }
    return this;
  }

  public DeleteWebsiteApi setDeleteDatabase(boolean deleteDatabase) {
    if (deleteDatabase) {
      addParam("database", 1);
    } else {
      removeParam("database");
    }
    return this;
  }

  public DeleteWebsiteApi setDeletePath(boolean deletePath) {
    if (deletePath) {
      addParam("path", 1);
    } else {
      removeParam("path");
    }
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id")
        && hasNonBlankStringParam("webname")
        && hasOptionalBooleanFlagIntParam("ftp")
        && hasOptionalBooleanFlagIntParam("database")
        && hasOptionalBooleanFlagIntParam("path");
  }
}
