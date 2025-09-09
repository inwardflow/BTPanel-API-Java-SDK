package net.heimeng.sdk.btapi.model.website;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebsiteInfo模型类的单元测试
 * <p>
 * 测试WebsiteInfo类的所有属性设置、获取以及辅助方法的正确性。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@DisplayName("WebsiteInfo模型类测试")
class WebsiteInfoTest {

    @Test
    @DisplayName("测试基本属性设置与获取")
    void testBasicProperties() {
        // 创建测试对象
        WebsiteInfo website = new WebsiteInfo();
        website.setId(1L);
        website.setName("example.com");
        website.setDomain("example.com");
        website.setPath("/www/wwwroot/example.com");
        website.setType("php");
        website.setStatus(1);
        website.setSsl(1);
        website.setCreateTime(1609459200L); // 2021-01-01 00:00:00

        // 验证属性设置
        assertEquals(1L, website.getId());
        assertEquals("example.com", website.getName());
        assertEquals("example.com", website.getDomain());
        assertEquals("/www/wwwroot/example.com", website.getPath());
        assertEquals("php", website.getType());
        assertEquals(1, website.getStatus());
        assertEquals(1, website.getSsl());
        assertEquals(1609459200L, website.getCreateTime());
    }

    @Test
    @DisplayName("测试创建时间转换")
    void testCreateDate() {
        // 测试正常时间戳
        WebsiteInfo website1 = new WebsiteInfo();
        long timestamp = 1609459200L; // 2021-01-01 00:00:00
        website1.setCreateTime(timestamp);
        Date date = website1.getCreateDate();
        assertNotNull(date);
        assertEquals(timestamp * 1000, date.getTime());

        // 测试无效时间戳
        WebsiteInfo website2 = new WebsiteInfo();
        website2.setCreateTime(0L);
        assertNull(website2.getCreateDate());

        // 测试null时间戳
        WebsiteInfo website3 = new WebsiteInfo();
        website3.setCreateTime(null);
        assertNull(website3.getCreateDate());
    }

    @Test
    @DisplayName("测试网站状态检查方法")
    void testIsRunning() {
        // 测试运行中状态
        WebsiteInfo runningWebsite = new WebsiteInfo();
        runningWebsite.setStatus(1);
        assertTrue(runningWebsite.isRunning());

        // 测试停止状态
        WebsiteInfo stoppedWebsite = new WebsiteInfo();
        stoppedWebsite.setStatus(0);
        assertFalse(stoppedWebsite.isRunning());

        // 测试null状态
        WebsiteInfo nullStatusWebsite = new WebsiteInfo();
        nullStatusWebsite.setStatus(null);
        assertFalse(nullStatusWebsite.isRunning());
    }

    @Test
    @DisplayName("测试SSL状态检查方法")
    void testIsSslEnabled() {
        // 测试SSL开启状态
        WebsiteInfo sslEnabledWebsite = new WebsiteInfo();
        sslEnabledWebsite.setSsl(1);
        assertTrue(sslEnabledWebsite.isSslEnabled());

        // 测试SSL未开启状态
        WebsiteInfo sslDisabledWebsite = new WebsiteInfo();
        sslDisabledWebsite.setSsl(0);
        assertFalse(sslDisabledWebsite.isSslEnabled());

        // 测试-1状态（宝塔API的特殊值）
        WebsiteInfo sslMinusOneWebsite = new WebsiteInfo();
        sslMinusOneWebsite.setSsl(-1);
        assertFalse(sslMinusOneWebsite.isSslEnabled());

        // 测试null状态
        WebsiteInfo nullSslWebsite = new WebsiteInfo();
        nullSslWebsite.setSsl(null);
        assertFalse(nullSslWebsite.isSslEnabled());
    }

    @Test
    @DisplayName("测试空值处理")
    void testNullValueHandling() {
        // 创建空对象
        WebsiteInfo emptyWebsite = new WebsiteInfo();

        // 验证空值处理
        assertNull(emptyWebsite.getId());
        assertNull(emptyWebsite.getName());
        assertNull(emptyWebsite.getDomain());
        assertNull(emptyWebsite.getPath());
        assertNull(emptyWebsite.getType());
        assertNull(emptyWebsite.getStatus());
        assertNull(emptyWebsite.getSsl());
        assertNull(emptyWebsite.getCreateTime());

        // 验证空值下的辅助方法
        assertNull(emptyWebsite.getCreateDate());
        assertFalse(emptyWebsite.isRunning());
        assertFalse(emptyWebsite.isSslEnabled());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        // 创建测试对象
        WebsiteInfo website = new WebsiteInfo();
        website.setId(1L);
        website.setName("example.com");

        // 验证toString方法不抛出异常
        String toStringResult = website.toString();
        assertNotNull(toStringResult);
        assertFalse(toStringResult.isEmpty());
        assertTrue(toStringResult.contains("id=1"));
        assertTrue(toStringResult.contains("name=example.com"));
    }

    @Test
    @DisplayName("测试不同网站类型场景")
    void testDifferentWebsiteTypes() {
        // 测试PHP网站
        WebsiteInfo phpWebsite = new WebsiteInfo();
        phpWebsite.setType("php");
        assertEquals("php", phpWebsite.getType());

        // 测试静态网站
        WebsiteInfo staticWebsite = new WebsiteInfo();
        staticWebsite.setType("html");
        assertEquals("html", staticWebsite.getType());

        // 测试其他类型
        WebsiteInfo otherWebsite = new WebsiteInfo();
        otherWebsite.setType("nodejs");
        assertEquals("nodejs", otherWebsite.getType());
    }
}