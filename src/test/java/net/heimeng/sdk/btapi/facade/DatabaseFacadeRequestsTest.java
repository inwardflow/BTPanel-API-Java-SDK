package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.testutil.TestValueFactory;

@DisplayName("Database facade request objects")
class DatabaseFacadeRequestsTest {

  @Test
  @DisplayName("DatabaseCreateRequest builder should apply sensible defaults")
  void createRequestBuilderAppliesDefaults() {
    DatabaseCreateRequest request =
        DatabaseCreateRequest.builder(
                TestValueFactory.sampleDatabaseName(),
                TestValueFactory.sampleDatabaseUser(),
                TestValueFactory.samplePassword())
            .build();

    assertEquals(TestValueFactory.sampleDatabaseName(), request.databaseName());
    assertEquals(TestValueFactory.sampleDatabaseUser(), request.username());
    assertEquals(TestValueFactory.samplePassword(), request.password());
    assertEquals(DatabaseCreateRequest.Type.MYSQL, request.type());
    assertEquals("utf8mb4", request.charset());
    assertEquals(TestValueFactory.sampleDatabaseRemark(), request.remark());
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
            () ->
                DatabaseCreateRequest.builder(
                        TestValueFactory.sampleDatabaseName(),
                        TestValueFactory.sampleDatabaseUser(),
                        TestValueFactory.samplePassword())
                    .sid(-1));

    assertEquals("sid cannot be negative", exception.getMessage());
  }

  @Test
  @DisplayName("DatabaseDeleteRequest should require positive id")
  void deleteRequestRequiresPositiveId() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new DatabaseDeleteRequest(TestValueFactory.sampleDatabaseName(), 0));

    assertEquals("databaseId must be positive", exception.getMessage());
  }

  @Test
  @DisplayName("DatabasePasswordUpdateRequest should require non blank password")
  void passwordUpdateRequestRequiresNonBlankPassword() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new DatabasePasswordUpdateRequest(
                    TestValueFactory.sampleDatabaseName(),
                    TestValueFactory.sampleDatabaseUser(),
                    " "));

    assertEquals("newPassword cannot be blank", exception.getMessage());
  }
}
