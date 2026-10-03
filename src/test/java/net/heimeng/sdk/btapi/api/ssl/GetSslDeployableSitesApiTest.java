package net.heimeng.sdk.btapi.api.ssl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslDeployableSites;

@DisplayName("GetSslDeployableSitesApi tests")
class GetSslDeployableSitesApiTest {

  @Test
  @DisplayName("should encode cert_list as JSON")
  void encodesCertificateNames() {
    GetSslDeployableSitesApi api =
        new GetSslDeployableSitesApi()
            .setCertificateNames(
                java.util.List.of(
                    "integration-test.example.com",
                    "integration-test.example.com",
                    " www.example.com "));

    assertEquals("ssl?action=GetSiteDomain", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(
        "[\"integration-test.example.com\",\"www.example.com\"]", api.getParams().get("cert_list"));
  }

  @Test
  @DisplayName("should parse all and matched site lists")
  void parsesDeployableSitesPayload() {
    GetSslDeployableSitesApi api =
        new GetSslDeployableSitesApi(java.util.List.of("integration-test.example.com"));

    BtResult<SslDeployableSites> result =
        api.parseResponse(
            """
            {
              "all": ["a.example.com", "b.example.com"],
              "site": ["b.example.com"]
            }
            """);

    assertTrue(result.isSuccess());
    assertEquals(2, result.getData().getAllSites().size());
    assertEquals(1, result.getData().getMatchedSites().size());
    assertTrue(result.getData().hasMatchedSites());
  }

  @Test
  @DisplayName("should preserve failure wrappers")
  void preservesFailureWrapper() {
    GetSslDeployableSitesApi api =
        new GetSslDeployableSitesApi(java.util.List.of("integration-test.example.com"));

    BtResult<SslDeployableSites> result =
        api.parseResponse("{\"status\":false,\"msg\":\"forbidden\"}");

    assertFalse(result.isSuccess());
    assertEquals("forbidden", result.getMsg());
  }

  @Test
  @DisplayName("should reject empty certificate lists")
  void rejectsEmptyCertificateNames() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new GetSslDeployableSitesApi().setCertificateNames(java.util.List.of(" ", "")));

    assertTrue(exception.getMessage().contains("certificateNames"));
  }
}
