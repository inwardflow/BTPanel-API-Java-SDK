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
import net.heimeng.sdk.btapi.model.website.PhpVersion;

@DisplayName("GetPhpVersionsApi 单元测试")
class GetPhpVersionsApiTest {

  @Test
  @DisplayName("应暴露正确的接口元数据")
  void exposesMetadata() {
    GetPhpVersionsApi api = new GetPhpVersionsApi();

    assertEquals("site?action=GetPHPVersion", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
  }

  @Test
  @DisplayName("应正确解析直接返回数组的 PHP 版本响应")
  void parsesRawArrayPayload() {
    GetPhpVersionsApi api = new GetPhpVersionsApi();
    BtResult<List<PhpVersion>> result =
        api.parseResponse("[{\"version\":\"82\",\"name\":\"PHP-82\"}]");

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertEquals("82", result.getData().get(0).getVersion());
    assertEquals("PHP-82", result.getData().get(0).getName());
  }

  @Test
  @DisplayName("应正确解析包装后的 PHP 版本响应")
  void parsesWrappedArrayPayload() {
    GetPhpVersionsApi api = new GetPhpVersionsApi();
    BtResult<List<PhpVersion>> result =
        api.parseResponse("{\"msg\":\"ok\",\"data\":[{\"version\":\"00\",\"name\":\"纯静态\"}]}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(1, result.getData().size());
  }

  @Test
  @DisplayName("应保留失败包装响应的状态与消息")
  void preservesFailurePayload() {
    GetPhpVersionsApi api = new GetPhpVersionsApi();
    BtResult<List<PhpVersion>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"unsupported\"}");

    assertFalse(result.isSuccess());
    assertEquals("unsupported", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("成功包装响应缺少 data 数组时应抛出异常")
  void rejectsMissingDataField() {
    GetPhpVersionsApi api = new GetPhpVersionsApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("data array"));
  }
}
