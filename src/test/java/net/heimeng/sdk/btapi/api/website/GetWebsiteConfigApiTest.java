package net.heimeng.sdk.btapi.api.website;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("GetWebsiteConfigApi 单元测试")
class GetWebsiteConfigApiTest {

  @Test
  @DisplayName("应暴露正确的接口元数据并校验参数")
  void exposesMetadataAndValidatesParams() {
    GetWebsiteConfigApi api = new GetWebsiteConfigApi().setId(8).setPath("/www/wwwroot/demo");

    assertEquals("site?action=GetDirUserINI", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(8, api.getParams().get("id"));
    assertEquals("/www/wwwroot/demo", api.getParams().get("path"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new GetWebsiteConfigApi().setId(8)));
  }

  @Test
  @DisplayName("应正确解析原始配置对象响应")
  void parsesRawConfigPayload() {
    GetWebsiteConfigApi api = new GetWebsiteConfigApi();
    BtResult<Map<String, Object>> result =
        api.parseResponse(
            """
            {
              "pass": false,
              "logs": true,
              "userini": true,
              "runPath": {
                "dirs": ["/", "/public"],
                "runPath": "/"
              }
            }
            """);

    assertTrue(result.isSuccess());
    assertEquals(false, result.getData().get("pass"));
    assertEquals(true, result.getData().get("logs"));
    assertEquals(
        Map.of("dirs", List.of("/", "/public"), "runPath", "/"), result.getData().get("runPath"));
  }

  @Test
  @DisplayName("应正确解析包装后的配置对象响应")
  void parsesWrappedConfigPayload() {
    GetWebsiteConfigApi api = new GetWebsiteConfigApi();
    BtResult<Map<String, Object>> result =
        api.parseResponse("{\"status\":true,\"msg\":\"ok\",\"data\":{\"logs\":false}}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(false, result.getData().get("logs"));
  }

  @Test
  @DisplayName("应保留失败包装响应的状态与消息")
  void preservesFailurePayload() {
    GetWebsiteConfigApi api = new GetWebsiteConfigApi();
    BtResult<Map<String, Object>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"denied\"}");

    assertFalse(result.isSuccess());
    assertEquals("denied", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("data 字段不是对象时应抛出异常")
  void rejectsNonObjectDataPayload() {
    GetWebsiteConfigApi api = new GetWebsiteConfigApi();

    BtApiException exception =
        assertThrows(
            BtApiException.class,
            () -> api.parseResponse("{\"status\":true,\"data\":\"invalid\"}"));

    assertTrue(exception.getMessage().contains("must be a JSON object"));
  }

  private boolean invokeValidate(GetWebsiteConfigApi api) {
    try {
      Method method = GetWebsiteConfigApi.class.getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
