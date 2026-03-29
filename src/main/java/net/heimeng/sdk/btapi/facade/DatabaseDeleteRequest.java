package net.heimeng.sdk.btapi.facade;

/**
 * Immutable request model for deleting a database.
 *
 * @param databaseName panel-visible database name
 * @param databaseId panel database identifier
 */
public record DatabaseDeleteRequest(String databaseName, int databaseId) {

  public DatabaseDeleteRequest {
    if (databaseName == null || databaseName.isBlank()) {
      throw new IllegalArgumentException("databaseName cannot be blank");
    }
    if (databaseId <= 0) {
      throw new IllegalArgumentException("databaseId must be positive");
    }
  }
}
