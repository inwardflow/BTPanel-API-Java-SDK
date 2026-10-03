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

import net.heimeng.sdk.btapi.api.website.GetPhpVersionsApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteBackupsApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteConfigApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteDetailApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteDomainsApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteLimitNetApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteListApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteNginxConfigApi;
import net.heimeng.sdk.btapi.api.website.GetWebsitePhpExtensionsApi;
import net.heimeng.sdk.btapi.api.website.GetWebsitePhpVersionApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteRewriteRulesApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteRootPathApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteSslListApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteTypesApi;
import net.heimeng.sdk.btapi.api.website.GetWebsitesApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.PhpVersion;
import net.heimeng.sdk.btapi.model.website.WebsiteInfo;
import net.heimeng.sdk.btapi.model.website.WebsiteType;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebsiteOperations read facade tests")
class WebsiteOperationsTest {

  @Mock private BtClient client;

  @Test
  @DisplayName("list(page, limit) should delegate to GetWebsitesApi")
  void listWithPaginationDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsitesApi.class))).thenReturn(successWebsiteList());

    BtResult<List<WebsiteInfo>> result = operations.list(1, 20);

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetWebsitesApi.class));
  }

  @Test
  @DisplayName("listRaw should delegate to GetWebsiteListApi")
  void listRawDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteListApi.class))).thenReturn(successRawWebsiteList());

    BtResult<List<Map<String, Object>>> result = operations.listRaw(1, 20, -1, "id desc", "demo");

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetWebsiteListApi.class));
  }

  @Test
  @DisplayName("getDetail should delegate to GetWebsiteDetailApi")
  void getDetailDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteDetailApi.class))).thenReturn(successWebsiteDetail());

    BtResult<Map<String, Object>> result = operations.getDetail(8);

    assertTrue(result.isSuccess());
    assertEquals("demo.example.com", result.getData().get("name"));
    verify(client).execute(any(GetWebsiteDetailApi.class));
  }

  @Test
  @DisplayName("getConfig should delegate to GetWebsiteConfigApi")
  void getConfigDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteConfigApi.class))).thenReturn(successWebsiteConfig());

    BtResult<Map<String, Object>> result = operations.getConfig(8, "/www/wwwroot/demo");

    assertTrue(result.isSuccess());
    assertEquals(true, result.getData().get("logs"));
    verify(client).execute(any(GetWebsiteConfigApi.class));
  }

  @Test
  @DisplayName("listDomains should delegate to GetWebsiteDomainsApi")
  void listDomainsDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteDomainsApi.class))).thenReturn(successDomains());

    BtResult<List<Map<String, Object>>> result = operations.listDomains(66);

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetWebsiteDomainsApi.class));
  }

  @Test
  @DisplayName("listTypes should delegate to GetWebsiteTypesApi")
  void listTypesDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteTypesApi.class))).thenReturn(successWebsiteTypes());

    BtResult<List<WebsiteType>> result = operations.listTypes();

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetWebsiteTypesApi.class));
  }

  @Test
  @DisplayName("listPhpVersions should delegate to GetPhpVersionsApi")
  void listPhpVersionsDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetPhpVersionsApi.class))).thenReturn(successPhpVersions());

    BtResult<List<PhpVersion>> result = operations.listPhpVersions();

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetPhpVersionsApi.class));
  }

  @Test
  @DisplayName("getRootPath should delegate to GetWebsiteRootPathApi")
  void getRootPathDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteRootPathApi.class))).thenReturn(successString("/www/demo"));

    BtResult<String> result = operations.getRootPath(8);

    assertTrue(result.isSuccess());
    assertEquals("/www/demo", result.getData());
    verify(client).execute(any(GetWebsiteRootPathApi.class));
  }

  @Test
  @DisplayName("getPhpVersion should delegate to GetWebsitePhpVersionApi")
  void getPhpVersionDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsitePhpVersionApi.class))).thenReturn(successString("82"));

    BtResult<String> result = operations.getPhpVersion(8);

    assertTrue(result.isSuccess());
    assertEquals("82", result.getData());
    verify(client).execute(any(GetWebsitePhpVersionApi.class));
  }

  @Test
  @DisplayName("listPhpExtensions should delegate to GetWebsitePhpExtensionsApi")
  void listPhpExtensionsDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsitePhpExtensionsApi.class))).thenReturn(successExtensions());

    BtResult<List<Map<String, Object>>> result = operations.listPhpExtensions(8);

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetWebsitePhpExtensionsApi.class));
  }

  @Test
  @DisplayName("getRewriteRules should delegate to GetWebsiteRewriteRulesApi")
  void getRewriteRulesDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteRewriteRulesApi.class)))
        .thenReturn(successString("rewrite ^/(.*)$ /index.php;"));

    BtResult<String> result = operations.getRewriteRules(8);

    assertTrue(result.isSuccess());
    assertEquals("rewrite ^/(.*)$ /index.php;", result.getData());
    verify(client).execute(any(GetWebsiteRewriteRulesApi.class));
  }

  @Test
  @DisplayName("getNginxConfig should delegate to GetWebsiteNginxConfigApi")
  void getNginxConfigDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteNginxConfigApi.class)))
        .thenReturn(successString("server { listen 80; }"));

    BtResult<String> result = operations.getNginxConfig(8, "demo.example.com");

    assertTrue(result.isSuccess());
    assertEquals("server { listen 80; }", result.getData());
    verify(client).execute(any(GetWebsiteNginxConfigApi.class));
  }

  @Test
  @DisplayName("listSslCertificates should delegate to GetWebsiteSslListApi")
  void listSslCertificatesDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteSslListApi.class))).thenReturn(successSslCertificates());

    BtResult<List<Map<String, Object>>> result = operations.listSslCertificates(8);

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetWebsiteSslListApi.class));
  }

  @Test
  @DisplayName("getLimitNet should delegate to GetWebsiteLimitNetApi")
  void getLimitNetDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteLimitNetApi.class))).thenReturn(successLimitNet());

    BtResult<Map<String, Object>> result = operations.getLimitNet(8);

    assertTrue(result.isSuccess());
    assertEquals(300, result.getData().get("perserver"));
    verify(client).execute(any(GetWebsiteLimitNetApi.class));
  }

  @Test
  @DisplayName("listBackups should delegate to GetWebsiteBackupsApi")
  void listBackupsDelegatesToClient() {
    WebsiteOperations operations = new WebsiteOperations(client);
    when(client.execute(any(GetWebsiteBackupsApi.class))).thenReturn(successBackups());

    BtResult<List<Map<String, Object>>> result = operations.listBackups(8, 1, 5, "load_backups");

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetWebsiteBackupsApi.class));
  }

  private static BtResult<List<WebsiteInfo>> successWebsiteList() {
    WebsiteInfo websiteInfo = new WebsiteInfo();
    websiteInfo.setName("demo.example.com");

    BtResult<List<WebsiteInfo>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(websiteInfo));
    return response;
  }

  private static BtResult<List<Map<String, Object>>> successRawWebsiteList() {
    BtResult<List<Map<String, Object>>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(Map.of("name", "demo.example.com")));
    return response;
  }

  private static BtResult<Map<String, Object>> successWebsiteDetail() {
    BtResult<Map<String, Object>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(Map.of("name", "demo.example.com"));
    return response;
  }

  private static BtResult<Map<String, Object>> successWebsiteConfig() {
    BtResult<Map<String, Object>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(Map.of("logs", true));
    return response;
  }

  private static BtResult<List<Map<String, Object>>> successDomains() {
    BtResult<List<Map<String, Object>>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(Map.of("name", "demo.example.com")));
    return response;
  }

  private static BtResult<List<WebsiteType>> successWebsiteTypes() {
    WebsiteType websiteType = new WebsiteType();
    websiteType.setId(0);
    websiteType.setName("Default");

    BtResult<List<WebsiteType>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(websiteType));
    return response;
  }

  private static BtResult<List<PhpVersion>> successPhpVersions() {
    PhpVersion phpVersion = new PhpVersion();
    phpVersion.setVersion("82");
    phpVersion.setName("PHP-82");

    BtResult<List<PhpVersion>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(phpVersion));
    return response;
  }

  private static BtResult<List<Map<String, Object>>> successExtensions() {
    BtResult<List<Map<String, Object>>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(Map.of("name", "redis")));
    return response;
  }

  private static BtResult<String> successString(String data) {
    BtResult<String> response = new BtResult<>();
    response.setStatus(true);
    response.setData(data);
    return response;
  }

  private static BtResult<List<Map<String, Object>>> successSslCertificates() {
    BtResult<List<Map<String, Object>>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(Map.of("subject", "demo.example.com")));
    return response;
  }

  private static BtResult<Map<String, Object>> successLimitNet() {
    BtResult<Map<String, Object>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(Map.of("perserver", 300));
    return response;
  }

  private static BtResult<List<Map<String, Object>>> successBackups() {
    BtResult<List<Map<String, Object>>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(Map.of("name", "backup.tar.gz")));
    return response;
  }
}
