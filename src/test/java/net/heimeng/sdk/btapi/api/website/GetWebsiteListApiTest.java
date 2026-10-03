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

@DisplayName("GetWebsiteListApi 单元测试")
class GetWebsiteListApiTest {

  @Test
  @DisplayName("应暴露正确的接口元数据并校验参数")
  void exposesMetadataAndValidatesParams() {
    GetWebsiteListApi api =
        new GetWebsiteListApi()
            .setPage(1)
            .setLimit(20)
            .setType(-1)
            .setOrder("id desc")
            .setTojs("get_site_list")
            .setSearch("demo");

    assertEquals("data?action=getData&table=sites", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(1, api.getParams().get("p"));
    assertEquals(20, api.getParams().get("limit"));
    assertEquals(-1, api.getParams().get("type"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("缺少 limit 或参数非法时应校验失败")
  void rejectsInvalidParams() {
    assertFalse(invokeValidate(new GetWebsiteListApi().setPage(1)));
    assertFalse(invokeValidate(new GetWebsiteListApi().setLimit(0)));
    assertFalse(invokeValidate(new GetWebsiteListApi().setLimit(10).setPage(0)));
    assertFalse(invokeValidate(new GetWebsiteListApi().setLimit(10).setType(1)));
  }

  @Test
  @DisplayName("应正确解析包装后的站点列表响应")
  void parsesWrappedListPayload() {
    GetWebsiteListApi api = new GetWebsiteListApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse(
            """
            {
              "msg": "ok",
              "data": [
                {
                  "id": 1,
                  "name": "demo.example.com",
                  "tags": ["prod", "cdn"],
                  "ssl": { "enabled": true }
                }
              ]
            }
            """);

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(1, result.getData().size());
    assertEquals("demo.example.com", result.getData().get(0).get("name"));
    assertEquals(List.of("prod", "cdn"), result.getData().get(0).get("tags"));
    assertEquals(Map.of("enabled", true), result.getData().get(0).get("ssl"));
  }

  @Test
  @DisplayName("应兼容直接返回数组的列表响应")
  void parsesRawArrayPayload() {
    GetWebsiteListApi api = new GetWebsiteListApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("[{\"id\":1,\"name\":\"demo.example.com\"}]");

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertEquals("demo.example.com", result.getData().get(0).get("name"));
  }

  @Test
  @DisplayName("应保留失败包装响应的状态与消息")
  void preservesFailurePayload() {
    GetWebsiteListApi api = new GetWebsiteListApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"panel busy\"}");

    assertFalse(result.isSuccess());
    assertEquals("panel busy", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("缺少 data 数组时应抛出异常")
  void rejectsMissingDataField() {
    GetWebsiteListApi api = new GetWebsiteListApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"msg\":\"ok\"}"));

    assertTrue(exception.getMessage().contains("data array"));
  }

  private boolean invokeValidate(GetWebsiteListApi api) {
    try {
      Method method = GetWebsiteListApi.class.getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
