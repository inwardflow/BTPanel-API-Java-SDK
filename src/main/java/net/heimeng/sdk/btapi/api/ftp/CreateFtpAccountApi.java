package net.heimeng.sdk.btapi.api.ftp;

/**
 * 创建 FTP 账户的 API。
 *
 * <p>负责封装用户名、密码、根目录和配额等参数，并统一解析布尔型执行结果。
 */
public class CreateFtpAccountApi extends AbstractFtpBooleanApi {

  private static final String ENDPOINT = "ftp?action=AddUser";

  public CreateFtpAccountApi() {
    super(ENDPOINT, "FTP account created successfully", "Failed to create FTP account");
  }

  public CreateFtpAccountApi(String username, String password, String path) {
    this();
    setUsername(username);
    setPassword(password);
    setPath(path);
  }

  public CreateFtpAccountApi setUsername(String username) {
    requireNonBlank(username, "username");
    addParam("ftp_username", username);
    if (!hasNonBlankParam("ps")) {
      addParam("ps", username);
    }
    return this;
  }

  public CreateFtpAccountApi setPassword(String password) {
    requireNonBlank(password, "password");
    addParam("ftp_password", password);
    return this;
  }

  public CreateFtpAccountApi setPath(String path) {
    requireNonBlank(path, "path");
    addParam("path", path);
    return this;
  }

  public CreateFtpAccountApi setSize(long size) {
    if (size < 0L) {
      throw new IllegalArgumentException("size cannot be negative");
    }
    return this;
  }

  public CreateFtpAccountApi setCanViewAll(boolean canViewAll) {
    return this;
  }

  public CreateFtpAccountApi setRemark(String remark) {
    requireNonBlank(remark, "remark");
    addParam("ps", remark);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("ftp_username", "ftp_password", "path", "ps");
  }

  private void requireNonBlank(String value, String paramName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(paramName + " cannot be blank");
    }
  }
}
