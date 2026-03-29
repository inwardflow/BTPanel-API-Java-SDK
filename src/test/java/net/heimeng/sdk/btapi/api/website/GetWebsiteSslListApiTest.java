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

@DisplayName("GetWebsiteSslListApi unit tests")
class GetWebsiteSslListApiTest {

  @Test
  @DisplayName("should expose metadata and validate params")
  void exposesMetadataAndValidatesParams() {
    GetWebsiteSslListApi api = new GetWebsiteSslListApi().setId(8);

    assertEquals("site?action=GetSSLCertList", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(8, api.getParams().get("id"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new GetWebsiteSslListApi().setId(0)));
  }

  @Test
  @DisplayName("should parse wrapped cert list payload")
  void parsesWrappedCertListPayload() {
    GetWebsiteSslListApi api = new GetWebsiteSslListApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse(
            "{\"msg\":\"ok\",\"certs\":[{\"subject\":\"demo.example.com\",\"issuer\":\"LetsEncrypt\"}]}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(
        List.of(Map.of("subject", "demo.example.com", "issuer", "LetsEncrypt")), result.getData());
  }

  @Test
  @DisplayName("should preserve wrapped failure payload")
  void preservesFailurePayload() {
    GetWebsiteSslListApi api = new GetWebsiteSslListApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"ssl disabled\"}");

    assertFalse(result.isSuccess());
    assertEquals("ssl disabled", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("missing certs array should throw")
  void rejectsMissingCertsField() {
    GetWebsiteSslListApi api = new GetWebsiteSslListApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("certs array"));
  }

  private boolean invokeValidate(GetWebsiteSslListApi api) {
    try {
      Method method = GetWebsiteSslListApi.class.getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
