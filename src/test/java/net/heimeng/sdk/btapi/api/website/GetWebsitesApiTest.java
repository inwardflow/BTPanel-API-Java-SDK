package net.heimeng.sdk.btapi.api.website;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.WebsiteInfo;

/** {@link GetWebsitesApi} 的单元测试。 */
@DisplayName("GetWebsitesApi tests")
class GetWebsitesApiTest {

  private GetWebsitesApi websitesApi;

  @BeforeEach
  void setUp() {
    websitesApi = new GetWebsitesApi();
  }

  @Test
  @DisplayName("API metadata matches the BT Panel endpoint contract")
  void apiBasicInfo() {
    assertEquals("data?action=getData&table=sites", websitesApi.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, websitesApi.getMethod());
  }

  @Test
  @DisplayName("Default paging parameters are applied")
  void defaultPaginationParams() {
    Map<String, Object> params = websitesApi.getParams();

    assertEquals(1, params.get("p"));
    assertEquals(10, params.get("limit"));
  }

  @Test
  @DisplayName("Custom paging parameters override defaults")
  void customPaginationParams() {
    GetWebsitesApi customApi = new GetWebsitesApi(2, 20);
    Map<String, Object> params = customApi.getParams();

    assertEquals(2, params.get("p"));
    assertEquals(20, params.get("limit"));
  }

  @Test
  @DisplayName("Response parsing maps common website fields")
  void parseResponseSuccess() {
    String response =
        "{"
            + "\"msg\":\"Success\","
            + "\"data\":["
            + "{\"id\":1,\"name\":\"example.com\",\"path\":\"/www/wwwroot/example.com\","
            + "\"project_type\":\"php\",\"status\":\"1\",\"ssl\":1,"
            + "\"addtime\":\"2021-01-01 12:00:00\"},"
            + "{\"id\":2,\"name\":\"test.com\",\"path\":\"/www/wwwroot/test.com\","
            + "\"project_type\":\"html\",\"status\":\"0\",\"ssl\":0,"
            + "\"addtime\":\"2021-01-02 13:30:00\"}"
            + "]}";

    BtResult<List<WebsiteInfo>> result = websitesApi.parseResponse(response);

    assertTrue(result.isSuccess());
    assertEquals("Success", result.getMsg());
    assertNotNull(result.getData());
    assertEquals(2, result.getData().size());

    WebsiteInfo firstWebsite = result.getData().get(0);
    assertEquals(1L, firstWebsite.getId());
    assertEquals("example.com", firstWebsite.getName());
    assertEquals("example.com", firstWebsite.getDomain());
    assertEquals("php", firstWebsite.getType());
    assertEquals(1, firstWebsite.getStatus());
    assertEquals(1, firstWebsite.getSsl());
    assertTrue(firstWebsite.isRunning());
    assertTrue(firstWebsite.isSslEnabled());
    assertNotNull(firstWebsite.getCreateTime());

    WebsiteInfo secondWebsite = result.getData().get(1);
    assertFalse(secondWebsite.isRunning());
    assertFalse(secondWebsite.isSslEnabled());
  }

  @Test
  @DisplayName("Empty data arrays are supported")
  void parseResponseEmptyData() {
    BtResult<List<WebsiteInfo>> result =
        websitesApi.parseResponse("{\"msg\":\"Success\",\"data\":[]}");

    assertTrue(result.isSuccess());
    assertNotNull(result.getData());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("Null source fields remain null in parsed output")
  void parseResponsePreservesNulls() {
    String response =
        "{"
            + "\"msg\":\"Success\","
            + "\"data\":["
            + "{\"id\":null,\"name\":null,\"path\":null,\"project_type\":null,"
            + "\"status\":null,\"ssl\":null,\"addtime\":null}"
            + "]}";

    BtResult<List<WebsiteInfo>> result = websitesApi.parseResponse(response);
    WebsiteInfo website = result.getData().get(0);

    assertNull(website.getId());
    assertNull(website.getName());
    assertNull(website.getPath());
    assertNull(website.getType());
    assertNull(website.getStatus());
    assertNull(website.getSsl());
    assertNull(website.getCreateTime());
  }

  @Test
  @DisplayName("Invalid JSON is rejected")
  void parseResponseRejectsInvalidJson() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> websitesApi.parseResponse("{invalid json}"));

    assertTrue(exception.getMessage().contains("Invalid JSON response"));
  }

  @Test
  @DisplayName("Missing data field is rejected")
  void parseResponseRejectsMissingDataField() {
    BtApiException exception =
        assertThrows(
            BtApiException.class, () -> websitesApi.parseResponse("{\"msg\":\"Success\"}"));

    assertTrue(exception.getMessage().contains("Missing required data field"));
  }
}
