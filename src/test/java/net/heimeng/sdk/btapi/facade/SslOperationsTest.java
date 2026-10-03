package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.heimeng.sdk.btapi.api.ssl.DeleteSslCertificateApi;
import net.heimeng.sdk.btapi.api.ssl.GetSslCertificatesApi;
import net.heimeng.sdk.btapi.api.ssl.GetSslDeployableSitesApi;
import net.heimeng.sdk.btapi.api.ssl.GetSslOrderListApi;
import net.heimeng.sdk.btapi.api.ssl.GetWebsiteSslStatusApi;
import net.heimeng.sdk.btapi.api.ssl.SetBatchSslCertificateToSiteApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslBatchDeploymentResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;
import net.heimeng.sdk.btapi.model.ssl.SslDeployableSites;
import net.heimeng.sdk.btapi.model.ssl.SslSiteStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("SslOperations facade tests")
class SslOperationsTest {

  @Mock private BtClient client;

  @Test
  @DisplayName("list should delegate to the certificate list API")
  void listDelegatesToClient() {
    SslOperations operations = new SslOperations(client);
    when(client.execute(any(GetSslCertificatesApi.class))).thenReturn(successListResponse());

    BtResult<List<SslCertificate>> result = operations.list();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetSslCertificatesApi.class));
  }

  @Test
  @DisplayName("getWebsiteStatus should delegate to the verified GetSSL API")
  void getWebsiteStatusDelegatesToClient() {
    SslOperations operations = new SslOperations(client);
    when(client.execute(any(GetWebsiteSslStatusApi.class))).thenReturn(successWebsiteStatus());

    BtResult<SslSiteStatus> result = operations.getWebsiteStatus("demo.example.com");

    assertTrue(result.isSuccess());
    assertTrue(result.getData().isEnabled());
    verify(client).execute(any(GetWebsiteSslStatusApi.class));
  }

  @Test
  @DisplayName("listOrders should delegate to the verified get_order_list API")
  void listOrdersDelegatesToClient() {
    SslOperations operations = new SslOperations(client);
    when(client.execute(any(GetSslOrderListApi.class))).thenReturn(successOrderList());

    BtResult<List<Map<String, Object>>> result = operations.listOrders("demo.example.com");

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetSslOrderListApi.class));
  }

  @Test
  @DisplayName("getDeployableSites should delegate to the verified GetSiteDomain API")
  void getDeployableSitesDelegatesToClient() {
    SslOperations operations = new SslOperations(client);
    when(client.execute(any(GetSslDeployableSitesApi.class)))
        .thenReturn(successDeployableSitesResponse());

    BtResult<SslDeployableSites> result =
        operations.getDeployableSites(List.of("integration-test.example.com"));

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().getMatchedSites().size());
    verify(client).execute(any(GetSslDeployableSitesApi.class));
  }

  @Test
  @DisplayName("deploySavedCertificate should delegate to the verified batch deployment API")
  void deploySavedCertificateDelegatesToClient() {
    SslOperations operations = new SslOperations(client);
    when(client.execute(any(SetBatchSslCertificateToSiteApi.class)))
        .thenReturn(successDeploymentResponse());

    BtResult<SslBatchDeploymentResult> result =
        operations.deploySavedCertificate(
            "hash-123", "demo.example.com", "integration-test.example.com");

    assertTrue(result.isSuccess());
    assertTrue(result.getData().isFullySuccessful());
    verify(client).execute(any(SetBatchSslCertificateToSiteApi.class));
  }

  @Test
  @DisplayName("deploySavedCertificates should accept explicit batch requests")
  void deploySavedCertificatesDelegatesToClient() {
    SslOperations operations = new SslOperations(client);
    when(client.execute(any(SetBatchSslCertificateToSiteApi.class)))
        .thenReturn(successDeploymentResponse());

    BtResult<SslBatchDeploymentResult> result =
        operations.deploySavedCertificates(
            List.of(
                SslBatchDeploymentRequest.of(
                    "hash-123", "demo.example.com", "integration-test.example.com")));

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetBatchSslCertificateToSiteApi.class));
  }

  @Test
  @DisplayName("delete should still delegate to the delete API")
  void deleteDelegatesToClient() {
    SslOperations operations = new SslOperations(client);
    when(client.execute(any(DeleteSslCertificateApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.delete(3);

    assertTrue(result.isSuccess());
    verify(client).execute(any(DeleteSslCertificateApi.class));
  }

  private static BtResult<Boolean> successBoolean() {
    BtResult<Boolean> response = new BtResult<>();
    response.setStatus(true);
    response.setData(true);
    return response;
  }

  private static BtResult<List<SslCertificate>> successListResponse() {
    SslCertificate certificate = new SslCertificate();
    certificate.setName("demo-cert");

    BtResult<List<SslCertificate>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(certificate));
    return response;
  }

  private static BtResult<SslSiteStatus> successWebsiteStatus() {
    SslSiteStatus status = new SslSiteStatus();
    status.setEnabled(true);

    BtResult<SslSiteStatus> response = new BtResult<>();
    response.setStatus(true);
    response.setData(status);
    return response;
  }

  private static BtResult<List<Map<String, Object>>> successOrderList() {
    BtResult<List<Map<String, Object>>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(Map.of("id", 1, "siteName", "demo.example.com")));
    return response;
  }

  private static BtResult<SslDeployableSites> successDeployableSitesResponse() {
    SslDeployableSites payload = new SslDeployableSites();
    payload.setAllSites(List.of("demo.example.com"));
    payload.setMatchedSites(List.of("demo.example.com"));

    BtResult<SslDeployableSites> response = new BtResult<>();
    response.setStatus(true);
    response.setData(payload);
    return response;
  }

  private static BtResult<SslBatchDeploymentResult> successDeploymentResponse() {
    SslBatchDeploymentResult payload = new SslBatchDeploymentResult();
    payload.setTotal(1);
    payload.setSuccessCount(1);
    payload.setFailedCount(0);

    BtResult<SslBatchDeploymentResult> response = new BtResult<>();
    response.setStatus(true);
    response.setData(payload);
    return response;
  }
}
