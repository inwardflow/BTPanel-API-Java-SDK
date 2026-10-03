package net.heimeng.sdk.btapi.facade;

/**
 * Immutable request model for database password changes.
 *
 * @param databaseName panel-visible database name
 * @param username database account username
 * @param newPassword new password to apply
 */
public record DatabasePasswordUpdateRequest(
    String databaseName, String username, String newPassword) {

  public DatabasePasswordUpdateRequest {
    if (databaseName == null || databaseName.isBlank()) {
      throw new IllegalArgumentException("databaseName cannot be blank");
    }
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username cannot be blank");
    }
    if (newPassword == null || newPassword.isBlank()) {
      throw new IllegalArgumentException("newPassword cannot be blank");
    }
  }
}
