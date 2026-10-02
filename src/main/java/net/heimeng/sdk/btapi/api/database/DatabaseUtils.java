package net.heimeng.sdk.btapi.api.database;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.database.DatabaseInfo;

/**
 * 数据库辅助工具类。
 *
 * <p>该工具类基于高层 {@link BtApiManager#database()} 门面提供一些常见查询与轮询能力， 并通过 {@link Optional}
 * 与异常明确区分“未找到”和“请求失败”两类场景。
 */
public final class DatabaseUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtils.class);
  private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(1);
  private static final Duration DEFAULT_DELETE_CONFIRM_TIMEOUT = Duration.ofSeconds(5);

  private DatabaseUtils() {}

  /** 判断指定数据库是否存在。 */
  public static boolean exists(BtApiManager apiManager, String databaseName) {
    return findByName(apiManager, databaseName).isPresent();
  }

  /** 按数据库名称查找数据库信息。 */
  public static Optional<DatabaseInfo> findByName(BtApiManager apiManager, String databaseName) {
    validateApiManager(apiManager);
    String validatedName = validateDatabaseName(databaseName);
    return listDatabases(apiManager).stream()
        .filter(database -> validatedName.equals(database.getName()))
        .findFirst();
  }

  /** 按数据库 ID 查找数据库信息。 */
  public static Optional<DatabaseInfo> findById(BtApiManager apiManager, int databaseId) {
    validateApiManager(apiManager);
    if (databaseId <= 0) {
      throw new IllegalArgumentException("databaseId must be greater than zero");
    }
    return listDatabases(apiManager).stream()
        .filter(database -> database.getId() == databaseId)
        .findFirst();
  }

  /** 在给定超时时间内等待数据库创建完成。 */
  public static Optional<DatabaseInfo> waitForCreation(
      BtApiManager apiManager, String databaseName, Duration timeout) {
    return waitForCreation(apiManager, databaseName, timeout, DEFAULT_POLL_INTERVAL);
  }

  /** 在给定超时时间与轮询间隔下等待数据库创建完成。 */
  public static Optional<DatabaseInfo> waitForCreation(
      BtApiManager apiManager, String databaseName, Duration timeout, Duration pollInterval) {
    validateApiManager(apiManager);
    String validatedName = validateDatabaseName(databaseName);
    Duration validatedTimeout = validateDuration(timeout, "timeout");
    Duration validatedPollInterval = validateDuration(pollInterval, "pollInterval");

    long deadline = System.nanoTime() + validatedTimeout.toNanos();
    while (System.nanoTime() < deadline) {
      Optional<DatabaseInfo> database = findByName(apiManager, validatedName);
      if (database.isPresent()) {
        return database;
      }
      sleep(validatedPollInterval, "等待数据库创建完成");
    }

    return Optional.empty();
  }

  /** 在给定超时时间内等待数据库删除完成。 */
  public static boolean waitForDeletion(
      BtApiManager apiManager, String databaseName, Duration timeout) {
    return waitForDeletion(apiManager, databaseName, timeout, DEFAULT_POLL_INTERVAL);
  }

  /** 在给定超时时间与轮询间隔下等待数据库删除完成。 */
  public static boolean waitForDeletion(
      BtApiManager apiManager, String databaseName, Duration timeout, Duration pollInterval) {
    validateApiManager(apiManager);
    String validatedName = validateDatabaseName(databaseName);
    Duration validatedTimeout = validateDuration(timeout, "timeout");
    Duration validatedPollInterval = validateDuration(pollInterval, "pollInterval");

    long deadline = System.nanoTime() + validatedTimeout.toNanos();
    while (System.nanoTime() < deadline) {
      if (findByName(apiManager, validatedName).isEmpty()) {
        return true;
      }
      sleep(validatedPollInterval, "等待数据库删除完成");
    }

    return false;
  }

  /** 若数据库存在则删除，并在默认超时时间内确认删除结果。 */
  public static boolean deleteIfExists(BtApiManager apiManager, String databaseName) {
    return deleteIfExists(apiManager, databaseName, DEFAULT_DELETE_CONFIRM_TIMEOUT);
  }

  /** 若数据库存在则删除，并在指定超时时间内确认删除结果。 */
  public static boolean deleteIfExists(
      BtApiManager apiManager, String databaseName, Duration confirmationTimeout) {
    validateApiManager(apiManager);
    String validatedName = validateDatabaseName(databaseName);
    Duration validatedTimeout = validateDuration(confirmationTimeout, "confirmationTimeout");

    Optional<DatabaseInfo> database = findByName(apiManager, validatedName);
    if (database.isEmpty()) {
      LOGGER.info("数据库不存在，跳过删除：{}", validatedName);
      return true;
    }

    DatabaseInfo databaseInfo = database.get();
    BtResult<Boolean> deleteResult =
        apiManager.database().delete(validatedName, databaseInfo.getId());
    if (deleteResult == null) {
      throw new BtApiException("Delete database response cannot be null");
    }
    if (!deleteResult.isSuccess()) {
      throw new BtApiException("Failed to delete database: " + deleteResult.getMsg());
    }

    return waitForDeletion(apiManager, validatedName, validatedTimeout);
  }

  private static List<DatabaseInfo> listDatabases(BtApiManager apiManager) {
    BtResult<List<DatabaseInfo>> result = apiManager.database().list();
    if (result == null) {
      throw new BtApiException("Database list response cannot be null");
    }
    if (!result.isSuccess()) {
      throw new BtApiException("Failed to list databases: " + result.getMsg());
    }
    return result.getData() == null
        ? List.of()
        : result.getData().stream().filter(Objects::nonNull).toList();
  }

  private static void validateApiManager(BtApiManager apiManager) {
    Objects.requireNonNull(apiManager, "apiManager cannot be null");
  }

  private static String validateDatabaseName(String databaseName) {
    if (databaseName == null || databaseName.isBlank()) {
      throw new IllegalArgumentException("databaseName cannot be blank");
    }
    return databaseName.trim();
  }

  private static Duration validateDuration(Duration duration, String fieldName) {
    Objects.requireNonNull(duration, fieldName + " cannot be null");
    if (duration.isNegative() || duration.isZero()) {
      throw new IllegalArgumentException(fieldName + " must be greater than zero");
    }
    return duration;
  }

  private static void sleep(Duration duration, String action) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new BtApiException(action + "时线程被中断", exception);
    }
  }
}
