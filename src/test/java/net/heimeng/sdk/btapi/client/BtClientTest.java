package net.heimeng.sdk.btapi.client;

import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * BtClient的单元测试类
 * <p>
 * 测试宝塔客户端的创建、配置和基础功能
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("宝塔客户端单元测试")
public class BtClientTest {

    @Mock
    private BtClient mockClient;

    @Test
    @DisplayName("测试使用构造函数创建客户端")
    void testConstructor() {
        // 使用构造函数创建客户端
        BtSdkConfig config = BtSdkConfig.builder()
                .baseUrl("http://localhost:8888")
                .apiKey("test_api_key")
                .build();
        
        BtClient client = new DefaultBtClient(config);
        
        // 验证客户端创建成功
        assertNotNull(client);
    }

    @Test
    @DisplayName("测试使用工厂方法创建客户端")
    void testFactoryCreateClient() {
        // 使用工厂方法创建客户端
        BtClient client = BtClientFactory.createClient("http://localhost:8888", "test_api_key");
        
        // 验证客户端创建成功
        assertNotNull(client);
    }

    @Test
    @DisplayName("测试使用自定义配置创建客户端")
    void testFactoryCreateClientWithConfig() {
        // 创建自定义配置
        BtSdkConfig config = BtSdkConfig.builder()
                .baseUrl("http://localhost:8888")
                .apiKey("test_api_key")
                .connectTimeout(10)
                .readTimeout(20)
                .build();
        
        // 使用工厂方法创建客户端
        BtClient client = BtClientFactory.createClient(config);
        
        // 验证客户端创建成功
        assertNotNull(client);
    }

    @Test
    @DisplayName("测试创建客户端时URL格式验证")
    void testCreateClientWithInvalidUrl() {
        // 测试使用无效URL创建客户端
        assertThrows(BtApiException.class, () -> {
            BtClientFactory.createClient("invalid-url", "test_api_key");
        }, "使用无效URL时应抛出BtApiException");
    }

    @Test
    @DisplayName("测试创建客户端时API密钥验证")
    void testCreateClientWithEmptyApiKey() {
        // 测试使用空API密钥创建客户端
        assertThrows(BtApiException.class, () -> {
            BtClientFactory.createClient("http://localhost:8888", "");
        }, "使用空API密钥时应抛出BtApiException");
    }

    @Test
    @DisplayName("测试创建客户端时null参数验证")
    void testCreateClientWithNullParameters() {
        // 测试使用null URL创建客户端
        assertThrows(BtApiException.class, () -> {
            BtClientFactory.createClient(null, "test_api_key");
        }, "使用null URL时应抛出BtApiException");
        
        // 测试使用null API密钥创建客户端
        assertThrows(BtApiException.class, () -> {
            BtClientFactory.createClient("http://localhost:8888", null);
        }, "使用null API密钥时应抛出BtApiException");
        
        // 测试使用null配置创建客户端
        assertThrows(BtApiException.class, () -> {
            BtClientFactory.createClient(null);
        }, "使用null配置时应抛出BtApiException");
    }

    @Test
    @DisplayName("测试客户端配置的正确性")
    void testClientConfiguration() {
        // 创建自定义配置
        BtSdkConfig config = BtSdkConfig.builder()
                .baseUrl("http://example.com:8888")
                .apiKey("test_config_api_key")
                .connectTimeout(8)
                .readTimeout(15)
                .retryCount(3)
                .retryInterval(Duration.ofMillis(2000))
                .build();
        
        // 使用工厂方法创建客户端
        BtClient client = BtClientFactory.createClient(config);
        
        // 验证客户端创建成功
        assertNotNull(client);
        // 在实际项目中，您可能需要添加更多的配置验证
    }
}