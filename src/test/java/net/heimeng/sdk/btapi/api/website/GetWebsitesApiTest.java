package net.heimeng.sdk.btapi.api.website;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.WebsiteInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GetWebsitesApi类的单元测试
 * <p>
 * 测试GetWebsitesApi类的API调用、参数设置以及响应解析功能。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@DisplayName("GetWebsitesApi类测试")
class GetWebsitesApiTest {

    private GetWebsitesApi websitesApi;

    @BeforeEach
    void setUp() {
        // 创建测试对象，使用默认分页参数
        websitesApi = new GetWebsitesApi();
    }

    @Test
    @DisplayName("测试API基础信息")
    void testApiBasicInfo() {
        // 验证API端点和请求方法
        assertEquals("data?action=getData&table=sites", websitesApi.getEndpoint());
        assertEquals(BtApi.HttpMethod.POST, websitesApi.getMethod());
    }

    @Test
    @DisplayName("测试默认分页参数")
    void testDefaultPaginationParams() {
        // 获取默认参数
        Map<String, Object> params = websitesApi.getParams();
        assertNotNull(params);
        assertEquals(2, params.size());
        assertEquals(1, params.get("p"));  // 默认页码为1
        assertEquals(10, params.get("limit"));  // 默认每页10条记录
    }

    @Test
    @DisplayName("测试自定义分页参数构造")
    void testCustomPaginationParamsConstructor() {
        // 创建自定义分页参数的API实例
        GetWebsitesApi customApi = new GetWebsitesApi(2, 20);
        Map<String, Object> params = customApi.getParams();
        assertNotNull(params);
        assertEquals(2, params.size());
        assertEquals(2, params.get("p"));  // 自定义页码为2
        assertEquals(20, params.get("limit"));  // 自定义每页20条记录
    }

    @Test
    @DisplayName("测试参数设置方法")
    void testParamSetters() {
        // 测试设置页码
        GetWebsitesApi result = websitesApi.setPage(3);
        assertEquals(websitesApi, result);  // 验证链式调用
        assertEquals(3, websitesApi.getParams().get("p"));

        // 测试设置每页记录数
        result = websitesApi.setLimit(50);
        assertEquals(websitesApi, result);  // 验证链式调用
        assertEquals(50, websitesApi.getParams().get("limit"));
    }

    @Test
    @DisplayName("测试链式调用参数设置")
    void testChainedParamSetting() {
        // 测试链式调用
        GetWebsitesApi chainedApi = new GetWebsitesApi()
                .setPage(4)
                .setLimit(100);
        
        Map<String, Object> params = chainedApi.getParams();
        assertNotNull(params);
        assertEquals(4, params.get("p"));
        assertEquals(100, params.get("limit"));
    }

    @Test
    @DisplayName("测试响应解析 - 成功场景")
    void testParseResponse_Success() {
        // 创建模拟成功响应
        String mockResponse = "{" +
                "\"msg\": \"Success\"," +
                "\"data\": [" +
                "{\"id\": 1, \"name\": \"example.com\", \"path\": \"/www/wwwroot/example.com\", \"project_type\": \"php\", \"status\": \"1\", \"ssl\": 1, \"addtime\": \"2021-01-01 12:00:00\"}," +
                "{\"id\": 2, \"name\": \"test.com\", \"path\": \"/www/wwwroot/test.com\", \"project_type\": \"html\", \"status\": \"0\", \"ssl\": 0, \"addtime\": \"2021-01-02 13:30:00\"}" +
                "]}";
        
        // 解析响应
        BtResult<List<WebsiteInfo>> result = websitesApi.parseResponse(mockResponse);
        
        // 验证解析结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Success", result.getMsg());
        
        List<WebsiteInfo> websites = result.getData();
        assertNotNull(websites);
        assertEquals(2, websites.size());
        
        // 验证第一个网站信息
        WebsiteInfo website1 = websites.get(0);
        assertEquals(1, website1.getId());
        assertEquals("example.com", website1.getName());
        assertEquals("/www/wwwroot/example.com", website1.getPath());
        assertEquals("php", website1.getType());
        assertEquals("1", website1.getStatus());
        assertEquals(1, website1.getSsl());
        assertNotNull(website1.getCreateTime());
        assertTrue(website1.isRunning());
        assertTrue(website1.isSslEnabled());
        
        // 验证第二个网站信息
        WebsiteInfo website2 = websites.get(1);
        assertEquals(2, website2.getId());
        assertEquals("test.com", website2.getName());
        assertEquals("/www/wwwroot/test.com", website2.getPath());
        assertEquals("html", website2.getType());
        assertEquals("0", website2.getStatus());
        assertEquals(0, website2.getSsl());
        assertNotNull(website2.getCreateTime());
        assertFalse(website2.isRunning());
        assertFalse(website2.isSslEnabled());
    }

