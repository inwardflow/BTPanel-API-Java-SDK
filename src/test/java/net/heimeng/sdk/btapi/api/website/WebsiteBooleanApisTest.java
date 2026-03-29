package net.heimeng.sdk.btapi.api.website;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("Website 布尔写操作 API 单元测试")
class WebsiteBooleanApisTest {

  @Test
  @DisplayName("添加站点域名 API 契约正确")
  void addWebsiteDomainApiContract() {
    AddWebsiteDomainApi api =
        new AddWebsiteDomainApi()
            .setId(8)
            .setWebname("demo.example.com")
            .setDomain("www.demo.example.com");

    assertEquals("site?action=AddDomain", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(8, api.getParams().get("id"));
    assertTrue(invokeValidate(api));

    BtResult<Boolean> result = api.parseResponse("{\"status\":true,\"msg\":\"ok\"}");
    assertTrue(result.isSuccess());
    assertTrue(result.getData());
  }

  @Test
  @DisplayName("删除站点域名 API 契约正确")
  void deleteWebsiteDomainApiContract() {
    DeleteWebsiteDomainApi api =
        new DeleteWebsiteDomainApi()
            .setId(8)
            .setWebname("demo.example.com")
            .setDomain("www.demo.example.com")
            .setPort(80);

    assertEquals("site?action=DelDomain", api.getEndpoint());
    assertEquals(80, api.getParams().get("port"));
    assertTrue(invokeValidate(api));

    BtResult<Boolean> result = api.parseResponse("{\"status\":false,\"msg\":\"denied\"}");
    assertFalse(result.isSuccess());
    assertFalse(result.getData());
    assertEquals("denied", result.getMsg());
  }

  @Test
  @DisplayName("关闭密码访问 API 契约正确")
  void closeWebsitePasswordApiContract() {
    CloseWebsitePasswordApi api = new CloseWebsitePasswordApi().setId(8);

    assertEquals("site?action=CloseHasPwd", api.getEndpoint());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("关闭 SSL API 契约正确")
  void closeWebsiteSslApiContract() {
    CloseWebsiteSslApi api = new CloseWebsiteSslApi().setId(8);

    assertEquals("site?action=CloseSSL", api.getEndpoint());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("创建站点备份 API 契约正确")
  void createWebsiteBackupApiContract() {
    CreateWebsiteBackupApi api = new CreateWebsiteBackupApi().setId(8);

    assertEquals("site?action=ToBackup", api.getEndpoint());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("删除站点备份 API 契约正确")
  void deleteWebsiteBackupApiContract() {
    DeleteWebsiteBackupApi api = new DeleteWebsiteBackupApi().setId(18);

    assertEquals("site?action=DelBackup", api.getEndpoint());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("启动站点 API 契约正确")
  void startWebsiteApiContract() {
    StartWebsiteApi api = new StartWebsiteApi().setId(8);

    assertEquals("site?action=SiteStart", api.getEndpoint());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("停止站点 API 契约正确")
  void stopWebsiteApiContract() {
    StopWebsiteApi api = new StopWebsiteApi().setId(8);

    assertEquals("site?action=StopSite", api.getEndpoint());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("切换日志状态 API 契约正确")
  void setWebsiteLogsApiContract() {
    SetWebsiteLogsApi api = new SetWebsiteLogsApi().setId(8);

    assertEquals("site?action=logsOpen", api.getEndpoint());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("设置站点备注 API 允许空字符串备注")
  void setWebsitePsApiAllowsBlankValue() {
    SetWebsitePsApi api = new SetWebsitePsApi().setId(8).setPs("");

    assertEquals("", api.getParams().get("ps"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("设置站点根目录 API 契约正确")
  void setWebsiteRootPathApiContract() {
    SetWebsiteRootPathApi api = new SetWebsiteRootPathApi().setId(8).setPath("/www/wwwroot/demo");

    assertEquals("site?action=SetPath", api.getEndpoint());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("设置站点运行目录 API 允许空字符串")
  void setWebsiteRunPathApiAllowsBlankValue() {
    SetWebsiteRunPathApi api = new SetWebsiteRunPathApi().setId(8).setRunPath("");

    assertEquals("", api.getParams().get("runPath"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("切换防跨站配置 API 契约正确")
  void setWebsiteUserIniApiContract() {
    SetWebsiteUserIniApi api = new SetWebsiteUserIniApi().setPath("/www/wwwroot/demo");

    assertEquals("site?action=SetDirUserINI", api.getEndpoint());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("设置 PHP 版本 API 契约正确")
  void setWebsitePhpVersionApiContract() {
    SetWebsitePhpVersionApi api = new SetWebsitePhpVersionApi().setId(8).setPhpVersion("82");

    assertEquals("82", api.getParams().get("php_version"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("设置站点密码访问 API 契约正确")
  void setWebsitePasswordApiContract() {
    SetWebsitePasswordApi api =
        new SetWebsitePasswordApi().setId(8).setUsername("admin").setPassword("secret");

    assertEquals("admin", api.getParams().get("username"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("设置伪静态规则 API 允许空规则内容")
  void setWebsiteRewriteRulesApiAllowsEmptyContent() {
    SetWebsiteRewriteRulesApi api =
        new SetWebsiteRewriteRulesApi().setId(8).setName("none").setContent("");

    assertEquals("", api.getParams().get("content"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("设置 Nginx 配置 API 允许空配置内容")
  void setWebsiteNginxConfigApiAllowsEmptyContent() {
    SetWebsiteNginxConfigApi api =
        new SetWebsiteNginxConfigApi().setId(8).setDomain("demo.example.com").setContent("");

    assertEquals("", api.getParams().get("content"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("设置流量限制 API 应将布尔值转换为整数标志位")
  void setWebsiteLimitNetApiContract() {
    SetWebsiteLimitNetApi api =
        new SetWebsiteLimitNetApi()
            .setId(8)
            .setEnabled(false)
            .setPerserver(300)
            .setPerip(30)
            .setLimitRate(0);

    assertEquals(0, api.getParams().get("enabled"));
    assertEquals(300, api.getParams().get("perserver"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("设置流量限制 API 应拒绝负数限制值")
  void setWebsiteLimitNetApiRejectsNegativeNumber() {
    SetWebsiteLimitNetApi api = new SetWebsiteLimitNetApi();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> api.setPerip(-1));

    assertTrue(exception.getMessage().contains("perip"));
  }

  @Test
  @DisplayName("设置 PHP 扩展 API 应将布尔值转换为整数标志位")
  void setWebsitePhpExtensionsApiContract() {
    SetWebsitePhpExtensionsApi api =
        new SetWebsitePhpExtensionsApi().setId(8).setModuleName("redis").setEnabled(true);

    assertEquals(1, api.getParams().get("enabled"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("设置 SSL API 支持可选强制 HTTPS 标志位")
  void setWebsiteSslApiContract() {
    SetWebsiteSslApi api =
        new SetWebsiteSslApi()
            .setId(8)
            .setDomain("demo.example.com")
            .setCert("cert")
            .setKey("key")
            .setForceHttps(true);

    assertEquals(1, api.getParams().get("force_https"));
    assertTrue(invokeValidate(api));

    api.setForceHttps(null);
    assertFalse(api.getParams().containsKey("force_https"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("删除站点 API 应正确维护关联删除标志")
  void deleteWebsiteApiContract() {
    DeleteWebsiteApi api =
        new DeleteWebsiteApi(8, "demo.example.com")
            .setDeleteFtp(true)
            .setDeleteDatabase(true)
            .setDeletePath(true);

    assertEquals("site?action=DeleteSite", api.getEndpoint());
    assertEquals(1, api.getParams().get("ftp"));
    assertEquals(1, api.getParams().get("database"));
    assertEquals(1, api.getParams().get("path"));
    assertTrue(invokeValidate(api));

    api.setDeleteFtp(false).setDeleteDatabase(false).setDeletePath(false);
    assertFalse(api.getParams().containsKey("ftp"));
    assertFalse(api.getParams().containsKey("database"));
    assertFalse(api.getParams().containsKey("path"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("删除站点 API 应拒绝非正整数 ID")
  void deleteWebsiteApiRejectsNonPositiveId() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new DeleteWebsiteApi(0, "demo"));

    assertTrue(exception.getMessage().contains("id"));
  }

  @Test
  @DisplayName("Website 布尔 API 应拒绝缺少状态字段的响应")
  void booleanApisRejectMissingStatusField() {
    AddWebsiteDomainApi api = new AddWebsiteDomainApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"msg\":\"ok\"}"));

    assertTrue(exception.getMessage().contains("status field"));
  }

  @Test
  @DisplayName("Website 布尔 API 应拒绝非 JSON 响应")
  void booleanApisRejectInvalidJson() {
    DeleteWebsiteApi api = new DeleteWebsiteApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("plain text"));

    assertTrue(exception.getMessage().contains("Invalid JSON"));
  }

  private boolean invokeValidate(Object api) {
    try {
      Method method = api.getClass().getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
