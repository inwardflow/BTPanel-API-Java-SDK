package net.heimeng.sdk.btapi.api.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.database.DatabaseInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseUtils tests")
class DatabaseUtilsTest {

  @Mock private BtClient client;

  private BtApiManager apiManager;

  @BeforeEach
  void setUp() {
    apiManager = new BtApiManager(client);
  }

  @Test
  @DisplayName("Find by name returns matching database")
  void findByNameReturnsMatchingDatabase() {
    when(client.execute(any(GetDatabasesApi.class)))
        .thenReturn(successListResponse(databaseInfo(7, "demo_db"), databaseInfo(8, "other_db")));

    Optional<DatabaseInfo> database = DatabaseUtils.findByName(apiManager, "demo_db");

    assertTrue(database.isPresent());
    assertEquals(7, database.get().getId());
    assertEquals("demo_db", database.get().getName());
  }

  @Test
  @DisplayName("Exists propagates list failures instead of swallowing them")
  void existsPropagatesListFailures() {
    when(client.execute(any(GetDatabasesApi.class))).thenReturn(failedListResponse("panel error"));

    BtApiException exception =
        assertThrows(BtApiException.class, () -> DatabaseUtils.exists(apiManager, "demo_db"));

    assertTrue(exception.getMessage().contains("panel error"));
  }

  @Test
  @DisplayName("Wait for creation polls until database becomes visible")
  void waitForCreationPollsUntilDatabaseAppears() {
    when(client.execute(any(GetDatabasesApi.class)))
        .thenReturn(successListResponse())
        .thenReturn(successListResponse(databaseInfo(11, "demo_db")));

    Optional<DatabaseInfo> database =
        DatabaseUtils.waitForCreation(
            apiManager, "demo_db", Duration.ofMillis(50), Duration.ofMillis(1));

    assertTrue(database.isPresent());
    assertEquals(11, database.get().getId());
    verify(client, times(2)).execute(any(GetDatabasesApi.class));
  }

  @Test
  @DisplayName("Delete if exists confirms deletion and returns true")
  void deleteIfExistsDeletesAndConfirmsRemoval() {
    when(client.execute(any(GetDatabasesApi.class)))
        .thenReturn(successListResponse(databaseInfo(21, "demo_db")))
        .thenReturn(successListResponse());
    when(client.execute(any(DeleteDatabaseApi.class))).thenReturn(successDeleteResponse());

    boolean deleted = DatabaseUtils.deleteIfExists(apiManager, "demo_db", Duration.ofMillis(50));

    assertTrue(deleted);
    verify(client).execute(any(DeleteDatabaseApi.class));
  }

  @Test
  @DisplayName("Delete if exists skips missing databases")
  void deleteIfExistsSkipsMissingDatabase() {
    when(client.execute(any(GetDatabasesApi.class))).thenReturn(successListResponse());

    boolean deleted = DatabaseUtils.deleteIfExists(apiManager, "missing_db");

    assertTrue(deleted);
    verify(client, never()).execute(any(DeleteDatabaseApi.class));
  }

  @Test
  @DisplayName("Blank database name is rejected eagerly")
  void blankDatabaseNameIsRejected() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> DatabaseUtils.findByName(apiManager, " "));

    assertTrue(exception.getMessage().contains("databaseName"));
  }

  @Test
  @DisplayName("Wait for deletion returns false when timeout expires")
  void waitForDeletionReturnsFalseWhenTimeoutExpires() {
    when(client.execute(any(GetDatabasesApi.class)))
        .thenReturn(successListResponse(databaseInfo(30, "demo_db")))
        .thenReturn(successListResponse(databaseInfo(30, "demo_db")))
        .thenReturn(successListResponse(databaseInfo(30, "demo_db")));

    boolean deleted =
        DatabaseUtils.waitForDeletion(
            apiManager, "demo_db", Duration.ofMillis(5), Duration.ofMillis(1));

    assertFalse(deleted);
  }

  private static BtResult<List<DatabaseInfo>> successListResponse(DatabaseInfo... databases) {
    BtResult<List<DatabaseInfo>> response = new BtResult<>();
    response.setStatus(true);
    response.setMsg("success");
    response.setData(List.of(databases));
    return response;
  }

  private static BtResult<List<DatabaseInfo>> failedListResponse(String message) {
    BtResult<List<DatabaseInfo>> response = new BtResult<>();
    response.setStatus(false);
    response.setMsg(message);
    response.setData(List.of());
    return response;
  }

  private static BtResult<Boolean> successDeleteResponse() {
    BtResult<Boolean> response = new BtResult<>();
    response.setStatus(true);
    response.setMsg("success");
    response.setData(true);
    return response;
  }

  private static DatabaseInfo databaseInfo(int id, String name) {
    DatabaseInfo databaseInfo = new DatabaseInfo();
    databaseInfo.setId(id);
    databaseInfo.setName(name);
    return databaseInfo;
  }
}
