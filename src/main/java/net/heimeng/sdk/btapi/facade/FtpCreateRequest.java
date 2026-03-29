package net.heimeng.sdk.btapi.facade;

/**
 * Immutable request model for FTP account creation.
 *
 * @param username FTP account username
 * @param password FTP account password
 * @param path FTP home directory
 * @param remark panel-visible remark
 */
public record FtpCreateRequest(String username, String password, String path, String remark) {

  public FtpCreateRequest {
    username = requireNonBlank(username, "username");
    password = requireNonBlank(password, "password");
    path = requireNonBlank(path, "path");
    remark = requireNonBlank(remark, "remark");
  }

  public static FtpCreateRequest of(String username, String password, String path) {
    return new FtpCreateRequest(username, password, path, username);
  }

  private static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
    return value;
  }
}
