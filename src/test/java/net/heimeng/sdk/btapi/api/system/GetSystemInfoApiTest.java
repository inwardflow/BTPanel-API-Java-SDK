package net.heimeng.sdk.btapi.api.system;

import static org.junit.jupiter.api.Assertions.*;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.SystemInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * GetSystemInfoApi的单元测试类
 * <p>
 * 测试获取系统信息API的各种场景，包括正常响应、边缘情况和异常处理。
 * 这些测试不依赖于实际的API调用，而是通过模拟HTTP响应来测试API的解析逻辑。
 * </p>
 */
@DisplayName("获取系统信息API单元测试")
class GetSystemInfoApiTest {

    private GetSystemInfoApi systemInfoApi;

    @BeforeEach
    void setUp() {
        systemInfoApi = new GetSystemInfoApi();
    }

    @Nested
    @DisplayName("构造函数和基本属性测试")
    class ConstructorAndBasicPropertiesTest {
        
        @Test
        @DisplayName("应该正确设置端点和请求方法")
        void shouldSetCorrectEndpointAndMethod() {
            // 验证端点和请求方法设置正确
            assertEquals("system?action=GetSystemTotal", systemInfoApi.getEndpoint(), "端点路径应该正确设置");
            assertEquals(BtApi.HttpMethod.POST, systemInfoApi.getMethod(), "请求方法应该设置为POST");
        }
    }

    @Nested
    @DisplayName("响应解析功能测试")
    class ResponseParsingTest {
        
        private static final String VALID_RESPONSE = """
        {
            "system": "Ubuntu 20.04.4 LTS x86_64",
            "kernel": "5.4.0-109-generic",
            "cpuRealUsed": "2.5",
            "memTotal": "8000",
            "memRealUsed": "4000",
            "version": "7.9.6"
        }
        """;
        
        @Test
        @DisplayName("应该正确解析包含完整信息的成功响应")
        void shouldParseCompleteSuccessResponseCorrectly() {
            // 执行解析
            BtResult<SystemInfo> result = systemInfoApi.parseResponse(VALID_RESPONSE);
            
            // 验证解析结果
            assertNotNull(result, "解析结果不应为null");
            assertTrue(result.isSuccess(), "结果状态应为成功");
            assertEquals("Success", result.getMsg(), "成功消息应正确设置");
            assertNotNull(result.getData(), "系统信息数据不应为null");
            
            SystemInfo systemInfo = result.getData();
            assertEquals("Ubuntu 20.04.4 LTS x86_64", systemInfo.getOs(), "操作系统信息应正确解析");
            assertEquals("Ubuntu 20.04.4 LTS x86_64", systemInfo.getKernel(), "内核信息应正确解析");
            assertEquals(2.5, systemInfo.getCpuUsage(), 0.001, "CPU使用率应正确解析为数字");
            assertEquals(8000L, systemInfo.getMemoryTotal(), "内存总量应正确解析为数字");
            assertEquals(4000L, systemInfo.getMemoryUsed(), "内存已用量应正确解析为数字");
            assertEquals("7.9.6", systemInfo.getPanelVersion(), "面板版本应正确解析");
            
            // 验证计算字段
            assertEquals(50.0, systemInfo.getMemoryUsagePercentage(), 0.001, "内存使用率应正确计算");
            assertEquals(0.0, systemInfo.getDiskUsage(), 0.001, "磁盘使用率应默认为0");
        }
        
        @Test
        @DisplayName("应该正确处理带有version字段的响应")
        void shouldHandleResponseWithVersionField() {
            // 准备测试数据，使用version字段（根据实际API返回）
            String mockResponse = """
            {
                "system": "CentOS Linux release 7.9.2009 (Core)",
                "cpuRealUsed": "5.2",
                "memTotal": "16000",
                "memRealUsed": "8000",
                "version": "7.9.6"
            }
            """;
            
            // 执行解析
            BtResult<SystemInfo> result = systemInfoApi.parseResponse(mockResponse);
            
            // 验证解析结果
            assertNotNull(result, "结果对象不应为null");
            assertNotNull(result.getData(), "系统信息数据不应为null");
            assertEquals("7.9.6", result.getData().getPanelVersion(), "版本信息应从version字段获取");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("应该对空响应抛出异常")
        void shouldThrowExceptionForEmptyResponse(String emptyResponse) {
            BtApiException exception = assertThrows(
                BtApiException.class, 
                () -> systemInfoApi.parseResponse(emptyResponse),
                "对空响应应抛出异常"
            );
            assertTrue(exception.getMessage().contains("Empty response"), "异常消息应包含Empty response");
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
            "{invalid json}", 
            "<html></html>", 
            "plain text"
        })
        @DisplayName("应该对无效JSON响应抛出异常")
        void shouldThrowExceptionForInvalidJson(String invalidJson) {
            BtApiException exception = assertThrows(
                BtApiException.class, 
                () -> systemInfoApi.parseResponse(invalidJson),
                "对无效JSON响应应抛出异常"
            );
            assertTrue(exception.getMessage().contains("Invalid JSON"), "异常消息应包含Invalid JSON");
        }
    }

