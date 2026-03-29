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

@DisplayName("GetWebsiteBackupsApi unit tests")
class GetWebsiteBackupsApiTest {

  @Test
  @DisplayName("should expose metadata and validate params")
  void exposesMetadataAndValidatesParams() {
    GetWebsiteBackupsApi api = new GetWebsiteBackupsApi().setPage(1).setLimit(20).setSiteId(8);

    assertEquals("data?action=getData&table=backup", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(1, api.getParams().get("p"));
    assertEquals(20, api.getParams().get("limit"));
    assertEquals(8, api.getParams().get("search"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new GetWebsiteBackupsApi().setLimit(20).setSiteId(0)));
  }

  @Test
  @DisplayName("should parse wrapped backup list payload")
  void parsesWrappedBackupPayload() {
    GetWebsiteBackupsApi api = new GetWebsiteBackupsApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("{\"msg\":\"ok\",\"data\":[{\"name\":\"backup.tar.gz\"}]}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(List.of(Map.of("name", "backup.tar.gz")), result.getData());
  }

  @Test
  @DisplayName("should preserve wrapped failure payload")
  void preservesFailurePayload() {
    GetWebsiteBackupsApi api = new GetWebsiteBackupsApi();
    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"busy\"}");

    assertFalse(result.isSuccess());
    assertEquals("busy", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("missing data array should throw")
  void rejectsMissingDataArray() {
    GetWebsiteBackupsApi api = new GetWebsiteBackupsApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("data array"));
  }

  private boolean invokeValidate(GetWebsiteBackupsApi api) {
    try {
      Method method = GetWebsiteBackupsApi.class.getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
