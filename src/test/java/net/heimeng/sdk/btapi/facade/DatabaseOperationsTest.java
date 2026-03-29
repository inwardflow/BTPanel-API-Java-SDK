package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.heimeng.sdk.btapi.api.database.ChangeDatabasePasswordApi;
import net.heimeng.sdk.btapi.api.database.CreateDatabaseApi;
import net.heimeng.sdk.btapi.api.database.DeleteDatabaseApi;
import net.heimeng.sdk.btapi.api.database.GetDatabasesApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.database.DatabaseInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseOperations facade tests")
class DatabaseOperationsTest {

  @Mock private BtClient client;

  @Test
  @DisplayName("list should delegate to GetDatabasesApi")
  void listDelegatesToClient() {
    DatabaseOperations operations = new DatabaseOperations(client);
    when(client.execute(any(GetDatabasesApi.class))).thenReturn(successListResponse());

    BtResult<List<DatabaseInfo>> result = operations.list();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetDatabasesApi.class));
  }

  @Test
  @DisplayName("create should map typed request defaults to CreateDatabaseApi")
  void createDelegatesToClientWithDefaultRequestValues() {
    DatabaseOperations operations = new DatabaseOperations(client);
    when(client.execute(any(CreateDatabaseApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.create(DatabaseCreateRequest.builder("demo_db", "demo_user", "secret").build());

    assertTrue(result.isSuccess());
    verify(client)
        .execute(
            argThat(
                api ->
                    api.getEndpoint().equals("database")
                        && "AddDatabase".equals(api.getParams().get("action"))
                        && "demo_db".equals(api.getParams().get("name"))
                        && "demo_user".equals(api.getParams().get("db_user"))
                        && "secret".equals(api.getParams().get("password"))
                        && "utf8mb4".equals(api.getParams().get("codeing"))
                        && "MySQL".equals(api.getParams().get("dtype"))
                        && "demo_db".equals(api.getParams().get("ps"))
                        && "%".equals(api.getParams().get("dataAccess"))
                        && "%".equals(api.getParams().get("address"))
                        && "0.0.0.0/0".equals(api.getParams().get("listen_ip"))
                        && "%".equals(api.getParams().get("host"))
                        && Integer.valueOf(0).equals(api.getParams().get("sid"))));
  }

  @Test
  @DisplayName("create should apply optional typed request overrides")
  void createDelegatesToClientWithCustomRequestValues() {
    DatabaseOperations operations = new DatabaseOperations(client);
    when(client.execute(any(CreateDatabaseApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.create(
            DatabaseCreateRequest.builder("demo_db", "demo_user", "secret")
                .type(DatabaseCreateRequest.Type.MONGODB)
                .charset("utf8")
                .remark("Production DB")
                .dataAccess("127.0.0.1")
                .address("127.0.0.1")
                .listenIp("127.0.0.1")
                .host("localhost")
                .sid(3)
                .build());

    assertTrue(result.isSuccess());
    verify(client)
        .execute(
            argThat(
                api ->
                    "MongoDb".equals(api.getParams().get("dtype"))
                        && "utf8".equals(api.getParams().get("codeing"))
                        && "Production DB".equals(api.getParams().get("ps"))
                        && "127.0.0.1".equals(api.getParams().get("dataAccess"))
                        && "127.0.0.1".equals(api.getParams().get("address"))
                        && "127.0.0.1".equals(api.getParams().get("listen_ip"))
                        && "localhost".equals(api.getParams().get("host"))
                        && Integer.valueOf(3).equals(api.getParams().get("sid"))));
  }

  @Test
  @DisplayName("delete should delegate to DeleteDatabaseApi")
  void deleteDelegatesToClient() {
    DatabaseOperations operations = new DatabaseOperations(client);
    when(client.execute(any(DeleteDatabaseApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.delete(new DatabaseDeleteRequest("demo_db", 9));

    assertTrue(result.isSuccess());
    verify(client)
        .execute(
            argThat(
                api ->
                    api.getEndpoint().equals("database?action=DeleteDatabase")
                        && "demo_db".equals(api.getParams().get("name"))
                        && Integer.valueOf(9).equals(api.getParams().get("id"))));
  }

  @Test
  @DisplayName("updatePassword should delegate to ChangeDatabasePasswordApi")
  void updatePasswordDelegatesToClient() {
    DatabaseOperations operations = new DatabaseOperations(client);
    when(client.execute(any(ChangeDatabasePasswordApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.updatePassword(
            new DatabasePasswordUpdateRequest("demo_db", "demo_user", "new-secret"));

    assertTrue(result.isSuccess());
    verify(client)
        .execute(
            argThat(
                api ->
                    api.getEndpoint().equals("database?action=ChangeDBPassword")
                        && "demo_db".equals(api.getParams().get("name"))
                        && "demo_user".equals(api.getParams().get("username"))
                        && "new-secret".equals(api.getParams().get("password"))));
  }

  private static BtResult<Boolean> successBoolean() {
    BtResult<Boolean> response = new BtResult<>();
    response.setStatus(true);
    response.setData(true);
    return response;
  }

  private static BtResult<List<DatabaseInfo>> successListResponse() {
    DatabaseInfo databaseInfo = new DatabaseInfo();
    databaseInfo.setId(1);
    databaseInfo.setName("demo_db");
    databaseInfo.setUsername("demo_user");

    BtResult<List<DatabaseInfo>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(databaseInfo));
    return response;
  }
}
