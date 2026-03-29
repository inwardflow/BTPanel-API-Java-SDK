package net.heimeng.sdk.btapi.api.website;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.WebsiteType;

@DisplayName("GetWebsiteTypesApi 单元测试")
class GetWebsiteTypesApiTest {

  @Test
  @DisplayName("应暴露正确的接口元数据")
  void exposesMetadata() {
    GetWebsiteTypesApi api = new GetWebsiteTypesApi();

    assertEquals("site?action=get_site_types", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
  }

  @Test
  @DisplayName("应正确解析直接返回数组的分类响应")
  void parsesRawArrayPayload() {
    GetWebsiteTypesApi api = new GetWebsiteTypesApi();
    BtResult<List<WebsiteType>> result = api.parseResponse("[{\"id\":0,\"name\":\"默认分类\"}]");

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertEquals(0, result.getData().get(0).getId());
    assertEquals("默认分类", result.getData().get(0).getName());
  }

  @Test
  @DisplayName("应正确解析包装后的分类响应")
  void parsesWrappedArrayPayload() {
    GetWebsiteTypesApi api = new GetWebsiteTypesApi();
    BtResult<List<WebsiteType>> result =
        api.parseResponse("{\"msg\":\"ok\",\"data\":[{\"id\":1,\"name\":\"业务站点\"}]}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(1, result.getData().size());
  }

  @Test
  @DisplayName("应保留失败包装响应的状态与消息")
  void preservesFailurePayload() {
    GetWebsiteTypesApi api = new GetWebsiteTypesApi();
    BtResult<List<WebsiteType>> result = api.parseResponse("{\"status\":false,\"msg\":\"busy\"}");

    assertFalse(result.isSuccess());
    assertEquals("busy", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("成功包装响应缺少 data 数组时应抛出异常")
  void rejectsMissingDataField() {
    GetWebsiteTypesApi api = new GetWebsiteTypesApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("data array"));
  }
}
