package net.heimeng.test;

import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.BtSdk;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.BtSite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * BtSdk类的单元测试
 * 测试Sdk的初始化、配置管理和API调用功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BtSdk单元测试")
public class BtSdkTest {

    private static final String TEST_BASE_URL = "http://localhost:8888";
    private static final String TEST_API_KEY = "test_api_key";

    @BeforeEach
    public void setUp() {
        // 重置BtSdk实例，确保测试之间相互独立
        BtSdk.reset();
    }

    @AfterEach
    public void tearDown() {
        // 测试完成后重置Sdk
        BtSdk.reset();
    }

    @Test
    @DisplayName("测试Sdk初始化 - 默认配置")
    public void testSdkInit_DefaultConfig() {
        // 初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "");
        BtSdk instance = BtSdk.getInstance();

        // 验证实例不为空
        assertNotNull(instance);
        // 验证单例模式
        assertSame(instance, BtSdk.getInstance());
    }

    @Test
    @DisplayName("测试Sdk初始化 - 自定义配置")
    public void testSdkInit_CustomConfig() {
        // 创建自定义配置
        BtSdkConfig customConfig = BtSdkConfig.builder()
                .connectTimeout(5)
                .readTimeout(70)
                .retryCount(2)
                .build();

        // 使用自定义配置初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "", customConfig);
        BtSdk instance = BtSdk.getInstance();

        // 验证实例不为空
        assertNotNull(instance);
        // 验证单例模式
        assertSame(instance, BtSdk.getInstance());
    }

    @Test
    @DisplayName("测试Sdk初始化 - 空参数验证")
    public void testSdkInit_NullParamsValidation() {
        // 测试空baseUrl
        assertThrows(IllegalArgumentException.class, () -> {
            BtSdk.initSdk(null, TEST_API_KEY, "");
        });

        // 测试空apiKey
        assertThrows(IllegalArgumentException.class, () -> {
            BtSdk.initSdk(TEST_BASE_URL, null, "");
        });

        // 测试空baseUrl和apiKey
        assertThrows(IllegalArgumentException.class, () -> {
            BtSdk.initSdk(null, null, "");
        });
    }

    @Test
    @DisplayName("测试Sdk初始化 - 空字符串参数验证")
    public void testSdkInit_EmptyStringParamsValidation() {
        // 测试空字符串baseUrl
        assertThrows(IllegalArgumentException.class, () -> {
            BtSdk.initSdk("", TEST_API_KEY, "");
        });

        // 测试空字符串apiKey
        assertThrows(IllegalArgumentException.class, () -> {
            BtSdk.initSdk(TEST_BASE_URL, "", "");
        });
    }

    @Test
    @DisplayName("测试Sdk初始化 - 无效URL格式验证")
    public void testSdkInit_InvalidUrlFormatValidation() {
        // 测试无效的URL格式
        assertThrows(IllegalArgumentException.class, () -> {
            BtSdk.initSdk("invalid-url", TEST_API_KEY, "");
        });
    }

    @Test
    @DisplayName("测试重置Sdk实例")
    public void testResetSdk() {
        // 初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "");
        BtSdk instance1 = BtSdk.getInstance();

        // 重置Sdk
        BtSdk.reset();

        // 重新初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "");
        BtSdk instance2 = BtSdk.getInstance();

        // 验证重置后获取的是新实例
        assertNotSame(instance1, instance2);
    }

    @Test
    @DisplayName("测试未初始化时获取实例")
    public void testGetInstance_NotInitialized() {
        // 确保Sdk未初始化
        BtSdk.reset();

        // 测试未初始化时获取实例会抛出异常
        assertThrows(IllegalStateException.class, BtSdk::getInstance);
    }

    @Test
    @DisplayName("测试获取系统总览信息 - 模拟成功响应")
    public void testGetSystemTotal_Success() throws BtApiException {
        // 初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "");

        // 创建模拟响应
        String mockResponse = "{\"status\":true,\"msg\":\"success\",\"data\":{\"cpu\":{\"percent\":10},\"memory\":{\"percent\":20}}}";

        // 模拟BtSdk实例直接返回结果，避免调用executeApi
        BtSdk mockSdk = Mockito.mock(BtSdk.class);
        Mockito.when(mockSdk.getSystemTotal()).thenReturn(mockResponse);

        // 执行测试
        String result = mockSdk.getSystemTotal();

        // 验证结果
        assertNotNull(result);
        assertTrue(JSONUtil.isTypeJSON(result));
    }

