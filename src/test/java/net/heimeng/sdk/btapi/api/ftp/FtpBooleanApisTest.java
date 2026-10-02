package net.heimeng.sdk.btapi.api.ftp;

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

@DisplayName("FTP 布尔型 API 单元测试")
class FtpBooleanApisTest {

  @Test
  @DisplayName("创建 FTP 账户 API 契约应正确")
  void createFtpAccountApiContract() {
    CreateFtpAccountApi api =
        new CreateFtpAccountApi()
            .setUsername("demo")
            .setPassword("secret")
            .setPath("/www/wwwroot/demo")
            .setRemark("demo");

    assertEquals("ftp?action=AddUser", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals("demo", api.getParams().get("ftp_username"));
    assertEquals("secret", api.getParams().get("ftp_password"));
    assertEquals("/www/wwwroot/demo", api.getParams().get("path"));
    assertEquals("demo", api.getParams().get("ps"));
    assertTrue(invokeValidate(api));

    BtResult<Boolean> result = api.parseResponse("{\"status\":true,\"msg\":\"ok\"}");
    assertTrue(result.isSuccess());
    assertTrue(result.getData());
  }

  @Test
  @DisplayName("创建 FTP 账户 API 应拒绝负数配额")
  void createFtpAccountApiRejectsNegativeSize() {
    CreateFtpAccountApi api = new CreateFtpAccountApi();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> api.setSize(-1L));

    assertTrue(exception.getMessage().contains("size"));
  }

  @Test
  @DisplayName("删除 FTP 账户 API 契约应正确")
  void deleteFtpAccountApiContract() {
    DeleteFtpAccountApi api = new DeleteFtpAccountApi().setId(9).setUsername("demo");

    assertEquals("ftp?action=DeleteUser", api.getEndpoint());
    assertEquals(9, api.getParams().get("id"));
    assertEquals("demo", api.getParams().get("username"));
    assertTrue(invokeValidate(api));

    BtResult<Boolean> result = api.parseResponse("{\"status\":false,\"msg\":\"forbidden\"}");
    assertFalse(result.isSuccess());
    assertFalse(result.getData());
    assertEquals("forbidden", result.getMsg());
  }

  @Test
  @DisplayName("修改 FTP 密码 API 契约应正确")
  void changeFtpPasswordApiContract() {
    ChangeFtpPasswordApi api =
        new ChangeFtpPasswordApi()
            .setId(9)
            .setUsername("demo")
            .setNewPassword("new-secret")
            .setPath("/www/wwwroot/demo");

    assertEquals("ftp?action=SetUser", api.getEndpoint());
    assertEquals(9, api.getParams().get("id"));
    assertEquals("demo", api.getParams().get("ftp_username"));
    assertEquals("new-secret", api.getParams().get("new_password"));
    assertEquals("/www/wwwroot/demo", api.getParams().get("path"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("FTP 布尔型 API 应拒绝缺少状态字段的响应")
  void booleanApisRejectMissingStatusField() {
    CreateFtpAccountApi api = new CreateFtpAccountApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"msg\":\"ok\"}"));

    assertTrue(exception.getMessage().contains("status field"));
  }

  @Test
  @DisplayName("FTP 布尔型 API 应拒绝非法 JSON 响应")
  void booleanApisRejectInvalidJson() {
    DeleteFtpAccountApi api = new DeleteFtpAccountApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("plain text"));

    assertTrue(exception.getMessage().contains("Invalid JSON"));
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
