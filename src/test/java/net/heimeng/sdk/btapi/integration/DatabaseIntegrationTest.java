package net.heimeng.sdk.btapi.integration;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据库管理API集成测试类
 * <p>注意：这些测试会实际调用宝塔面板API，可能会产生实际效果</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("默认禁用集成测试，避免不必要的API调用")
public class DatabaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseIntegrationTest.class);

    private BtClient client;
    private BtApiManager apiManager;
    private Properties testProperties;
    
    private String testDbName;
    private String testDbUser;
    private String testDbPassword;

    @BeforeEach
    void setUp() throws IOException {
        // 加载测试配置
        testProperties = new Properties();
        testProperties.load(getClass().getClassLoader().getResourceAsStream("application-test.properties"));
        
        // 从配置文件或环境变量获取配置
        String baseUrl = System.getenv().getOrDefault("baseUrl", testProperties.getProperty("baseUrl"));
        String apiKey = System.getenv().getOrDefault("apiKey", testProperties.getProperty("apiKey"));
        String originalDbName = System.getenv().getOrDefault("test.dbName", testProperties.getProperty("test.dbName"));
        String originalDbUser = System.getenv().getOrDefault("test.dbUser", testProperties.getProperty("test.dbUser"));
        testDbPassword = System.getenv().getOrDefault("test.dbPassword", testProperties.getProperty("test.dbPassword"));
        
        // 生成随机ID避免冲突
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        testDbName = originalDbName + "_" + randomId;
        testDbUser = originalDbUser + "_" + randomId;
        
        logger.info("测试配置：baseUrl={}, testDbName={}, testDbUser={}", baseUrl, testDbName, testDbUser);
        
        // 创建配置对象
        BtSdkConfig config = BtSdkConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .connectTimeout(Integer.parseInt(testProperties.getProperty("connectTimeout")))
                .readTimeout(Integer.parseInt(testProperties.getProperty("readTimeout")))
                .retryCount(Integer.parseInt(testProperties.getProperty("retryCount")))
                .build();
        
        // 创建客户端和API管理器
        client = BtClientFactory.createClient(config);
        apiManager = new BtApiManager(client);
    }

    @AfterEach
    void tearDown() {
        if (apiManager != null) {
            apiManager.close();
        }
    }

    /**
     * 测试获取数据库列表
     */
    @Test
    void testGetDatabases() {
        logger.info("开始测试获取数据库列表");
        
        try {
            // 创建并配置API
            GetDatabasesApi databasesApi = new GetDatabasesApi();
            
            // 执行API调用
            BtResult<List<DatabaseInfo>> result = apiManager.execute(databasesApi);
            
            // 验证结果
            assertTrue(result.isSuccess(), "获取数据库列表失败: " + result.getMsg());
            assertNotNull(result.getData(), "返回数据不能为空");
            
            logger.info("获取数据库列表成功，共{}个数据库", result.getData().size());
        } catch (BtApiException e) {
            logger.error("获取数据库列表发生API异常", e);
            fail("获取数据库列表发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("获取数据库列表发生未预期异常", e);
            fail("获取数据库列表发生未预期异常: " + e.getMessage());
        }
    }

    /**
     * 测试创建数据库（会创建实际数据库）
     */
    @Test
    void testCreateDatabase() {
        logger.info("开始测试创建数据库: {}", testDbName);
        
        try {
            // 创建并配置API
            CreateDatabaseApi createDatabaseApi = new CreateDatabaseApi(testDbName, testDbUser, testDbPassword)
                    .setCharset("utf8mb4")
                    .setDatabaseType(CreateDatabaseApi.TYPE_MYSQL)
                    .setNote("测试数据库备注");
            
            // 执行API调用
            BtResult<Boolean> result = apiManager.execute(createDatabaseApi);
            
            // 验证结果
            assertTrue(result.isSuccess(), "创建数据库失败: " + result.getMsg());
            assertTrue(result.getData(), "创建数据库操作返回失败");
            
            logger.info("创建数据库成功: {}", testDbName);
        } catch (BtApiException e) {
            logger.error("创建数据库发生API异常", e);
            fail("创建数据库发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("创建数据库发生未预期异常", e);
            fail("创建数据库发生未预期异常: " + e.getMessage());
        }
    }

    /**
     * 测试删除数据库（会删除实际数据库）
     * 注意：需要先创建数据库才能测试删除
     */
    @Test
    void testDeleteDatabase() {
        // 先创建数据库
        testCreateDatabase();
        
        logger.info("开始测试删除数据库: {}", testDbName);
        
        try {
            // 创建并配置API
            DeleteDatabaseApi deleteDatabaseApi = new DeleteDatabaseApi(testDbName);
            
            // 执行API调用
            BtResult<Boolean> result = apiManager.execute(deleteDatabaseApi);
            
            // 验证结果
            assertTrue(result.isSuccess(), "删除数据库失败: " + result.getMsg());
            assertTrue(result.getData(), "删除数据库操作返回失败");
            
            logger.info("删除数据库成功: {}", testDbName);
        } catch (BtApiException e) {
            logger.error("删除数据库发生API异常", e);
            fail("删除数据库发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("删除数据库发生未预期异常", e);
            fail("删除数据库发生未预期异常: " + e.getMessage());
        }
    }

    /**
     * 检查数据库是否存在
     */
    private boolean isDatabaseExists(String dbName) throws BtApiException {
        GetDatabasesApi databasesApi = new GetDatabasesApi();
        BtResult<List<DatabaseInfo>> result = apiManager.execute(databasesApi);
        
        if (result.isSuccess() && result.getData() != null) {
            for (DatabaseInfo database : result.getData()) {
                if (dbName.equals(database.getName())) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 启用测试环境变量检查
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
    void testEnvironmentVariableEnabled() {
        assertTrue(true, "集成测试环境变量已启用");
    }
}