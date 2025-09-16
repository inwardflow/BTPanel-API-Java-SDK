package net.heimeng.sdk.btapi.integration;

import static org.junit.jupiter.api.Assertions.*;

import net.heimeng.sdk.btapi.api.database.CreateDatabaseApi;
import net.heimeng.sdk.btapi.api.database.DeleteDatabaseApi;
import net.heimeng.sdk.btapi.api.database.GetDatabasesApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.client.BtClientFactory;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.database.DatabaseInfo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 数据库管理API集成测试类
 * <p>注意：这些测试会实际调用宝塔面板API，可能会产生实际效果</p>
 *
 * <p>
 * 测试运行条件：
 * 1. 需要设置环境变量ENABLE_INTEGRATION_TESTS=true来启用集成测试
 * 2. 需要提供有效的宝塔面板API配置（baseUrl和apiKey）
 * </p>
 *
 * <p>CI/CD最佳实践：
 * - 测试默认禁用，避免在日常开发中意外执行
 * - 可以通过环境变量控制测试执行
 * - 测试资源会自动清理
 * - 测试相互隔离，不依赖于执行顺序
 * - 测试有明确的超时限制
 * </p>
 *
 * <p>测试策略：
 * - 每个测试方法都有前置检查和后置验证
 * - 测试数据使用随机后缀避免冲突
 * - 即使测试失败也会尝试清理资源
 * - 避免测试间依赖，每个测试独立管理资源
 * </p>
 */
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_METHOD) // 每个测试独立实例，避免状态共享
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public class DatabaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseIntegrationTest.class);

    private static final String PROPERTIES_FILE = "application-test.properties";
    private static final String ENV_BASE_URL = "baseUrl";
    private static final String ENV_API_KEY = "apiKey";
    private static final String ENV_TEST_DB_NAME = "test.dbName";
    private static final String ENV_TEST_DB_USER = "test.dbUser";
    private static final String ENV_TEST_DB_PASSWORD = "test.dbPassword";

    private BtClient client;
    private BtApiManager apiManager;
    private Properties testProperties;
    private String testDbName;
    private String testDbUser;
    private String testDbPassword;
    private String testDbId;

    @BeforeEach
    void setUp() throws IOException {
        // 1. 加载测试配置
        testProperties = new Properties();
        try (var stream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (stream == null) {
                throw new IOException("配置文件 " + PROPERTIES_FILE + " 不存在");
            }
            testProperties.load(stream);
        } catch (IOException e) {
            logger.error("无法加载测试配置文件 {}", PROPERTIES_FILE, e);
            throw e;
        }

        // 2. 验证必要配置
        testDbName = getConfiguration(ENV_TEST_DB_NAME);
        testDbUser = getConfiguration(ENV_TEST_DB_USER);
        testDbPassword = getConfiguration(ENV_TEST_DB_PASSWORD);
        String baseUrl = getConfiguration(ENV_BASE_URL);
        String apiKey = getConfiguration(ENV_API_KEY);

        assertNotNull(baseUrl, "baseUrl配置不能为空");
        assertNotNull(apiKey, "apiKey配置不能为空");
        assertNotNull(testDbName, "test.dbName配置不能为空");
        assertNotNull(testDbUser, "test.dbUser配置不能为空");
        assertNotNull(testDbPassword, "test.dbPassword配置不能为空");

        // 3. 生成随机ID避免冲突
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        testDbName = testDbName + "_" + randomId;
        testDbUser = testDbUser + "_" + randomId;

        logger.info("测试配置：baseUrl=****, testDbName={}, testDbUser={}", testDbName, testDbUser);

        // 4. 创建客户端
        BtSdkConfig config = BtSdkConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .connectTimeout(Integer.parseInt(testProperties.getProperty("connectTimeout", "5000")))
                .readTimeout(Integer.parseInt(testProperties.getProperty("readTimeout", "10000")))
                .retryCount(Integer.parseInt(testProperties.getProperty("retryCount", "3")))
                .build();

        client = BtClientFactory.createClient(config);
        apiManager = new BtApiManager(client);
    }

    @AfterEach
    void tearDown() {
        // 清理测试创建的数据库（即使测试失败也会执行）
        if (apiManager != null && testDbName != null) {
            try {
                if (isDatabaseExists(testDbName)) {
                    logger.info("测试后清理数据库: {}", testDbName);
                    DatabaseInfo dbInfo = getDatabaseInfoByName(testDbName);
                    if (dbInfo != null) {
                        DeleteDatabaseApi deleteApi = new DeleteDatabaseApi(testDbName, dbInfo.getId());
                        BtResult<Boolean> result = apiManager.execute(deleteApi);
                        if (result.isSuccess()) {
                            logger.info("测试数据库清理成功: {}", testDbName);
                        } else {
                            logger.warn("测试数据库清理失败: {}, 原因: {}", testDbName, result.getMsg());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("测试数据库清理时发生异常", e);
            } finally {
                // 确保资源释放
                try {
                    if (apiManager != null) {
                        apiManager.close();
                    }
                } catch (Exception e) {
                    logger.error("关闭API管理器时发生异常", e);
                }
            }
        }
        // 清空引用以便垃圾回收
        client = null;
        apiManager = null;
        testProperties = null;
    }

    private String getConfiguration(String key) {
        String value = System.getenv(key);
        if (value == null && testProperties != null) {
            value = testProperties.getProperty(key);
        }
        return value;
    }

    @Test
    @DisplayName("测试获取数据库列表API")
    void testGetDatabases() {
        GetDatabasesApi databasesApi = new GetDatabasesApi();
        BtResult<List<DatabaseInfo>> result = apiManager.execute(databasesApi);

        assertTrue(result.isSuccess(), "获取数据库列表失败: " + result.getMsg());
        assertNotNull(result.getData(), "返回数据不应为null");
        // 允许空列表（测试环境可能无数据库）
        logger.info("获取数据库列表成功，共{}个数据库", result.getData() != null ? result.getData().size() : 0);
    }

    @Test
    @DisplayName("测试创建MySQL数据库API")
    void testCreateDatabase() {
        // 1. 确保测试前数据库不存在
        assertFalse(isDatabaseExists(testDbName), "测试前数据库应不存在");

        // 2. 创建数据库
        CreateDatabaseApi createApi = CreateDatabaseApi.builder(testDbName, testDbUser, testDbPassword)
                .withCharset("utf8mb4")
                .withDatabaseType(CreateDatabaseApi.DatabaseType.MYSQL)
                .withNote("测试数据库备注")
                .build();

        BtResult<Boolean> createResult = apiManager.execute(createApi);
        assertTrue(createResult.isSuccess(), "创建数据库失败: " + createResult.getMsg());
        assertTrue(createResult.getData(), "创建操作返回失败");

        // 3. 验证创建结果
        assertTrue(isDatabaseExists(testDbName), "创建后数据库应存在");
        DatabaseInfo createdDb = getDatabaseInfoByName(testDbName);
        assertNotNull(createdDb, "创建的数据库信息应存在");
        assertEquals("MySQL", createdDb.getType(), "数据库类型应为MySQL");
        assertEquals(testDbUser, createdDb.getUsername(), "数据库用户应匹配");
        // 注意：API可能不返回备注信息，跳过备注验证
    }

    @Test
    @DisplayName("测试删除数据库API")
    void testDeleteDatabase() {
        // 1. 确保测试前数据库存在
        if (!isDatabaseExists(testDbName)) {
            CreateDatabaseApi createApi = CreateDatabaseApi.builder(testDbName, testDbUser, testDbPassword)
                    .withCharset("utf8mb4")
                    .withDatabaseType(CreateDatabaseApi.DatabaseType.MYSQL)
                    .build();
            BtResult<Boolean> createResult = apiManager.execute(createApi);
            assertTrue(createResult.isSuccess(), "测试前创建数据库失败: " + createResult.getMsg());
        }

        // 2. 获取数据库ID
        DatabaseInfo dbInfo = getDatabaseInfoByName(testDbName);
        assertNotNull(dbInfo, "测试数据库应存在");

        // 3. 执行删除
        DeleteDatabaseApi deleteApi = new DeleteDatabaseApi(testDbName, dbInfo.getId());
        BtResult<Boolean> deleteResult = apiManager.execute(deleteApi);
        assertTrue(deleteResult.isSuccess(), "删除数据库失败: " + deleteResult.getMsg());
        assertTrue(deleteResult.getData(), "删除操作返回失败");

        // 4. 验证删除结果
        assertFalse(isDatabaseExists(testDbName), "删除后数据库应不存在");

        // 5. 验证幂等性：重复删除应成功
        BtResult<Boolean> deleteAgainResult = apiManager.execute(deleteApi);
        assertTrue(deleteAgainResult.isSuccess(), "重复删除应幂等成功");
    }

    private boolean isDatabaseExists(String dbName) {
        if (dbName == null || apiManager == null) {
            return false;
        }

        try {
            GetDatabasesApi getApi = new GetDatabasesApi();
            BtResult<List<DatabaseInfo>> result = apiManager.execute(getApi);
            return result.isSuccess() && result.getData() != null &&
                    result.getData().stream().anyMatch(db -> dbName.equals(db.getName()));
        } catch (BtApiException e) {
            logger.error("检查数据库是否存在时发生异常: {}", e.getMessage());
            return false;
        }
    }

    private DatabaseInfo getDatabaseInfoByName(String dbName) {
        if (dbName == null || apiManager == null) {
            return null;
        }

        try {
            GetDatabasesApi getApi = new GetDatabasesApi();
            BtResult<List<DatabaseInfo>> result = apiManager.execute(getApi);
            if (result.isSuccess() && result.getData() != null) {
                return result.getData().stream()
                        .filter(db -> dbName.equals(db.getName()))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (BtApiException e) {
            logger.error("获取数据库信息时发生异常: {}", e.getMessage());
            return null;
        }
    }

    @Test
    @DisplayName("测试配置加载功能")
    void testConfigurationLoading() {
        assertNotNull(testProperties, "测试配置文件应加载成功");
        assertNotNull(getConfiguration(ENV_BASE_URL), "baseUrl配置缺失");
        assertNotNull(getConfiguration(ENV_API_KEY), "apiKey配置缺失");
        assertNotNull(getConfiguration(ENV_TEST_DB_NAME), "test.dbName配置缺失");
        assertNotNull(getConfiguration(ENV_TEST_DB_USER), "test.dbUser配置缺失");
        assertNotNull(getConfiguration(ENV_TEST_DB_PASSWORD), "test.dbPassword配置缺失");
    }
}