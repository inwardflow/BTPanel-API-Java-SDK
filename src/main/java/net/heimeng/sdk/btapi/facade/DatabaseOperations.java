package net.heimeng.sdk.btapi.facade;

import java.util.List;
import java.util.Objects;

import net.heimeng.sdk.btapi.api.database.ChangeDatabasePasswordApi;
import net.heimeng.sdk.btapi.api.database.CreateDatabaseApi;
import net.heimeng.sdk.btapi.api.database.DeleteDatabaseApi;
import net.heimeng.sdk.btapi.api.database.GetDatabasesApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.database.DatabaseInfo;

/**
 * 数据库相关能力的门面入口。
 *
 * <p>聚合数据库列表、创建、删除和改密等常见操作，适合作为业务代码的首选调用层。
 */
public final class DatabaseOperations extends AbstractOperations {

  public DatabaseOperations(BtClient client) {
    super(client);
  }

  public BtResult<List<DatabaseInfo>> list() {
    return execute(new GetDatabasesApi());
  }

  public BtResult<Boolean> create(CreateDatabaseApi api) {
    Objects.requireNonNull(api, "api cannot be null");
    return execute(api);
  }

  public BtResult<Boolean> create(DatabaseCreateRequest request) {
    Objects.requireNonNull(request, "request cannot be null");

    CreateDatabaseApi.Builder builder =
        CreateDatabaseApi.builder(request.databaseName(), request.username(), request.password())
            .withCharset(request.charset())
            .withNote(request.remark())
            .withDataAccess(request.dataAccess())
            .withAddress(request.address())
            .withListenIp(request.listenIp())
            .withHost(request.host())
            .withSid(request.sid());

    if (request.type() == DatabaseCreateRequest.Type.MONGODB) {
      builder.asMongoDb();
    } else {
      builder.asMySql();
    }

    return execute(builder.build());
  }

  public BtResult<Boolean> delete(DatabaseDeleteRequest request) {
    Objects.requireNonNull(request, "request cannot be null");
    return execute(DeleteDatabaseApi.create(request.databaseName(), request.databaseId()));
  }

  /**
   * Backward-compatible overload retained for existing utility helpers.
   *
   * <p>New code should prefer {@link #delete(DatabaseDeleteRequest)} so the request shape remains
   * explicit at the facade boundary.
   */
  public BtResult<Boolean> delete(String databaseName, int databaseId) {
    return delete(new DatabaseDeleteRequest(databaseName, databaseId));
  }

  public BtResult<Boolean> updatePassword(DatabasePasswordUpdateRequest request) {
    Objects.requireNonNull(request, "request cannot be null");
    return execute(
        new ChangeDatabasePasswordApi(
            request.databaseName(), request.username(), request.newPassword()));
  }
}
