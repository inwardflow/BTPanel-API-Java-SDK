package net.heimeng.sdk.btapi.api.ssl;

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

@DisplayName("SSL 布尔型 API 单元测试")
class SslBooleanApisTest {

  @Test
  @DisplayName("安装 SSL 证书 API 契约应正确")
  void installSslCertificateApiContract() {
    InstallSslCertificateApi api =
        new InstallSslCertificateApi()
            .setDomain("demo.example.com")
            .setKey("private-key")
            .setCert("certificate-content");

    assertEquals("site?action=SetSSL", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals("demo.example.com", api.getParams().get("siteName"));
    assertEquals("private-key", api.getParams().get("key"));
    assertEquals("certificate-content", api.getParams().get("csr"));
    assertFalse(api.getParams().containsKey("force_https"));
    assertFalse(api.getParams().containsKey("auto_renew"));
    assertTrue(invokeValidate(api));

    BtResult<Boolean> result = api.parseResponse("{\"status\":true,\"msg\":\"ok\"}");
    assertTrue(result.isSuccess());
    assertTrue(result.getData());
  }

  @Test
  @DisplayName("安装 SSL 证书 API 应忽略空 CA 参数")
  void installSslCertificateApiIgnoresBlankCa() {
    InstallSslCertificateApi api = new InstallSslCertificateApi();
    api.setCa(" ");

    assertFalse(api.getParams().containsKey("ca"));
  }

  @Test
  @DisplayName("删除 SSL 证书 API 契约应正确")
  void deleteSslCertificateApiContract() {
    DeleteSslCertificateApi api = new DeleteSslCertificateApi().setId(12).setDomain("demo.com");

    assertEquals("ssl?action=remove_cloud_cert", api.getEndpoint());
    assertEquals(12, api.getParams().get("ssl_id"));
    assertEquals(1, api.getParams().get("local"));
    assertFalse(api.getParams().containsKey("domain"));
    assertTrue(invokeValidate(api));

    BtResult<Boolean> result = api.parseResponse("{\"status\":false,\"msg\":\"missing\"}");
    assertFalse(result.isSuccess());
    assertFalse(result.getData());
    assertEquals("missing", result.getMsg());
  }

  @Test
  @DisplayName("删除 SSL 证书 API 应拒绝非正数 ID")
  void deleteSslCertificateApiRejectsNonPositiveId() {
    DeleteSslCertificateApi api = new DeleteSslCertificateApi();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> api.setId(0));

    assertTrue(exception.getMessage().contains("positive"));
  }

  @Test
  @DisplayName("SSL 布尔型 API 应拒绝缺少状态字段的响应")
  void booleanApisRejectMissingStatusField() {
    InstallSslCertificateApi api = new InstallSslCertificateApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"msg\":\"ok\"}"));

    assertTrue(exception.getMessage().contains("status field"));
  }

  @Test
  @DisplayName("SSL 布尔型 API 应拒绝非法 JSON 响应")
  void booleanApisRejectInvalidJson() {
    DeleteSslCertificateApi api = new DeleteSslCertificateApi();

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
