package net.heimeng.sdk.btapi.integration;

import net.heimeng.sdk.btapi.api.system.GetSystemInfoApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.client.BtClientFactory;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.exception.BtAuthenticationException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.SystemInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GetSystemInfoApi的集成测试类
 * <p>
 * 这些测试会实际调用宝塔面板API，需要配置正确的连接信息
 * </p>
 */
@DisplayName("获取系统信息API集成测试")
@Disabled("默认禁用集成测试，避免不必要的API调用")
public class GetSystemInfoApiIT {

    private static final String BASE_URL = System.getenv("BT_PANEL_URL") != null ? 
            System.getenv("BT_PANEL_URL") : "http://localhost:8888";
    private static final String API_KEY = System.getenv("BT_PANEL_API_KEY") != null ? 
            System.getenv("BT_PANEL_API_KEY") : "your_api_key_here";

    private BtClient client;
    private BtApiManager apiManager;

    @BeforeEach
    void setUp() {
        // 创建客户端和API管理器
        BtSdkConfig config = BtSdkConfig.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .connectTimeout(5)
                .readTimeout(10)
                .build();
        client = BtClientFactory.createClient(config);
        apiManager = new BtApiManager(client);
    }

    @AfterEach
    void tearDown() {
        // 关闭客户端资源
        if (apiManager != null) {
            apiManager.close();
        }
    }

    @Test
    @DisplayName("测试获取系统信息成功响应")
    void testExecute_Success() throws BtApiException {
        // 创建API实例
        GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
        
        // 执行API调用
        BtResult<SystemInfo> result = apiManager.execute(systemInfoApi);
        
        // 验证结果
        assertNotNull(result, "响应结果不应为null");
        assertTrue(result.isSuccess(), "API调用应成功");
        
        // 验证返回的数据
        SystemInfo systemInfo = result.getData();
        assertNotNull(systemInfo, "系统信息数据不应为null");
        assertNotNull(systemInfo.getOs(), "操作系统信息不应为null");
        assertTrue(systemInfo.getCpuUsage() >= 0 && systemInfo.getCpuUsage() <= 100, 
                "CPU使用率应在0-100之间");
        assertTrue(systemInfo.getMemoryTotal() >= 0, "内存总量应大于等于0");
        assertTrue(systemInfo.getMemoryUsed() >= 0, "已用内存应大于等于0");
        assertNotNull(systemInfo.getPanelVersion(), "面板版本不应为null");
    }

    @Test
    @DisplayName("测试使用无效API密钥时的异常处理")
    void testExecute_InvalidApiKey() {
        // 使用无效的API密钥创建客户端
        BtClient invalidClient = BtClientFactory.createClient(BASE_URL, "invalid_api_key");
        BtApiManager invalidApiManager = new BtApiManager(invalidClient);
        
        // 创建API实例
        GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
        
        // 验证异常抛出
        BtApiException exception = assertThrows(BtAuthenticationException.class,
                () -> invalidApiManager.execute(systemInfoApi), 
                "使用无效API密钥时应抛出BtAuthenticationException");
        
        // 清理资源
        invalidApiManager.close();
    }

    @Test
    @DisplayName("测试使用无效URL时的异常处理")
    void testExecute_InvalidUrl() {
        // 使用无效的URL创建客户端
        BtClient invalidClient = BtClientFactory.createClient("http://invalid-url:8888", API_KEY);
        BtApiManager invalidApiManager = new BtApiManager(invalidClient);
        
        // 创建API实例
        GetSystemInfoApi systemInfoApi = new GetSystemInfoApi();
        
        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, 
                () -> invalidApiManager.execute(systemInfoApi), 
                "使用无效URL时应抛出异常");
        
        // 清理资源
        invalidApiManager.close();
    }
}