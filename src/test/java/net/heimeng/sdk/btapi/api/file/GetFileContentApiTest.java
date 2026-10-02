package net.heimeng.sdk.btapi.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("GetFileContentApi 单元测试")
class GetFileContentApiTest {

  @Test
  @DisplayName("应正确暴露接口元数据并校验参数")
  void exposesMetadataAndValidatesParams() {
    GetFileContentApi api = new GetFileContentApi().setPath("/www/test.conf");

    assertEquals("files?action=GetFileBody", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals("/www/test.conf", api.getParams().get("path"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("应支持直接返回的原始文本内容")
  void parsesRawTextContent() {
    GetFileContentApi api = new GetFileContentApi();
    BtResult<String> result = api.parseResponse("server { listen 80; }");

    assertTrue(result.isSuccess());
    assertEquals("server { listen 80; }", result.getData());
  }

  @Test
  @DisplayName("应将非包装 JSON 视为原始文件内容")
  void treatsJsonFileContentAsRawContent() {
    GetFileContentApi api = new GetFileContentApi();
    BtResult<String> result = api.parseResponse("{\"name\":\"demo\"}");

    assertTrue(result.isSuccess());
    assertEquals("{\"name\":\"demo\"}", result.getData());
  }

  @Test
  @DisplayName("应正确解析标准包装响应")
  void parsesWrappedResponse() {
    GetFileContentApi api = new GetFileContentApi();
    BtResult<String> result =
        api.parseResponse("{\"status\":true,\"msg\":\"ok\",\"data\":\"hello\"}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals("hello", result.getData());
  }

  @Test
  @DisplayName("应保留失败包装响应")
  void preservesFailureResponse() {
    GetFileContentApi api = new GetFileContentApi();
    BtResult<String> result =
        api.parseResponse("{\"status\":false,\"msg\":\"denied\",\"data\":\"\"}");

    assertTrue(result.isFailed());
    assertEquals("denied", result.getMsg());
  }

  @Test
  @DisplayName("空响应应抛出异常")
  void rejectsBlankResponse() {
    GetFileContentApi api = new GetFileContentApi();

    BtApiException exception = assertThrows(BtApiException.class, () -> api.parseResponse(" "));

    assertTrue(exception.getMessage().contains("Empty response"));
  }

  private boolean invokeValidate(GetFileContentApi api) {
    try {
      Method method = GetFileContentApi.class.getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