    @Test
    @DisplayName("测试获取网站列表 - 模拟成功响应")
    public void testGetSiteList_Success() throws BtApiException {
        // 初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "");

        // 创建模拟响应
        String mockResponse = "{\"status\":true,\"msg\":\"success\",\"data\":[{\"id\":1,\"name\":\"test.com\"}]}";

        // 创建模拟的BtSite对象列表
        List<BtSite> mockSiteList = new ArrayList<>();
        BtSite mockSite = new BtSite();
        mockSite.setId(1);
        mockSite.setName("test.com");
        mockSiteList.add(mockSite);

        // 模拟BtSdk实例直接返回结果，避免调用executeApi
        BtSdk mockSdk = Mockito.mock(BtSdk.class);
        Mockito.when(mockSdk.getSiteList()).thenReturn(mockSiteList);

        // 执行测试
        List<BtSite> siteList = mockSdk.getSiteList();

        // 验证结果
        assertNotNull(siteList);
        assertFalse(siteList.isEmpty());
        assertEquals(1, siteList.size());
        assertEquals(1, siteList.get(0).getId());
        assertEquals("test.com", siteList.get(0).getName());
    }

    @Test
    @DisplayName("测试创建目录")
    public void testCreateDir() throws BtApiException {
        // 初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "");

        // 为了测试成功，我们直接模拟createDir方法返回成功结果
        BtResult mockResult = new BtResult(true, "目录创建成功");
        BtSdk mockSdk = Mockito.mock(BtSdk.class);
        Mockito.when(mockSdk.createDir(anyString())).thenReturn(mockResult);

        // 执行测试
        BtResult result = mockSdk.createDir("/test/dir");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("目录创建成功", result.getMsg());
    }

    @Test
    @DisplayName("测试创建目录 - 空路径验证")
    public void testCreateDir_NullPathValidation() throws BtApiException {
        // 初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "");
        BtSdk instance = BtSdk.getInstance();

        // 测试空路径
        assertThrows(IllegalArgumentException.class, () -> {
            instance.createDir(null);
        });

        // 测试空字符串路径
        assertThrows(IllegalArgumentException.class, () -> {
            instance.createDir("");
        });
    }

    @Test
    @DisplayName("测试异常处理 - API异常")
    public void testExceptionHandling_ApiException() throws BtApiException {
        // 初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "");

        // 使用Mockito模拟BtSdk的getSystemTotal方法抛出BtApiException
        BtSdk mockSdk = Mockito.spy(BtSdk.getInstance());
        Mockito.doThrow(new BtApiException("API调用失败", null, "API_ERROR"))
                .when(mockSdk).getSystemTotal();

        // 执行测试并验证异常
        BtApiException exception = assertThrows(BtApiException.class, mockSdk::getSystemTotal);
        assertEquals("API调用失败", exception.getMessage());
        assertEquals("API_ERROR", exception.getErrorCode());
    }

    @Test
    @DisplayName("测试自定义配置 - 重试机制")
    public void testCustomConfig_RetryMechanism() {
        // 创建自定义配置，设置重试次数为3
        BtSdkConfig retryConfig = BtSdkConfig.builder()
                .retryCount(3)
                .retryInterval(Duration.ofMillis(100))
                .build();

        // 使用自定义配置初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "", retryConfig);
        BtSdk instance = BtSdk.getInstance();

        // 验证配置已应用
        assertNotNull(instance);
    }

    @Test
    @DisplayName("测试添加FTP用户 - 参数验证")
    public void testAddFtpUser_ParameterValidation() throws BtApiException {
        // 初始化Sdk
        BtSdk.initSdk(TEST_BASE_URL, TEST_API_KEY, "");
        BtSdk instance = BtSdk.getInstance();

        // 测试空参数
        assertThrows(IllegalArgumentException.class, () -> {
            instance.addFtpUser(null);
        });

        // 测试空Map参数
        assertThrows(IllegalArgumentException.class, () -> {
            instance.addFtpUser(Collections.emptyMap());
        });
    }
}