package net.heimeng.sdk.btapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heimeng.sdk.btapi.api.database.GetDatabasesApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.facade.DatabaseCreateRequest;
import net.heimeng.sdk.btapi.facade.DatabaseDeleteRequest;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.database.DatabaseInfo;

@DisplayName("Database integration tests")
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
@Timeout(value = 60, unit = TimeUnit.SECONDS)
class DatabaseIntegrationTest extends AbstractIntegrationTestSupport {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseIntegrationTest.class);

  private BtApiManager apiManager;
  private String testDbName;
  private String testDbUser;
  private String testDbPassword;

  @BeforeEach
  void setUp() {
    assumeConfigurationPresent(ENV_BASE_URL, "baseUrl");
    assumeConfigurationPresent(ENV_API_KEY, "apiKey");
    assumeConfigurationPresent(ENV_TEST_DB_NAME, "test.dbName");
    assumeConfigurationPresent(ENV_TEST_DB_USER, "test.dbUser");
    assumeConfigurationPresent(ENV_TEST_DB_PASSWORD, "test.dbPassword");

    apiManager = createApiManager();
    assumePanelApiAccessible(apiManager);

    String suffix = uniqueSuffix();
    testDbName = getRequiredConfiguration(ENV_TEST_DB_NAME, "test.dbName") + "_" + suffix;
    testDbUser = getRequiredConfiguration(ENV_TEST_DB_USER, "test.dbUser") + "_" + suffix;
    testDbPassword = getRequiredConfiguration(ENV_TEST_DB_PASSWORD, "test.dbPassword");

    logger.info(
        "Database integration test initialized, testDbName={}, testDbUser={}",
        testDbName,
        testDbUser);
  }

  @AfterEach
  void tearDown() {
    try {
      deleteDatabaseIfExists(testDbName);
    } finally {
      closeQuietly(apiManager);
    }
  }

  @Test
  @DisplayName("Should query databases")
  void testGetDatabases() throws BtApiException {
    BtResult<List<DatabaseInfo>> result = apiManager.execute(new GetDatabasesApi());

    assertTrue(result.isSuccess(), "Failed to get databases: " + result.getMsg());
    assertNotNull(result.getData(), "Database list should not be null");
  }

  @Test
  @DisplayName("Should create MySQL database")
  void testCreateDatabase() throws BtApiException {
    assertFalse(isDatabaseExists(testDbName), "Database should not exist before test setup");

    BtResult<Boolean> createResult =
        apiManager
            .database()
            .create(
                DatabaseCreateRequest.builder(testDbName, testDbUser, testDbPassword)
                    .remark("Integration test database")
                    .build());

    assertTrue(createResult.isSuccess(), "Failed to create database: " + createResult.getMsg());
    assertTrue(Boolean.TRUE.equals(createResult.getData()), "Create database should return true");
    assertTrue(isDatabaseExists(testDbName), "Database should exist after creation");

    DatabaseInfo createdDatabase = getDatabaseInfoByName(testDbName);
    assertNotNull(createdDatabase, "Created database should be queryable");
    assertEquals("MySQL", createdDatabase.getType(), "Database type mismatch");
    assertEquals(testDbUser, createdDatabase.getUsername(), "Database username mismatch");
  }

  @Test
  @DisplayName("Should delete database")
  void testDeleteDatabase() throws BtApiException {
    BtResult<Boolean> createResult =
        apiManager
            .database()
            .create(DatabaseCreateRequest.builder(testDbName, testDbUser, testDbPassword).build());
    assertTrue(createResult.isSuccess(), "Failed to prepare database: " + createResult.getMsg());

    DatabaseInfo databaseInfo = getDatabaseInfoByName(testDbName);
    assertNotNull(databaseInfo, "Database should exist before delete");

    BtResult<Boolean> deleteResult =
        apiManager.database().delete(new DatabaseDeleteRequest(testDbName, databaseInfo.getId()));

    assertTrue(deleteResult.isSuccess(), "Failed to delete database: " + deleteResult.getMsg());
    assertTrue(Boolean.TRUE.equals(deleteResult.getData()), "Delete database should return true");
    assertFalse(isDatabaseExists(testDbName), "Database should no longer exist");
  }

  @Test
  @DisplayName("Should load database integration configuration")
  void testConfigurationLoading() {
    assertNotNull(getOptionalConfiguration(ENV_BASE_URL, "baseUrl"), "baseUrl missing");
    assertNotNull(getOptionalConfiguration(ENV_API_KEY, "apiKey"), "apiKey missing");
    assertNotNull(getOptionalConfiguration(ENV_TEST_DB_NAME, "test.dbName"), "test.dbName missing");
    assertNotNull(getOptionalConfiguration(ENV_TEST_DB_USER, "test.dbUser"), "test.dbUser missing");
    assertNotNull(
        getOptionalConfiguration(ENV_TEST_DB_PASSWORD, "test.dbPassword"),
        "test.dbPassword missing");
  }

  private boolean isDatabaseExists(String databaseName) {
    return getDatabaseInfoByName(databaseName) != null;
  }

  private DatabaseInfo getDatabaseInfoByName(String databaseName) {
    if (databaseName == null || databaseName.isBlank()) {
      return null;
    }

    try {
      BtResult<List<DatabaseInfo>> result = apiManager.execute(new GetDatabasesApi());
      if (!result.isSuccess() || result.getData() == null) {
        return null;
      }
      return result.getData().stream()
          .filter(databaseInfo -> databaseName.equals(databaseInfo.getName()))
          .findFirst()
          .orElse(null);
    } catch (BtApiException exception) {
      logger.warn(
          "Failed to query database info, databaseName={}, reason={}",
          databaseName,
          exception.getMessage());
      return null;
    }
  }

  private void deleteDatabaseIfExists(String databaseName) {
    try {
      DatabaseInfo databaseInfo = getDatabaseInfoByName(databaseName);
      if (databaseInfo == null) {
        return;
      }

      BtResult<Boolean> result =
          apiManager.database().delete(new DatabaseDeleteRequest(databaseName, databaseInfo.getId()));
      if (!result.isSuccess()) {
        logger.warn("Database cleanup failed, databaseName={}, reason={}", databaseName, result.getMsg());
      }
    } catch (Exception exception) {
      logger.warn(
          "Database cleanup raised an exception, databaseName={}, reason={}",
          databaseName,
          exception.getMessage());
    }
  }
}
