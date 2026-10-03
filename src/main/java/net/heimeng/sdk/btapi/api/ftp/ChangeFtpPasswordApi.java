package net.heimeng.sdk.btapi.api.ftp;

/**
 * 修改 FTP 账户密码的 API。
 *
 * <p>根据用户名更新对应 FTP 账户的密码，并统一解析布尔型执行结果。
 */
public class ChangeFtpPasswordApi extends AbstractFtpBooleanApi {

  private static final String ENDPOINT = "ftp?action=SetUser";

  public ChangeFtpPasswordApi() {
    super(ENDPOINT, "FTP password changed successfully", "Failed to change FTP password");
  }

  public ChangeFtpPasswordApi(int id, String username, String newPassword, String path) {
    this();
    setId(id);
    setUsername(username);
    setNewPassword(newPassword);
    setPath(path);
  }

  public ChangeFtpPasswordApi setId(int id) {
    if (id <= 0) {
      throw new IllegalArgumentException("id must be positive");
    }
    addParam("id", id);
    return this;
  }

  public ChangeFtpPasswordApi setUsername(String username) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username cannot be blank");
    }
    addParam("ftp_username", username);
    return this;
  }

  public ChangeFtpPasswordApi setNewPassword(String newPassword) {
    if (newPassword == null || newPassword.isBlank()) {
      throw new IllegalArgumentException("newPassword cannot be blank");
    }
    addParam("new_password", newPassword);
    return this;
  }

  public ChangeFtpPasswordApi setPath(String path) {
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException("path cannot be blank");
    }
    addParam("path", path);
    return this;
  }

  @Override
  protected boolean validateParams() {
    Object id = params.get("id");
    return id instanceof Number numberValue
        && numberValue.intValue() > 0
        && hasRequiredParams("ftp_username", "new_password", "path");
  }
}
