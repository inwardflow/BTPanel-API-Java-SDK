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

@DisplayName("GetWebsiteDetailApi 单元测试")
class GetWebsiteDetailApiTest {

  @Test
  @DisplayName("应暴露正确的接口元数据并校验参数")
  void exposesMetadataAndValidatesParams() {
    GetWebsiteDetailApi api = new GetWebsiteDetailApi().setId(8);

    assertEquals("site?action=GetSiteStatus", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(8, api.getParams().get("id"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new GetWebsiteDetailApi().setId(0)));
  }

  @Test
  @DisplayName("应正确解析带 data 包装的网站详情")
  void parsesWrappedDetailPayload() {
    GetWebsiteDetailApi api = new GetWebsiteDetailApi();
    BtResult<Map<String, Object>> result =
        api.parseResponse(
            """
            {
              "status": true,
              "msg": "ok",
              "data": {
                "id": 1,
                "name": "demo.example.com",
                "flags": ["cdn"],
                "runtime": { "php": "82" }
              }
            }
            """);

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(1, result.getData().get("id"));
    assertEquals(List.of("cdn"), result.getData().get("flags"));
    assertEquals(Map.of("php", "82"), result.getData().get("runtime"));
  }

  @Test
  @DisplayName("应兼容直接返回详情对象的响应")
  void parsesDirectObjectPayload() {
    GetWebsiteDetailApi api = new GetWebsiteDetailApi();
    BtResult<Map<String, Object>> result =
        api.parseResponse("{\"id\":2,\"name\":\"demo.example.com\"}");

    assertTrue(result.isSuccess());
    assertEquals(2, result.getData().get("id"));
    assertEquals("demo.example.com", result.getData().get("name"));
  }

  @Test
  @DisplayName("应保留失败包装响应的状态与消息")
  void preservesFailurePayload() {
    GetWebsiteDetailApi api = new GetWebsiteDetailApi();
    BtResult<Map<String, Object>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"not found\"}");

    assertFalse(result.isSuccess());
    assertEquals("not found", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("成功包装响应缺少详情载荷时应抛出异常")
  void rejectsMissingPayload() {
    GetWebsiteDetailApi api = new GetWebsiteDetailApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("detail payload"));
  }

  @Test
  @DisplayName("data 字段不是对象时应抛出异常")
  void rejectsNonObjectDataPayload() {
    GetWebsiteDetailApi api = new GetWebsiteDetailApi();

    BtApiException exception =
        assertThrows(
            BtApiException.class, () -> api.parseResponse("{\"status\":true,\"data\":[]}"));

    assertTrue(exception.getMessage().contains("must be a JSON object"));
  }

  private boolean invokeValidate(GetWebsiteDetailApi api) {
    try {
      Method method = GetWebsiteDetailApi.class.getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