    @Test
    @DisplayName("测试响应解析 - 空响应")
    void testParseResponse_EmptyResponse() {
        // 创建模拟空响应
        String mockResponse = "{" +
                "\"msg\": \"Success\"," +
                "\"data\": []" +
                "}";
        
        // 解析响应
        BtResult<List<WebsiteInfo>> result = websitesApi.parseResponse(mockResponse);
        
        // 验证解析结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Success", result.getMsg());
        
        List<WebsiteInfo> websites = result.getData();
        assertNotNull(websites);
        assertTrue(websites.isEmpty());
    }

    @Test
    @DisplayName("测试响应解析 - 无效JSON格式")
    void testParseResponse_InvalidJson() {
        // 创建无效的JSON响应
        String invalidJson = "{invalid json}";
        
        // 验证解析异常
        BtApiException exception = assertThrows(BtApiException.class, () -> {
            websitesApi.parseResponse(invalidJson);
        });
        
        assertTrue(exception.getMessage().contains("JSON解析错误"));
    }

    @Test
    @DisplayName("测试响应解析 - 缺少data字段")
    void testParseResponse_MissingDataField() {
        // 创建缺少data字段的响应
        String mockResponse = "{" +
                "\"msg\": \"Success\"" +
                "}";
        
        // 验证解析异常
        BtApiException exception = assertThrows(BtApiException.class, () -> {
            websitesApi.parseResponse(mockResponse);
        });
        
        assertTrue(exception.getMessage().contains("data字段不存在"));
    }

    @Test
    @DisplayName("测试响应解析 - data字段非数组")
    void testParseResponse_DataNotArray() {
        // 创建data字段非数组的响应
        String mockResponse = "{" +
                "\"msg\": \"Success\"," +
                "\"data\": {\"id\": 1, \"name\": \"example.com\"}" +
                "}";
        
        // 验证解析异常
        BtApiException exception = assertThrows(BtApiException.class, () -> {
            websitesApi.parseResponse(mockResponse);
        });
        
        assertTrue(exception.getMessage().contains("data不是有效数组"));
    }

    @Test
    @DisplayName("测试响应解析 - 特殊值处理")
    void testParseResponse_SpecialValues() {
        // 创建包含特殊值的响应
        String responseWithSpecialValues = "{" +
                "\"msg\": \"Success\"," +
                "\"data\": [" +
                "{\"id\": null, \"name\": null, \"path\": null, \"project_type\": null, \"status\": null, \"ssl\": -1, \"addtime\": null}," +
                "{\"id\": 3, \"name\": \"special.com\", \"path\": \"\", \"project_type\": \"\", \"status\": \"0\", \"ssl\": 999, \"addtime\": \"2021-01-03 14:45:00\"}" +
                "]}";
        
        // 解析响应
        BtResult<List<WebsiteInfo>> result = websitesApi.parseResponse(responseWithSpecialValues);
        
        // 验证解析结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        List<WebsiteInfo> websites = result.getData();
        assertNotNull(websites);
        assertEquals(2, websites.size());
        
        // 验证第一个网站的特殊值处理
        WebsiteInfo website1 = websites.get(0);
        assertNull(website1.getId());
        assertNull(website1.getName());
        assertNull(website1.getPath());
        assertNull(website1.getType());
        assertNull(website1.getStatus());
        assertEquals(-1, website1.getSsl());
        assertNull(website1.getCreateTime());
        assertFalse(website1.isRunning());
        assertFalse(website1.isSslEnabled());
        
        // 验证第二个网站的特殊值处理
        WebsiteInfo website2 = websites.get(1);
        assertEquals(3, website2.getId());
        assertEquals("special.com", website2.getName());
        assertEquals("", website2.getPath());
        assertEquals("", website2.getType());
        assertEquals("0", website2.getStatus());
        assertEquals(999, website2.getSsl()); // 非标准SSL值也应正确解析
        assertNotNull(website2.getCreateTime());
        assertFalse(website2.isRunning());
        assertTrue(website2.isSslEnabled()); // 非零值应被视为SSL已启用
    }
}