    @Nested
    @DisplayName("边界情况和异常处理测试")
    class EdgeCaseAndExceptionHandlingTest {
        
        @Test
        @DisplayName("应该处理空的system字段")
        void shouldHandleEmptySystemField() {
            // 准备测试数据，system字段为空
            String mockResponse = """
            {
                "system": "",
                "cpuRealUsed": "2.5",
                "memTotal": "8000",
                "memRealUsed": "4000",
                "version": "7.9.6"
            }
            """;
            
            // 执行解析
            BtResult<SystemInfo> result = systemInfoApi.parseResponse(mockResponse);
            
            // 验证hostname默认为"Unknown"
            assertNotNull(result);
            assertNotNull(result.getData());
            assertEquals("", result.getData().getOs(), "操作系统信息应保留为空字符串");
            assertEquals("", result.getData().getKernel(), "内核信息应保留为空字符串");
        }
        
        @Test
        @DisplayName("应该处理缺少的system字段")
        void shouldHandleMissingSystemField() {
            // 准备测试数据，缺少system字段
            String mockResponse = """
            {
                "cpuRealUsed": "2.5",
                "memTotal": "8000",
                "memRealUsed": "4000",
                "version": "7.9.6"
            }
            """;
            
            // 执行解析
            BtResult<SystemInfo> result = systemInfoApi.parseResponse(mockResponse);
            
            // 验证结果
            assertNotNull(result);
            assertNotNull(result.getData());
        }
        
        @Test
        @DisplayName("应该处理内存字段为零的情况")
        void shouldHandleZeroMemoryValues() {
            // 准备测试数据，内存相关字段为0
            String mockResponse = """
            {
                "system": "Ubuntu",
                "cpuRealUsed": "2.5",
                "memTotal": "0",
                "memRealUsed": "0",
                "version": "7.9.6"
            }
            """;
            
            // 执行解析
            BtResult<SystemInfo> result = systemInfoApi.parseResponse(mockResponse);
            
            // 验证结果
            assertNotNull(result, "结果对象不应为null");
            assertNotNull(result.getData(), "系统信息数据不应为null");
            assertEquals(0L, result.getData().getMemoryTotal(), "内存总量应为0");
            assertEquals(0L, result.getData().getMemoryUsed(), "内存已用量应为0");
            assertEquals(0.0, result.getData().getMemoryUsagePercentage(), 0.001, "内存使用率应为0");
        }
        
        @Test
        @DisplayName("应该处理所有可选字段缺失的情况")
        void shouldHandleAllOptionalFieldsMissing() {
            // 准备测试数据，只有最基本的字段
            String minimalResponse = "{}";
            
            // 执行解析
            BtResult<SystemInfo> result = systemInfoApi.parseResponse(minimalResponse);
            
            // 验证结果
            assertNotNull(result, "结果对象不应为null");
            assertNotNull(result.getData(), "系统信息数据不应为null");
            assertEquals("Unknown", result.getData().getHostname(), "主机名应默认为Unknown");
            assertEquals("Unknown", result.getData().getOs(), "操作系统信息应默认为Unknown");
            assertEquals("Unknown", result.getData().getKernel(), "内核信息应默认为Unknown");
            assertEquals(0.0, result.getData().getCpuUsage(), "CPU使用率应默认为0");
            assertEquals(0L, result.getData().getMemoryTotal(), "内存总量应默认为0");
            assertEquals(0L, result.getData().getMemoryUsed(), "内存已用量应默认为0");
            assertEquals("Unknown", result.getData().getPanelVersion(), "面板版本应默认为Unknown");
        }
    }
}