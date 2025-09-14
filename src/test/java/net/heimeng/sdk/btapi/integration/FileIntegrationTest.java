package net.heimeng.sdk.btapi.integration;

import net.heimeng.sdk.btapi.api.file.CreateFileDirectoryApi;
import net.heimeng.sdk.btapi.api.file.DeleteFileApi;
import net.heimeng.sdk.btapi.api.file.GetFileContentApi;
import net.heimeng.sdk.btapi.api.file.SaveFileContentApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.client.BtClientFactory;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件管理API集成测试类
 * <p>注意：这些测试会实际调用宝塔面板API，可能会产生实际效果</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("默认禁用集成测试，避免不必要的API调用")
public class FileIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(FileIntegrationTest.class);

    private BtClient client;
    private BtApiManager apiManager;
    private Properties testProperties;
    
    private String testFilePath;
    private String testDirectoryPath;
    private String testContent = "这是测试文件内容 - " + UUID.randomUUID().toString();

    @BeforeEach
    void setUp() throws IOException {
        // 加载测试配置
        testProperties = new Properties();
        testProperties.load(getClass().getClassLoader().getResourceAsStream("application-test.properties"));
        
        // 从配置文件或环境变量获取配置
        String baseUrl = System.getenv().getOrDefault("baseUrl", testProperties.getProperty("baseUrl"));
        String apiKey = System.getenv().getOrDefault("apiKey", testProperties.getProperty("apiKey"));
        String originalFilePath = System.getenv().getOrDefault("test.filePath", testProperties.getProperty("test.filePath"));
        
        // 生成随机ID避免冲突
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        testFilePath = originalFilePath.replace(".txt", "_" + randomId + ".txt");
        testDirectoryPath = testFilePath.replace("test_" + randomId + ".txt", "test_dir_" + randomId);
        
        logger.info("测试配置：baseUrl={}, testFilePath={}, testDirectoryPath={}", 
                baseUrl, testFilePath, testDirectoryPath);
        
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
        // 清理测试文件和目录
        try {
            DeleteFileApi deleteFileApi = new DeleteFileApi().setPath(testFilePath);
            apiManager.execute(deleteFileApi);
            
            DeleteFileApi deleteDirApi = new DeleteFileApi().setPath(testDirectoryPath);
            apiManager.execute(deleteDirApi);
        } catch (BtApiException e) {
            logger.warn("清理测试文件/目录时发生异常: {}", e.getMessage());
        }
        
        if (apiManager != null) {
            apiManager.close();
        }
    }

    /**
     * 测试创建目录
     */
    @Test
    void testCreateDirectory() {
        logger.info("开始测试创建目录: {}", testDirectoryPath);
        
        try {
            // 创建并配置API
            CreateFileDirectoryApi createDirApi = new CreateFileDirectoryApi()
                    .setPath(testDirectoryPath);
            
            // 执行API调用
            BtResult<Boolean> result = apiManager.execute(createDirApi);
            
            // 验证结果
            assertTrue(result.isSuccess(), "创建目录失败: " + result.getMsg());
            assertTrue(result.getData(), "创建目录操作返回失败");
            
            logger.info("创建目录成功: {}", testDirectoryPath);
        } catch (BtApiException e) {
            logger.error("创建目录发生API异常", e);
            fail("创建目录发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("创建目录发生未预期异常", e);
            fail("创建目录发生未预期异常: " + e.getMessage());
        }
    }

    /**
     * 测试保存文件内容
     */
    @Test
    void testSaveFileContent() {
        logger.info("开始测试保存文件内容: {}", testFilePath);
        
        try {
            // 创建并配置API
            SaveFileContentApi saveFileApi = new SaveFileContentApi()
                    .setPath(testFilePath)
                    .setData(testContent);
            
            // 执行API调用
            BtResult<Boolean> result = apiManager.execute(saveFileApi);
            
            // 验证结果
            assertTrue(result.isSuccess(), "保存文件内容失败: " + result.getMsg());
            assertTrue(result.getData(), "保存文件内容操作返回失败");
            
            logger.info("保存文件内容成功: {}", testFilePath);
        } catch (BtApiException e) {
            logger.error("保存文件内容发生API异常", e);
            fail("保存文件内容发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("保存文件内容发生未预期异常", e);
            fail("保存文件内容发生未预期异常: " + e.getMessage());
        }
    }

    /**
     * 测试获取文件内容
     * 注意：需要先保存文件内容才能测试读取
     */
    @Test
    void testGetFileContent() {
        // 先保存文件内容
        testSaveFileContent();
        
        logger.info("开始测试获取文件内容: {}", testFilePath);
        
        try {
            // 创建并配置API
            GetFileContentApi getFileApi = new GetFileContentApi()
                    .setPath(testFilePath);
            
            // 执行API调用
            BtResult<String> result = apiManager.execute(getFileApi);
            
            // 验证结果
            assertTrue(result.isSuccess(), "获取文件内容失败: " + result.getMsg());
            assertNotNull(result.getData(), "返回的文件内容不能为空");
            assertEquals(testContent, result.getData(), "返回的文件内容与预期不符");
            
            logger.info("获取文件内容成功，内容长度: {} 字符", result.getData().length());
        } catch (BtApiException e) {
            logger.error("获取文件内容发生API异常", e);
            fail("获取文件内容发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("获取文件内容发生未预期异常", e);
            fail("获取文件内容发生未预期异常: " + e.getMessage());
        }
    }

    /**
     * 测试删除文件
     * 注意：需要先保存文件内容才能测试删除
     */
    @Test
    void testDeleteFile() {
        // 先保存文件内容
        testSaveFileContent();
        
        logger.info("开始测试删除文件: {}", testFilePath);
        
        try {
            // 创建并配置API
            DeleteFileApi deleteFileApi = new DeleteFileApi()
                    .setPath(testFilePath);
            
            // 执行API调用
            BtResult<Boolean> result = apiManager.execute(deleteFileApi);
            
            // 验证结果
            assertTrue(result.isSuccess(), "删除文件失败: " + result.getMsg());
            assertTrue(result.getData(), "删除文件操作返回失败");
            
            logger.info("删除文件成功: {}", testFilePath);
        } catch (BtApiException e) {
            logger.error("删除文件发生API异常", e);
            fail("删除文件发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("删除文件发生未预期异常", e);
            fail("删除文件发生未预期异常: " + e.getMessage());
        }
    }

    /**
     * 完整的文件操作流程测试
     */
    @Test
    void testFileOperationFlow() {
        logger.info("开始测试完整的文件操作流程");
        
        try {
            // 1. 创建目录
            testCreateDirectory();
            
            // 2. 在目录中创建文件
            String fileInDir = testDirectoryPath + "/test_file_in_dir.txt";
            SaveFileContentApi saveFileApi = new SaveFileContentApi()
                    .setPath(fileInDir)
                    .setData("这是目录中的测试文件内容");
            BtResult<Boolean> saveResult = apiManager.execute(saveFileApi);
            assertTrue(saveResult.isSuccess(), "在目录中创建文件失败: " + saveResult.getMsg());
            
            // 3. 读取文件内容
            GetFileContentApi getFileApi = new GetFileContentApi().setPath(fileInDir);
            BtResult<String> getResult = apiManager.execute(getFileApi);
            assertTrue(getResult.isSuccess(), "读取目录中的文件内容失败: " + getResult.getMsg());
            assertEquals("这是目录中的测试文件内容", getResult.getData(), "文件内容与预期不符");
            
            // 4. 删除文件
            DeleteFileApi deleteFileApi = new DeleteFileApi().setPath(fileInDir);
            BtResult<Boolean> deleteFileResult = apiManager.execute(deleteFileApi);
            assertTrue(deleteFileResult.isSuccess(), "删除目录中的文件失败: " + deleteFileResult.getMsg());
            
            // 5. 删除目录
            DeleteFileApi deleteDirApi = new DeleteFileApi().setPath(testDirectoryPath);
            BtResult<Boolean> deleteDirResult = apiManager.execute(deleteDirApi);
            assertTrue(deleteDirResult.isSuccess(), "删除目录失败: " + deleteDirResult.getMsg());
            
            logger.info("完整的文件操作流程测试成功");
        } catch (BtApiException e) {
            logger.error("文件操作流程测试发生API异常", e);
            fail("文件操作流程测试发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("文件操作流程测试发生未预期异常", e);
            fail("文件操作流程测试发生未预期异常: " + e.getMessage());
        }
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