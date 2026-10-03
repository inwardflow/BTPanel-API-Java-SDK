package net.heimeng.sdk.btapi.api.ssl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("GetSslOrderListApi tests")
class GetSslOrderListApiTest {

  @Test
  @DisplayName("should expose the verified endpoint metadata")
  void exposesMetadata() {
    GetSslOrderListApi api = new GetSslOrderListApi("demo.example.com");

    assertEquals("ssl?action=get_order_list", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals("demo.example.com", api.getParams().get("siteName"));
  }

  @Test
  @DisplayName("should parse direct array responses")
  void parsesDirectArrayResponse() {
    GetSslOrderListApi api = new GetSslOrderListApi("demo.example.com");

    BtResult<List<Map<String, Object>>> result =
        api.parseResponse(
            """
            [
              {
                "id": 1,
                "siteName": "demo.example.com"
              }
            ]
            """);

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertEquals("demo.example.com", result.getData().get(0).get("siteName"));
  }

  @Test
  @DisplayName("should preserve failure wrappers")
  void preservesFailureWrapper() {
    GetSslOrderListApi api = new GetSslOrderListApi("demo.example.com");

    BtResult<List<Map<String, Object>>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"forbidden\"}");

    assertFalse(result.isSuccess());
    assertEquals("forbidden", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("should reject wrapped success responses without data")
  void rejectsWrappedPayloadWithoutData() {
    GetSslOrderListApi api = new GetSslOrderListApi("demo.example.com");

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("data array"));
  }
}
