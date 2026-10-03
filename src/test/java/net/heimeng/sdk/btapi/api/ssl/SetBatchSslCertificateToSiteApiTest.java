package net.heimeng.sdk.btapi.api.ssl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.facade.SslBatchDeploymentRequest;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslBatchDeploymentResult;

@DisplayName("SetBatchSslCertificateToSiteApi tests")
class SetBatchSslCertificateToSiteApiTest {

  @Test
  @DisplayName("should encode batch deployment payloads as JSON")
  void encodesBatchPayload() {
    SetBatchSslCertificateToSiteApi api =
        new SetBatchSslCertificateToSiteApi()
            .addDeployment("hash-1", "a.example.com", "cert-a")
            .addDeployment(SslBatchDeploymentRequest.of("hash-2", "b.example.com", "cert-b"));

    assertEquals("ssl?action=SetBatchCertToSite", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(
        "[{\"ssl_hash\":\"hash-1\",\"siteName\":\"a.example.com\",\"certName\":\"cert-a\"},"
            + "{\"ssl_hash\":\"hash-2\",\"siteName\":\"b.example.com\",\"certName\":\"cert-b\"}]",
        api.getParams().get("BatchInfo"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("should parse a fully successful deployment")
  void parsesSuccessResponse() {
    SetBatchSslCertificateToSiteApi api =
        new SetBatchSslCertificateToSiteApi("hash-1", "a.example.com", "cert-a");

    BtResult<SslBatchDeploymentResult> result =
        api.parseResponse(
            """
            {
              "total": 1,
              "success": 1,
              "faild": 0,
              "successList": [
                {
                  "status": true,
                  "certName": "cert-a",
                  "siteName": "a.example.com"
                }
              ],
              "faildList": []
            }
            """);

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().getTotal());
    assertEquals(1, result.getData().getSuccessCount());
    assertEquals(0, result.getData().getFailedCount());
    assertTrue(result.getData().isFullySuccessful());
  }

  @Test
  @DisplayName("should keep partial failures accessible to callers")
  void keepsPartialFailuresAccessible() {
    SetBatchSslCertificateToSiteApi api =
        new SetBatchSslCertificateToSiteApi()
            .setBatch(List.of(SslBatchDeploymentRequest.of("hash-1", "a.example.com", "cert-a")));

    BtResult<SslBatchDeploymentResult> result =
        api.parseResponse(
            """
            {
              "total": 1,
              "success": 0,
              "faild": 1,
              "successList": [],
              "faildList": [
                {
                  "status": false,
                  "certName": "cert-a",
                  "siteName": "a.example.com",
                  "msg": "domain mismatch"
                }
              ]
            }
            """);

    assertTrue(result.isSuccess());
    assertFalse(result.getData().isFullySuccessful());
    assertEquals(1, result.getData().getFailedList().size());
    assertEquals("domain mismatch", result.getData().getFailedList().get(0).getMessage());
  }

  @Test
  @DisplayName("should preserve failure wrappers")
  void preservesFailureWrapper() {
    SetBatchSslCertificateToSiteApi api =
        new SetBatchSslCertificateToSiteApi("hash-1", "a.example.com", "cert-a");

    BtResult<SslBatchDeploymentResult> result =
        api.parseResponse("{\"status\":false,\"msg\":\"forbidden\"}");

    assertFalse(result.isSuccess());
    assertEquals("forbidden", result.getMsg());
  }

  @Test
  @DisplayName("should reject empty batches")
  void rejectsEmptyBatches() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new SetBatchSslCertificateToSiteApi().setBatch(List.of()));

    assertTrue(exception.getMessage().contains("requests"));
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
