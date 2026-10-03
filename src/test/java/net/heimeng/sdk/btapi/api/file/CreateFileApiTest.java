package net.heimeng.sdk.btapi.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("创建文件 API 单元测试")
class CreateFileApiTest {

  @Test
  @DisplayName("创建文件 API 契约正确")
  void createFileApiContract() {
    CreateFileApi api = new CreateFileApi().setPath("/www/test.txt");

    assertEquals("files?action=CreateFile", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals("/www/test.txt", api.getParams().get("path"));
    assertTrue(invokeValidate(api));

    BtResult<Boolean> result = api.parseResponse("{\"status\":true,\"msg\":\"ok\"}");
    assertTrue(result.isSuccess());
    assertTrue(result.getData());
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
