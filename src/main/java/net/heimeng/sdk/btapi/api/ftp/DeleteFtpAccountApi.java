package net.heimeng.sdk.btapi.api.ftp;

/**
 * 删除 FTP 账户的 API。
 *
 * <p>根据用户名删除指定 FTP 账户，并统一解析布尔型执行结果。
 */
public class DeleteFtpAccountApi extends AbstractFtpBooleanApi {

  private static final String ENDPOINT = "ftp?action=DeleteUser";

  public DeleteFtpAccountApi() {
    super(ENDPOINT, "FTP account deleted successfully", "Failed to delete FTP account");
  }

  public DeleteFtpAccountApi(int id, String username) {
    this();
    setId(id);
    setUsername(username);
  }

  public DeleteFtpAccountApi setId(int id) {
    if (id <= 0) {
      throw new IllegalArgumentException("id must be positive");
    }
    addParam("id", id);
    return this;
  }

  public DeleteFtpAccountApi setUsername(String username) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username cannot be blank");
    }
    addParam("username", username);
    return this;
  }

  @Override
  protected boolean validateParams() {
    Object id = params.get("id");
    return id instanceof Number numberValue
        && numberValue.intValue() > 0
        && hasRequiredParams("username");
  }
}
