package net.heimeng.sdk.btapi.integration;

import net.heimeng.sdk.btapi.api.website.CreateWebsiteApi;
import net.heimeng.sdk.btapi.api.website.DeleteWebsiteApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteListApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.client.BtClientFactory;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.CreateWebsiteResult;
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
 * 网站管理API集成测试类
 * <p>注意：这些测试会实际调用宝塔面板API，可能会产生实际效果</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("默认禁用集成测试，避免不必要的API调用")
public class WebsiteIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(WebsiteIntegrationTest.class);

    private BtClient client;
    private BtApiManager apiManager;
    private Properties testProperties;
    
    private String testDomain;
    private String testWebroot;

    @BeforeEach
    void setUp() throws IOException {
        // 加载测试配置
        testProperties = new Properties();
        testProperties.load(getClass().getClassLoader().getResourceAsStream("application-test.properties"));
        
        // 从配置文件或环境变量获取配置
        String baseUrl = System.getenv().getOrDefault("baseUrl", testProperties.getProperty("baseUrl"));
        String apiKey = System.getenv().getOrDefault("apiKey", testProperties.getProperty("apiKey"));
        testDomain = System.getenv().getOrDefault("test.domain", testProperties.getProperty("test.domain"));
        testWebroot = System.getenv().getOrDefault("test.webroot", testProperties.getProperty("test.webroot"));
        
        // 生成随机子域名避免冲突
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        testDomain = randomId + "." + testDomain;
        testWebroot = testWebroot.replaceFirst("\\w+\\.\\w+$", randomId + ".example.com");
        
        logger.info("测试配置：baseUrl={}, testDomain={}, testWebroot={}", baseUrl, testDomain, testWebroot);
        
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
     * 测试获取网站列表
     */
    @Test
    void testGetWebsiteList() {
        logger.info("开始测试获取网站列表");
        
        try {
            // 创建并配置API
            GetWebsiteListApi websiteListApi = new GetWebsiteListApi()
                    .setPage(1)
                    .setLimit(20);
            
            // 执行API调用
            BtResult<List<Map<String, Object>>> result = apiManager.execute(websiteListApi);
            
            // 验证结果
            assertTrue(result.isSuccess(), "获取网站列表失败: " + result.getMsg());
            assertNotNull(result.getData(), "返回数据不能为空");
            assertTrue(result.getData().size() > 0, "网站列表不能为空");
            
            logger.info("获取网站列表成功，共{}个网站", result.getData().size());
        } catch (BtApiException e) {
            logger.error("获取网站列表发生API异常", e);
            fail("获取网站列表发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("获取网站列表发生未预期异常", e);
            fail("获取网站列表发生未预期异常: " + e.getMessage());
        }
    }

    /**
     * 测试创建网站（会创建实际网站）
     */
    @Test
    void testCreateWebsite() {
        logger.info("开始测试创建网站: {}", testDomain);
        
        try {
            // 创建并配置API - 分类ID默认为0（未分类），端口默认为80
            CreateWebsiteApi createWebsiteApi = new CreateWebsiteApi(testDomain, testWebroot, 0, "7.4", 80, "测试网站")
                    .setType("PHP")
                    .setCreateFtp(false)
                    .setCreateDatabase(false);
            
            // 执行API调用
            BtResult<CreateWebsiteResult> result = apiManager.execute(createWebsiteApi);
            
            // 验证结果
            assertTrue(result.isSuccess(), "创建网站失败: " + result.getMsg());
            assertNotNull(result.getData(), "返回数据不能为空");
            assertTrue(result.getData().isSiteStatus(), "网站创建状态为失败");
            
            logger.info("创建网站成功: {}", testDomain);
        } catch (BtApiException e) {
            logger.error("创建网站发生API异常", e);
            fail("创建网站发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("创建网站发生未预期异常", e);
            fail("创建网站发生未预期异常: " + e.getMessage());
        }
    }

    /**
     * 测试删除网站（会删除实际网站）
     * 注意：需要先创建网站才能测试删除
     */
    @Test
    void testDeleteWebsite() {
        // 先创建网站
        testCreateWebsite();
        
        logger.info("开始测试删除网站: {}", testDomain);
        
        try {
            // 创建并配置API
            DeleteWebsiteApi deleteWebsiteApi = new DeleteWebsiteApi(testDomain)
                    .setDelFiles(true)
                    .setDelDatabase(false);
            
            // 执行API调用
            BtResult<Boolean> result = apiManager.execute(deleteWebsiteApi);
            
            // 验证结果
            assertTrue(result.isSuccess(), "删除网站失败: " + result.getMsg());
            assertTrue(result.getData(), "删除网站操作返回失败");
            
            logger.info("删除网站成功: {}", testDomain);
        } catch (BtApiException e) {
            logger.error("删除网站发生API异常", e);
            fail("删除网站发生API异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("删除网站发生未预期异常", e);
            fail("删除网站发生未预期异常: " + e.getMessage());
        }
    }

    /**
     * 根据域名获取网站ID
     */
    private Integer getWebsiteIdByName(String domain) throws BtApiException {
        GetWebsiteListApi websiteListApi = new GetWebsiteListApi()
                .setPage(1)
                .setLimit(100);
        
        // 注意：根据GetWebsiteListApi的实现，它直接返回网站列表，而不是包含data和total字段的嵌套结构
        BtResult<List<Map<String, Object>>> result = apiManager.execute(websiteListApi);
        
        if (result.isSuccess() && result.getData() != null) {
            for (Map<String, Object> website : result.getData()) {
                if (domain.equals(website.get("name"))) {
                    return (Integer) website.get("id");
                }
            }
        }
        
        return null;
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