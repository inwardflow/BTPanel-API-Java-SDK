package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database facade request objects")
class DatabaseFacadeRequestsTest {

  @Test
  @DisplayName("DatabaseCreateRequest builder should apply sensible defaults")
  void createRequestBuilderAppliesDefaults() {
    DatabaseCreateRequest request =
        DatabaseCreateRequest.builder("demo_db", "demo_user", "secret").build();

    assertEquals("demo_db", request.databaseName());
    assertEquals("demo_user", request.username());
    assertEquals("secret", request.password());
    assertEquals(DatabaseCreateRequest.Type.MYSQL, request.type());
    assertEquals("utf8mb4", request.charset());
    assertEquals("demo_db", request.remark());
    assertEquals("%", request.dataAccess());
    assertEquals("%", request.address());
    assertEquals("0.0.0.0/0", request.listenIp());
    assertEquals("%", request.host());
    assertEquals(0, request.sid());
  }

  @Test
  @DisplayName("DatabaseCreateRequest should reject negative sid")
  void createRequestRejectsNegativeSid() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> DatabaseCreateRequest.builder("demo_db", "demo_user", "secret").sid(-1));

    assertEquals("sid cannot be negative", exception.getMessage());
  }

  @Test
  @DisplayName("DatabaseDeleteRequest should require positive id")
  void deleteRequestRequiresPositiveId() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> new DatabaseDeleteRequest("demo_db", 0));

    assertEquals("databaseId must be positive", exception.getMessage());
  }

  @Test
  @DisplayName("DatabasePasswordUpdateRequest should require non blank password")
  void passwordUpdateRequestRequiresNonBlankPassword() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new DatabasePasswordUpdateRequest("demo_db", "demo_user", " "));

    assertEquals("newPassword cannot be blank", exception.getMessage());
  }
}
