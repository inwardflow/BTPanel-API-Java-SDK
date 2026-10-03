package net.heimeng.sdk.btapi.facade;

/**
 * Immutable request model for deleting an FTP account.
 *
 * @param accountId panel FTP account identifier
 * @param username FTP account username
 */
public record FtpDeleteRequest(int accountId, String username) {

  public FtpDeleteRequest {
    if (accountId <= 0) {
      throw new IllegalArgumentException("accountId must be positive");
    }
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username cannot be blank");
    }
  }
}
