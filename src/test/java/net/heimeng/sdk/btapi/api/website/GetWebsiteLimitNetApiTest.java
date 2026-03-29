package net.heimeng.sdk.btapi.api.website;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("GetWebsiteLimitNetApi 单元测试")
class GetWebsiteLimitNetApiTest {

  @Test
  @DisplayName("应暴露正确的接口元数据并校验参数")
  void exposesMetadataAndValidatesParams() {
    GetWebsiteLimitNetApi api = new GetWebsiteLimitNetApi().setId(8);

    assertEquals("site?action=GetLimitNet", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(8, api.getParams().get("id"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new GetWebsiteLimitNetApi().setId(0)));
  }

  @Test
  @DisplayName("应正确解析原始流量限制配置响应")
  void parsesRawLimitConfigPayload() {
    GetWebsiteLimitNetApi api = new GetWebsiteLimitNetApi();
    BtResult<Map<String, Object>> result =
        api.parseResponse("{\"perserver\":300,\"perip\":25,\"limit_rate\":512,\"enabled\":true}");

    assertTrue(result.isSuccess());
    assertEquals(300, result.getData().get("perserver"));
    assertEquals(25, result.getData().get("perip"));
    assertEquals(512, result.getData().get("limit_rate"));
    assertEquals(true, result.getData().get("enabled"));
  }

  @Test
  @DisplayName("应正确解析包装后的流量限制配置响应")
  void parsesWrappedLimitConfigPayload() {
    GetWebsiteLimitNetApi api = new GetWebsiteLimitNetApi();
    BtResult<Map<String, Object>> result =
        api.parseResponse("{\"status\":true,\"msg\":\"ok\",\"data\":{\"enabled\":false}}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(false, result.getData().get("enabled"));
  }

  @Test
  @DisplayName("应保留失败包装响应的状态与消息")
  void preservesFailurePayload() {
    GetWebsiteLimitNetApi api = new GetWebsiteLimitNetApi();
    BtResult<Map<String, Object>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"nginx only\"}");

    assertFalse(result.isSuccess());
    assertEquals("nginx only", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("data 字段不是对象时应抛出异常")
  void rejectsNonObjectDataPayload() {
    GetWebsiteLimitNetApi api = new GetWebsiteLimitNetApi();

    BtApiException exception =
        assertThrows(
            BtApiException.class, () -> api.parseResponse("{\"status\":true,\"data\":123}"));

    assertTrue(exception.getMessage().contains("must be a JSON object"));
  }

  private boolean invokeValidate(GetWebsiteLimitNetApi api) {
    try {
      Method method = GetWebsiteLimitNetApi.class.getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
