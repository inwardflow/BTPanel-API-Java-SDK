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

@DisplayName("GetWebsitePhpExtensionsApi unit tests")
class GetWebsitePhpExtensionsApiTest {

  @Test
  @DisplayName("should expose metadata and validate params")
  void exposesMetadataAndValidatesParams() {
    GetWebsitePhpExtensionsApi api = new GetWebsitePhpExtensionsApi().setId(8);

    assertEquals("site?action=GetPHPModules", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(8, api.getParams().get("id"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new GetWebsitePhpExtensionsApi().setId(0)));
  }

  @Test
  @DisplayName("should parse wrapped extensions payload")
  void parsesWrappedExtensionsPayload() {
    GetWebsitePhpExtensionsApi api = new GetWebsitePhpExtensionsApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("{\"msg\":\"ok\",\"data\":[{\"name\":\"redis\",\"status\":true}]}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(List.of(Map.of("name", "redis", "status", true)), result.getData());
  }

  @Test
  @DisplayName("should preserve wrapped failure payload")
  void preservesFailurePayload() {
    GetWebsitePhpExtensionsApi api = new GetWebsitePhpExtensionsApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"php not installed\"}");

    assertFalse(result.isSuccess());
    assertEquals("php not installed", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("missing data array should throw")
  void rejectsMissingDataField() {
    GetWebsitePhpExtensionsApi api = new GetWebsitePhpExtensionsApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("data array"));
  }

  private boolean invokeValidate(GetWebsitePhpExtensionsApi api) {
    try {
      Method method = GetWebsitePhpExtensionsApi.class.getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
