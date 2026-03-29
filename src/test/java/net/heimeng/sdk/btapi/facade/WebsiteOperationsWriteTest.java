package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.heimeng.sdk.btapi.api.website.AddWebsiteDomainApi;
import net.heimeng.sdk.btapi.api.website.CloseWebsitePasswordApi;
import net.heimeng.sdk.btapi.api.website.CloseWebsiteSslApi;
import net.heimeng.sdk.btapi.api.website.CreateWebsiteBackupApi;
import net.heimeng.sdk.btapi.api.website.DeleteWebsiteApi;
import net.heimeng.sdk.btapi.api.website.DeleteWebsiteBackupApi;
import net.heimeng.sdk.btapi.api.website.DeleteWebsiteDomainApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteLimitNetApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteLogsApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteNginxConfigApi;
import net.heimeng.sdk.btapi.api.website.SetWebsitePasswordApi;
import net.heimeng.sdk.btapi.api.website.SetWebsitePhpExtensionsApi;
import net.heimeng.sdk.btapi.api.website.SetWebsitePhpVersionApi;
import net.heimeng.sdk.btapi.api.website.SetWebsitePsApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteRewriteRulesApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteRootPathApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteRunPathApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteSslApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteUserIniApi;
import net.heimeng.sdk.btapi.api.website.StartWebsiteApi;
import net.heimeng.sdk.btapi.api.website.StopWebsiteApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebsiteOperations write facade tests")
class WebsiteOperationsWriteTest {

  @Mock private BtClient client;

  @Test
  @DisplayName("addDomain should delegate to AddWebsiteDomainApi")
  void addDomainDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(AddWebsiteDomainApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.addDomain(8, "demo.example.com", "www.demo.example.com");

    assertTrue(result.isSuccess());
    verify(client).execute(any(AddWebsiteDomainApi.class));
  }

  @Test
  @DisplayName("removeDomain should delegate to DeleteWebsiteDomainApi")
  void removeDomainDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(DeleteWebsiteDomainApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.removeDomain(8, "demo.example.com", "www.demo.example.com", 80);

    assertTrue(result.isSuccess());
    verify(client).execute(any(DeleteWebsiteDomainApi.class));
  }

  @Test
  @DisplayName("delete should delegate to DeleteWebsiteApi")
  void deleteDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(DeleteWebsiteApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.delete(8, "demo.example.com", new WebsiteDeleteOptions(true, true, false));

    assertTrue(result.isSuccess());
    verify(client).execute(any(DeleteWebsiteApi.class));
  }

  @Test
  @DisplayName("start should delegate to StartWebsiteApi")
  void startDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(StartWebsiteApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.start(8);

    assertTrue(result.isSuccess());
    verify(client).execute(any(StartWebsiteApi.class));
  }

  @Test
  @DisplayName("stop should delegate to StopWebsiteApi")
  void stopDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(StopWebsiteApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.stop(8);

    assertTrue(result.isSuccess());
    verify(client).execute(any(StopWebsiteApi.class));
  }

  @Test
  @DisplayName("updateRemark should delegate to SetWebsitePsApi")
  void updateRemarkDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsitePsApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.updateRemark(8, "production");

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsitePsApi.class));
  }

  @Test
  @DisplayName("updateRootPath should delegate to SetWebsiteRootPathApi")
  void updateRootPathDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsiteRootPathApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.updateRootPath(8, "/www/wwwroot/demo");

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsiteRootPathApi.class));
  }

  @Test
  @DisplayName("updateRunPath should delegate to SetWebsiteRunPathApi")
  void updateRunPathDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsiteRunPathApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.updateRunPath(8, "public");

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsiteRunPathApi.class));
  }

  @Test
  @DisplayName("toggleUserIni should delegate to SetWebsiteUserIniApi")
  void toggleUserIniDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsiteUserIniApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.toggleUserIni("/www/wwwroot/demo");

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsiteUserIniApi.class));
  }

  @Test
  @DisplayName("updatePhpVersion should delegate to SetWebsitePhpVersionApi")
  void updatePhpVersionDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsitePhpVersionApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.updatePhpVersion(8, "82");

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsitePhpVersionApi.class));
  }

  @Test
  @DisplayName("updatePhpExtension should delegate to SetWebsitePhpExtensionsApi")
  void updatePhpExtensionDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsitePhpExtensionsApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.updatePhpExtension(8, "redis", true);

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsitePhpExtensionsApi.class));
  }

  @Test
  @DisplayName("updateRewriteRules should delegate to SetWebsiteRewriteRulesApi")
  void updateRewriteRulesDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsiteRewriteRulesApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.updateRewriteRules(8, "none", "rewrite ^ /index.php;");

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsiteRewriteRulesApi.class));
  }

  @Test
  @DisplayName("updateNginxConfig should delegate to SetWebsiteNginxConfigApi")
  void updateNginxConfigDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsiteNginxConfigApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.updateNginxConfig(8, "demo.example.com", "server { listen 80; }");

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsiteNginxConfigApi.class));
  }

  @Test
  @DisplayName("enablePasswordProtection should delegate to SetWebsitePasswordApi")
  void enablePasswordProtectionDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsitePasswordApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.enablePasswordProtection(8, "admin", "secret");

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsitePasswordApi.class));
  }

  @Test
  @DisplayName("disablePasswordProtection should delegate to CloseWebsitePasswordApi")
  void disablePasswordProtectionDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(CloseWebsitePasswordApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.disablePasswordProtection(8);

    assertTrue(result.isSuccess());
    verify(client).execute(any(CloseWebsitePasswordApi.class));
  }

  @Test
  @DisplayName("installSslCertificate should delegate to SetWebsiteSslApi")
  void installSslCertificateDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsiteSslApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.installSslCertificate(
            8, new WebsiteSslCertificateOptions("demo.example.com", "cert", "key", Boolean.TRUE));

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsiteSslApi.class));
  }

  @Test
  @DisplayName("disableSsl should delegate to CloseWebsiteSslApi")
  void disableSslDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(CloseWebsiteSslApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.disableSsl(8);

    assertTrue(result.isSuccess());
    verify(client).execute(any(CloseWebsiteSslApi.class));
  }

  @Test
  @DisplayName("toggleLogs should delegate to SetWebsiteLogsApi")
  void toggleLogsDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsiteLogsApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.toggleLogs(8);

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsiteLogsApi.class));
  }

  @Test
  @DisplayName("updateLimitNet should delegate to SetWebsiteLimitNetApi")
  void updateLimitNetDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(SetWebsiteLimitNetApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.updateLimitNet(8, new WebsiteLimitNetOptions(true, 300, 30, 1024));

    assertTrue(result.isSuccess());
    verify(client).execute(any(SetWebsiteLimitNetApi.class));
  }

  @Test
  @DisplayName("createBackup should delegate to CreateWebsiteBackupApi")
  void createBackupDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(CreateWebsiteBackupApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.createBackup(8);

    assertTrue(result.isSuccess());
    verify(client).execute(any(CreateWebsiteBackupApi.class));
  }

  @Test
  @DisplayName("deleteBackup should delegate to DeleteWebsiteBackupApi")
  void deleteBackupDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(DeleteWebsiteBackupApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.deleteBackup(18);

    assertTrue(result.isSuccess());
    verify(client).execute(any(DeleteWebsiteBackupApi.class));
  }

  private static BtResult<Boolean> successBoolean() {
    BtResult<Boolean> response = new BtResult<>();
    response.setStatus(true);
    response.setData(true);
    return response;
  }
}
