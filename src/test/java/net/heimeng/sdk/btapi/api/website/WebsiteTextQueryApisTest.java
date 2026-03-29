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

@DisplayName("Website 文本查询 API 单元测试")
class WebsiteTextQueryApisTest {

  @Test
  @DisplayName("网站根目录 API 契约应正确")
  void rootPathApiContract() {
    GetWebsiteRootPathApi api = new GetWebsiteRootPathApi().setId(8);

    assertEquals("data?action=getKey&table=sites&key=path", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(8, api.getParams().get("id"));
    assertTrue(invokeValidate(api));

    BtResult<String> result = api.parseResponse("/www/wwwroot/demo");
    assertTrue(result.isSuccess());
    assertEquals("/www/wwwroot/demo", result.getData());
  }

  @Test
  @DisplayName("网站 PHP 版本 API 应正确解析包装成功响应")
  void phpVersionApiParsesWrappedSuccess() {
    GetWebsitePhpVersionApi api = new GetWebsitePhpVersionApi().setId(8);

    BtResult<String> result = api.parseResponse("{\"status\":true,\"msg\":\"ok\",\"data\":\"82\"}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals("82", result.getData());
  }

  @Test
  @DisplayName("网站伪静态规则 API 应保留失败响应")
  void rewriteRulesApiPreservesFailureResponse() {
    GetWebsiteRewriteRulesApi api = new GetWebsiteRewriteRulesApi().setId(8);

    BtResult<String> result = api.parseResponse("{\"status\":false,\"msg\":\"rewrite disabled\"}");

    assertFalse(result.isSuccess());
    assertEquals("rewrite disabled", result.getMsg());
    assertEquals("", result.getData());
  }

  @Test
  @DisplayName("网站 Nginx 配置 API 应校验域名参数")
  void nginxConfigApiValidatesDomain() {
    GetWebsiteNginxConfigApi api =
        new GetWebsiteNginxConfigApi().setId(8).setDomain("demo.example.com");

    assertEquals("site?action=getConf", api.getEndpoint());
    assertTrue(invokeValidate(api));

    api.setDomain(" ");
    assertFalse(invokeValidate(api));
  }

  @Test
  @DisplayName("非包装 JSON 文本应视为原始内容")
  void nonWrappedJsonContentIsTreatedAsRawText() {
    GetWebsiteNginxConfigApi api =
        new GetWebsiteNginxConfigApi().setId(8).setDomain("demo.example.com");

    BtResult<String> result = api.parseResponse("{\"server\":\"demo\"}");

    assertTrue(result.isSuccess());
    assertEquals("{\"server\":\"demo\"}", result.getData());
  }

  @Test
  @DisplayName("包装成功响应缺少 data 字段时应抛出异常")
  void wrappedSuccessWithoutDataIsRejected() {
    GetWebsitePhpVersionApi api = new GetWebsitePhpVersionApi().setId(8);

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("data field"));
  }

  @Test
  @DisplayName("空响应应抛出异常")
  void blankResponseIsRejected() {
    GetWebsiteRootPathApi api = new GetWebsiteRootPathApi().setId(8);

    BtApiException exception = assertThrows(BtApiException.class, () -> api.parseResponse(" "));

    assertTrue(exception.getMessage().contains("Empty response"));
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
