package net.heimeng.test;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.BtUtils;
import net.heimeng.sdk.btapi.api.BtApiFactory;
import net.heimeng.sdk.btapi.model.BtPanel;
import net.heimeng.sdk.btapi.model.BtPanelData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BT插件单元测试案例")
public class BTUnitTest {
    @Mock
    private BtPanel btPanel;

    // 添加此注解以确保 btPanel 被正确注入
    @InjectMocks
    private BTUnitTest testInstance;

    @BeforeEach
    public void init() {
        // 使用lenient允许未使用的模拟
        lenient().when(btPanel.getApiKey()).thenReturn("test_api_key");
    }

    @DisplayName("测试获取服务器总览接口")
    @Test
    public void testGetSystemTotal() {
        // 准备模拟数据
        String mockResponse = "{\"status\":true,\"msg\":\"success\",\"data\":{\"cpu\":{\"percent\":10},\"memory\":{\"percent\":20}}}";
        when(btPanel.execute(BtApiFactory.GET_SYSTEM_TOTAL)).thenReturn(mockResponse);

        // 执行测试
        String result = btPanel.execute(BtApiFactory.GET_SYSTEM_TOTAL);
        System.out.println("模拟响应结果: " + UnicodeUtil.toString(result));

        // 验证响应格式正确
        assert JSONUtil.isTypeJSON(result);
    }

    @DisplayName("测试获取Api登录令牌")
    @Test
    public void testGetRequestSign() {
        long requestTime = BtUtils.generateRequestTime();
        String requestToken = BtUtils.generateRequestToken(btPanel.getApiKey(), requestTime);

        System.out.println("Request Time: " + requestTime);
        System.out.println("Request Token: " + requestToken);

        // 验证生成的令牌不为空
        assert requestToken != null && !requestToken.isEmpty();
    }

    @DisplayName("测试获取站点列表接口")
    @Test
    public void testGetSiteList() {
        // 准备模拟数据
        String mockResponse = "{\"status\":true,\"msg\":\"success\",\"data\":[{\"id\":1,\"name\":\"test.com\"}]}";
        when(btPanel.execute(BtApiFactory.GET_SITE_LIST)).thenReturn(mockResponse);

        // 执行测试
        String result = btPanel.execute(BtApiFactory.GET_SITE_LIST);
        System.out.println("模拟响应结果: " + UnicodeUtil.toString(result));

        // 验证响应格式正确
        assert JSONUtil.isTypeJSON(result);
    }

    @DisplayName("测试 SiteMigrateCheckSurroundingsBtApi 接口")
    @Test
    public void testSiteMigrateCheckSurroundingsBtApi() {
        // 准备模拟数据
        String mockResponse = "{\"status\":true,\"msg\":\"success\",\"data\":{\"compatible\":true}}";
        Map<String, Object> params = Map.of("panel", "http://localhost:8888", "api_token", "test_token");
        when(btPanel.execute(BtApiFactory.SITE_MIGRATE_CHECK_SURROUNDINGS, params)).thenReturn(mockResponse);

        // 执行测试
        String result = btPanel.execute(BtApiFactory.SITE_MIGRATE_CHECK_SURROUNDINGS, params);
        System.out.println("模拟响应结果: " + UnicodeUtil.toString(result));

        // 验证响应格式正确
        assert JSONUtil.isTypeJSON(result);
    }

    @DisplayName("测试 SiteMigrateSetPanelApiBtApi 接口")
    @Test
    public void testSiteMigrateSetPanelApiBtApi() {
        // 准备模拟数据
        String mockResponse = "{\"status\":true,\"msg\":\"success\"}";
        Map<String, Object> params = Map.of("panel", "http://localhost:8888", "token", "test_token");
        when(btPanel.execute(BtApiFactory.SITE_MIGRATE_SET_PANEL_API, params)).thenReturn(mockResponse);

        // 执行测试
        String result = btPanel.execute(BtApiFactory.SITE_MIGRATE_SET_PANEL_API, params);
        System.out.println("模拟响应结果: " + UnicodeUtil.toString(result));

        // 验证响应格式正确
        assert JSONUtil.isTypeJSON(result);
    }

    @DisplayName("测试 buildSyncInfo 方法")
    @Test
    public void testBuildSyncInfo() {
        // 准备测试数据
        Map<String, Object> syncInfoMap = new HashMap<>();
        syncInfoMap.put("sites", List.of(Map.of("id", 9, "name", "baidu.com")));
        syncInfoMap.put("zip", true);

        System.out.println("同步信息Map: " + syncInfoMap);
        String jsonStr = JSONUtil.toJsonPrettyStr(syncInfoMap);
        System.out.println("JSON格式: " + jsonStr);

        // 验证JSON转换正确
        assert JSONUtil.isTypeJSON(jsonStr);

        // 模拟API响应
        String mockResponse = "{\"status\":true,\"msg\":\"success\",\"data\":{\"site_info\":{\"id\":9}}}";
        when(btPanel.execute(BtApiFactory.SITE_MIGRATE_GET_SITE_INFO)).thenReturn(mockResponse);

        // 执行测试
        String result = btPanel.execute(BtApiFactory.SITE_MIGRATE_GET_SITE_INFO);
        System.out.println("模拟响应结果: " + result);

        // 验证响应可以转换为BtPanelData
        BtPanelData data = JSONUtil.toBean(result, BtPanelData.class);
        System.out.println("转换后的数据对象: " + data);
        assert data != null;
    }
}
