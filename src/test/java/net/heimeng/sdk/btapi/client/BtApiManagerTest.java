package net.heimeng.sdk.btapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.heimeng.sdk.btapi.api.file.GetFileContentApi;
import net.heimeng.sdk.btapi.api.ftp.GetFtpAccountsApi;
import net.heimeng.sdk.btapi.api.ssl.GetSslCertificatesApi;
import net.heimeng.sdk.btapi.api.system.CheckPanelUpdateApi;
import net.heimeng.sdk.btapi.api.system.GetDiskInfoApi;
import net.heimeng.sdk.btapi.api.system.GetNetworkStatusApi;
import net.heimeng.sdk.btapi.api.system.GetSystemInfoApi;
import net.heimeng.sdk.btapi.api.system.GetTaskCountApi;
import net.heimeng.sdk.btapi.api.website.GetWebsitesApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ftp.FtpAccount;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;
import net.heimeng.sdk.btapi.model.system.DiskInfo;
import net.heimeng.sdk.btapi.model.system.NetworkStatus;
import net.heimeng.sdk.btapi.model.system.PanelUpdateInfo;
import net.heimeng.sdk.btapi.model.system.SystemInfo;
import net.heimeng.sdk.btapi.model.website.WebsiteInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("BtApiManager tests")
class BtApiManagerTest {

  @Mock private BtClient client;

