package net.heimeng.sdk.btapi.api.ssl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslSiteStatus;

@DisplayName("GetWebsiteSslStatusApi tests")
class GetWebsiteSslStatusApiTest {

  @Test
  @DisplayName("should expose the verified endpoint metadata")
  void exposesMetadata() {
    GetWebsiteSslStatusApi api = new GetWebsiteSslStatusApi("demo.example.com");

    assertEquals("site?action=GetSSL", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals("demo.example.com", api.getParams().get("siteName"));
  }

  @Test
  @DisplayName("should treat undeployed status=false as a successful query")
  void parsesUndeployedPayload() {
    GetWebsiteSslStatusApi api = new GetWebsiteSslStatusApi("demo.example.com");

    BtResult<SslSiteStatus> result =
        api.parseResponse(
            """
            {
              "status": false,
              "oid": -1,
              "domain": [{"name": "demo.example.com"}],
              "key": false,
              "csr": false,
              "type": -1,
              "httpTohttps": false,
              "cert_data": {},
              "email": "ops@example.com"
            }
            """);

    assertTrue(result.isSuccess());
    assertFalse(result.getData().isEnabled());
    assertEquals(1, result.getData().getDomains().size());
    assertNull(result.getData().getPrivateKeyPem());
    assertNull(result.getData().getCertificatePem());
    assertNull(result.getData().getCertificateDetail());
  }

  @Test
  @DisplayName("should parse deployed certificate details")
  void parsesDeployedPayload() {
    GetWebsiteSslStatusApi api = new GetWebsiteSslStatusApi("demo.example.com");

    BtResult<SslSiteStatus> result =
        api.parseResponse(
            """
            {
              "status": true,
              "oid": 12,
              "domain": [{"name": "demo.example.com"}],
              "key": "<private-key-pem>",
              "csr": "<certificate-pem>",
              "type": 0,
              "httpTohttps": true,
              "cert_data": {
                "issuer": "integration-test.example.com",
                "notBefore": "2026-03-24",
                "notAfter": "2036-03-21",
                "subject": "integration-test.example.com",
                "id": 14
              }
            }
            """);

    assertTrue(result.isSuccess());
    assertTrue(result.getData().isEnabled());
    assertEquals(12, result.getData().getOrderId());
    assertTrue(result.getData().hasCertificate());
    assertNotNull(result.getData().getCertificateDetail());
    assertEquals(14, result.getData().getCertificateDetail().getId());
    assertEquals(
        "integration-test.example.com", result.getData().getCertificateDetail().getSubject());
    assertNotNull(result.getData().getCertificateDetail().getValidFrom());
    assertNotNull(result.getData().getCertificateDetail().getValidTo());
  }

  @Test
  @DisplayName("should preserve failure wrappers for invalid requests")
  void preservesFailureWrapper() {
    GetWebsiteSslStatusApi api = new GetWebsiteSslStatusApi("demo.example.com");

    BtResult<SslSiteStatus> result =
        api.parseResponse("{\"status\":false,\"msg\":\"site not found\"}");

    assertFalse(result.isSuccess());
    assertEquals("site not found", result.getMsg());
    assertNull(result.getData());
  }

  @Test
  @DisplayName("should reject payloads without status fields")
  void rejectsPayloadWithoutRequiredFields() {
    GetWebsiteSslStatusApi api = new GetWebsiteSslStatusApi("demo.example.com");

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"foo\":\"bar\"}"));

    assertTrue(exception.getMessage().contains("payload fields"));
  }
}
