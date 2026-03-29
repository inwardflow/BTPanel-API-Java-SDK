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

@DisplayName("GetWebsiteDomainsApi 单元测试")
class GetWebsiteDomainsApiTest {

  @Test
  @DisplayName("应暴露正确的接口元数据并校验参数")
  void exposesMetadataAndValidatesParams() {
    GetWebsiteDomainsApi api = new GetWebsiteDomainsApi().setSiteId(66);

    assertEquals("data?action=getData&table=domain", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(true, api.getParams().get("list"));
    assertEquals(66, api.getParams().get("search"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new GetWebsiteDomainsApi().setSiteId(0)));
  }

  @Test
  @DisplayName("应正确解析直接返回数组的域名列表响应")
  void parsesRawArrayPayload() {
    GetWebsiteDomainsApi api = new GetWebsiteDomainsApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("[{\"id\":73,\"name\":\"w1.hao.com\",\"port\":80}]");

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertEquals("w1.hao.com", result.getData().get(0).get("name"));
  }

  @Test
  @DisplayName("应正确解析包装后的域名列表响应")
  void parsesWrappedArrayPayload() {
    GetWebsiteDomainsApi api = new GetWebsiteDomainsApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("{\"msg\":\"ok\",\"data\":[{\"name\":\"demo.example.com\"}]}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(List.of(Map.of("name", "demo.example.com")), result.getData());
  }

  @Test
  @DisplayName("应保留失败包装响应的状态与消息")
  void preservesFailurePayload() {
    GetWebsiteDomainsApi api = new GetWebsiteDomainsApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"denied\"}");

    assertFalse(result.isSuccess());
    assertEquals("denied", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("缺少 data 数组时应抛出异常")
  void rejectsMissingDataField() {
    GetWebsiteDomainsApi api = new GetWebsiteDomainsApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("data array"));
  }

  private boolean invokeValidate(GetWebsiteDomainsApi api) {
    try {
      Method method = GetWebsiteDomainsApi.class.getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