  @Test
  @DisplayName("Generic synchronous execution delegates to the client")
  void executeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);
    GetSystemInfoApi api = new GetSystemInfoApi();
    BtResult<SystemInfo> response = new BtResult<>();
    response.setStatus(true);
    response.setData(new SystemInfo());

    when(client.execute(api)).thenReturn(response);

    BtResult<SystemInfo> result = apiManager.execute(api);

    assertEquals(response, result);
    verify(client).execute(api);
  }

  @Test
  @DisplayName("Generic asynchronous execution delegates to the client")
  void executeAsyncDelegatesToClient() throws Exception {
    BtApiManager apiManager = new BtApiManager(client);
    GetSystemInfoApi api = new GetSystemInfoApi();
    BtResult<SystemInfo> response = new BtResult<>();
    response.setStatus(true);

    when(client.executeAsync(api)).thenReturn(CompletableFuture.completedFuture(response));

    BtResult<SystemInfo> result = apiManager.executeAsync(api).get();

    assertEquals(response, result);
    verify(client).executeAsync(api);
  }

  @Test
  @DisplayName("Timeout helper unwraps BtApiException")
  void executeAsyncWithTimeoutUnwrapsBtApiException() {
    BtApiManager apiManager = new BtApiManager(client);
    GetSystemInfoApi api = new GetSystemInfoApi();
    CompletableFuture<BtResult<SystemInfo>> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(new BtApiException("network failure"));

    when(client.executeAsync(api)).thenReturn(failedFuture);

    BtApiException exception =
        assertThrows(BtApiException.class, () -> apiManager.executeAsyncWithTimeout(api));

    assertEquals("network failure", exception.getMessage());
  }

  @Test
  @DisplayName("System facade delegates to the correct API")
  void systemFacadeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);
    BtResult<SystemInfo> response = new BtResult<>();
    response.setStatus(true);
    response.setData(new SystemInfo());

    when(client.execute(any(GetSystemInfoApi.class))).thenReturn(response);

    BtResult<SystemInfo> result = apiManager.system().getSystemInfo();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    verify(client).execute(any(GetSystemInfoApi.class));
  }

  @Test
  @DisplayName("System task count facade delegates to the correct API")
  void systemTaskCountFacadeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);
    BtResult<Integer> response = new BtResult<>();
    response.setStatus(true);
    response.setData(2);

    when(client.execute(any(GetTaskCountApi.class))).thenReturn(response);

    BtResult<Integer> result = apiManager.system().getTaskCount();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(2, result.getData());
    verify(client).execute(any(GetTaskCountApi.class));
  }

  @Test
  @DisplayName("System network facade delegates to the correct API")
  void systemNetworkFacadeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);
    BtResult<NetworkStatus> response = new BtResult<>();
    response.setStatus(true);
    response.setData(new NetworkStatus());

    when(client.execute(any(GetNetworkStatusApi.class))).thenReturn(response);

    BtResult<NetworkStatus> result = apiManager.system().getNetworkStatus();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    verify(client).execute(any(GetNetworkStatusApi.class));
  }

  @Test
  @DisplayName("System disk facade delegates to the correct API")
  void systemDiskFacadeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);
    BtResult<java.util.List<DiskInfo>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(java.util.List.of(new DiskInfo()));

    when(client.execute(any(GetDiskInfoApi.class))).thenReturn(response);

    BtResult<java.util.List<DiskInfo>> result = apiManager.system().getDiskInfo();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    verify(client).execute(any(GetDiskInfoApi.class));
  }

  @Test
  @DisplayName("System panel update facade delegates to the correct API")
  void systemPanelUpdateFacadeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);
    BtResult<PanelUpdateInfo> response = new BtResult<>();
    response.setStatus(true);
    response.setData(new PanelUpdateInfo());

    when(client.execute(any(CheckPanelUpdateApi.class))).thenReturn(response);

    BtResult<PanelUpdateInfo> result = apiManager.system().checkPanelUpdate();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    verify(client).execute(any(CheckPanelUpdateApi.class));
  }

  @Test
  @DisplayName("Website facade delegates to the correct API")
  void websiteFacadeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);
    BtResult<java.util.List<WebsiteInfo>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(java.util.List.of(new WebsiteInfo()));

    when(client.execute(any(GetWebsitesApi.class))).thenReturn(response);

    BtResult<java.util.List<WebsiteInfo>> result = apiManager.website().list();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    verify(client).execute(any(GetWebsitesApi.class));
  }

  @Test
  @DisplayName("File facade delegates to the correct API")
  void fileFacadeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);
    BtResult<String> response = new BtResult<>();
    response.setStatus(true);
    response.setData("server { listen 80; }");

    when(client.execute(any(GetFileContentApi.class))).thenReturn(response);

    BtResult<String> result = apiManager.file().getContent("/www/server.conf");

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals("server { listen 80; }", result.getData());
    verify(client).execute(any(GetFileContentApi.class));
  }

  @Test
  @DisplayName("FTP facade delegates to the correct API")
  void ftpFacadeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);
    BtResult<java.util.List<FtpAccount>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(java.util.List.of(new FtpAccount()));

    when(client.execute(any(GetFtpAccountsApi.class))).thenReturn(response);

    BtResult<java.util.List<FtpAccount>> result = apiManager.ftp().list();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetFtpAccountsApi.class));
  }

  @Test
  @DisplayName("SSL facade delegates to the correct API")
  void sslFacadeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);
    BtResult<java.util.List<SslCertificate>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(java.util.List.of(new SslCertificate()));

    when(client.execute(any(GetSslCertificatesApi.class))).thenReturn(response);

    BtResult<java.util.List<SslCertificate>> result = apiManager.ssl().list();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetSslCertificatesApi.class));
  }

  @Test
  @DisplayName("Close delegates to the underlying client")
  void closeDelegatesToClient() {
    BtApiManager apiManager = new BtApiManager(client);

    apiManager.close();

    verify(client).close();
  }

  @Test
  @DisplayName("Async execution surfaces completion exceptions")
  void executeAsyncSurfaceFailure() {
    BtApiManager apiManager = new BtApiManager(client);
    GetSystemInfoApi api = new GetSystemInfoApi();
    CompletableFuture<BtResult<SystemInfo>> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(new IllegalStateException("boom"));

    when(client.executeAsync(api)).thenReturn(failedFuture);

    ExecutionException exception =
        assertThrows(ExecutionException.class, () -> apiManager.executeAsync(api).get());

    assertEquals("boom", exception.getCause().getMessage());
  }
}
