package net.heimeng.sdk.btapi.facade;

/**
 * Immutable request model for FTP password changes.
 *
 * @param accountId panel FTP account identifier
 * @param username FTP account username
 * @param path FTP home directory currently associated with the account
 * @param newPassword new password to apply
 */
public record FtpPasswordUpdateRequest(
    int accountId, String username, String path, String newPassword) {

  public FtpPasswordUpdateRequest {
    if (accountId <= 0) {
      throw new IllegalArgumentException("accountId must be positive");
    }
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username cannot be blank");
    }
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException("path cannot be blank");
    }
    if (newPassword == null || newPassword.isBlank()) {
      throw new IllegalArgumentException("newPassword cannot be blank");
    }
  }
}
