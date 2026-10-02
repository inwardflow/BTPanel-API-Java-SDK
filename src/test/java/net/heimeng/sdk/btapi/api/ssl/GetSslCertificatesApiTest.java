package net.heimeng.sdk.btapi.api.ssl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;

@DisplayName("GetSslCertificatesApi 单元测试")
class GetSslCertificatesApiTest {

  private final GetSslCertificatesApi api = new GetSslCertificatesApi();

  @Test
  @DisplayName("应暴露正确的接口元数据")
  void exposesMetadata() {
    assertEquals("ssl?action=get_cert_list", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertTrue(api.getParams().isEmpty());
  }

  @Test
  @DisplayName("应正确解析新版 SSL 证书列表响应")
  void parsesSuccessfulCertificateList() {
    BtResult<List<SslCertificate>> result =
        api.parseResponse(
            """
            [
              {
                "id": 3,
                "hash": "AA:BB:CC",
                "dns": ["demo.example.com", "www.demo.example.com"],
                "subject": "demo.example.com",
                "info": {
                  "issuer": "Let's Encrypt",
                  "notBefore": "2024-01-01",
                  "notAfter": "2099-01-01"
                },
                "not_after": "2099-01-01",
                "endtime": 365
              }
            ]
            """);

    assertTrue(result.isSuccess());
    assertEquals("Success", result.getMsg());
    assertEquals(1, result.getData().size());

    SslCertificate certificate = result.getData().get(0);
    assertEquals(3, certificate.getId());
    assertEquals("demo.example.com", certificate.getName());
    assertEquals("", certificate.getType());
    assertEquals(List.of("demo.example.com", "www.demo.example.com"), certificate.getDomains());
    assertEquals("Let's Encrypt", certificate.getIssuer());
    assertFalse(certificate.isAutoRenew());
    assertEquals("AA:BB:CC", certificate.getHash());
    assertEquals("AA:BB:CC", certificate.getFingerprint());
    assertNotNull(certificate.getValidFrom());
    assertNotNull(certificate.getValidTo());
    assertTrue(certificate.isValid());
  }

  @Test
  @DisplayName("应兼容旧版包装对象响应")
  void supportsLegacyWrappedPayload() {
    BtResult<List<SslCertificate>> result =
        api.parseResponse(
            """
            {
              "status": true,
              "msg": "ok",
              "data": [
                {
                  "id": 9,
                  "domains": "a.example.com,b.example.com"
                }
              ]
            }
            """);

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(List.of("a.example.com", "b.example.com"), result.getData().get(0).getDomains());
  }

  @Test
  @DisplayName("应保留失败响应的状态与消息")
  void preservesFailurePayload() {
    BtResult<List<SslCertificate>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"forbidden\"}");

    assertFalse(result.isSuccess());
    assertEquals("forbidden", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("成功响应缺少 data 数组时应抛出异常")
  void rejectsSuccessfulPayloadWithoutData() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("data array"));
  }

  @Test
  @DisplayName("非法时间格式应被忽略")
  void ignoresInvalidDateFields() {
    BtResult<List<SslCertificate>> result = api.parseResponse("[{\"not_after\":\"bad\"}]");

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertEquals("unknown", result.getData().get(0).getStatus());
  }

  @Test
  @DisplayName("空响应应抛出异常")
  void rejectsBlankResponse() {
    BtApiException exception = assertThrows(BtApiException.class, () -> api.parseResponse(" "));

    assertTrue(exception.getMessage().contains("Empty response"));
  }
}
