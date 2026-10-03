package net.heimeng.sdk.btapi.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heimeng.sdk.btapi.api.website.CreateWebsiteApi;
import net.heimeng.sdk.btapi.api.website.DeleteWebsiteApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteListApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslBatchDeploymentResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;
import net.heimeng.sdk.btapi.model.ssl.SslDeployableSites;
import net.heimeng.sdk.btapi.model.ssl.SslSiteStatus;
import net.heimeng.sdk.btapi.model.website.CreateWebsiteResult;
import net.heimeng.sdk.btapi.testutil.TestCertificates;

@DisplayName("SSL integration tests")
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
@Timeout(value = 90, unit = TimeUnit.SECONDS)
class SslIntegrationTest extends AbstractIntegrationTestSupport {

  private static final Logger logger = LoggerFactory.getLogger(SslIntegrationTest.class);

  private BtApiManager apiManager;
  private String testDomain;
  private String testWebroot;
  private boolean certificateInstalled;

  @BeforeEach
  void setUp() {
    assumeConfigurationPresent(ENV_BASE_URL, "baseUrl");
    assumeConfigurationPresent(ENV_API_KEY, "apiKey");

    apiManager = createApiManager();
    assumePanelApiAccessible(apiManager);
  }

  @AfterEach
  void tearDown() {
    try {
      deleteSavedCertificatesQuietly();
      deleteWebsiteIfExists();
    } finally {
      closeQuietly(apiManager);
    }
  }

  @Test
  @DisplayName("Should query saved SSL certificates")
  void testGetSslCertificates() throws BtApiException {
    BtResult<List<SslCertificate>> result = apiManager.ssl().list();

    assertTrue(result.isSuccess(), "Failed to get SSL certificates: " + result.getMsg());
    assertNotNull(result.getData(), "SSL certificate list should not be null");
  }

  @Test
  @DisplayName("Should query website SSL status and order list before deployment")
  void testGetWebsiteSslStatusAndOrderList() throws BtApiException {
    ensureWebsiteExists();

    BtResult<SslSiteStatus> statusResult = apiManager.ssl().getWebsiteStatus(testDomain);
    BtResult<List<Map<String, Object>>> orderResult = apiManager.ssl().listOrders(testDomain);

    assertTrue(statusResult.isSuccess(), "Failed to get website SSL status");
    assertNotNull(statusResult.getData(), "Website SSL status payload should not be null");
    assertFalse(statusResult.getData().isEnabled(), "Fresh test website should start without SSL");
    assertNotNull(orderResult.getData(), "SSL order list should not be null");
  }

  @Test
  @DisplayName("Should deploy a saved SSL certificate to a site")
  void testDeploySavedCertificateToSite() throws BtApiException {
    ensureWebsiteExists();

    // A throwaway certificate keeps the test independent of whatever is in the panel's store.
    TestCertificates.SelfSigned certificate =
        TestCertificates.generate(testDomain, Duration.ofDays(2), false);

    // 1. SetSSL installs the certificate and saves it into the panel certificate store.
    BtResult<Boolean> installResult =
        apiManager
            .ssl()
            .install(testDomain, certificate.privateKeyPem(), certificate.certificatePem());
    assertTrue(installResult.isSuccess(), "Failed to install test certificate");
    certificateInstalled = true;

    SslCertificate savedCertificate = findSavedCertificate(testDomain);
    assertNotNull(savedCertificate, "Installed certificate should be saved in the panel store");
    assertNotNull(savedCertificate.getHash(), "Saved certificate should expose its hash");

    // 2. Turn SSL off so that the deployment below is what turns it back on.
    BtResult<Boolean> disableResult = apiManager.website().disableSsl(testDomain);
    assertTrue(disableResult.isSuccess(), "Failed to disable SSL: " + disableResult.getMsg());
    assertFalse(
        apiManager.ssl().getWebsiteStatus(testDomain).getData().isEnabled(),
        "SSL should be disabled before deploying the saved certificate");

    // 3. Deploy the saved certificate from the store.
    BtResult<SslDeployableSites> deployableSites =
        apiManager.ssl().getDeployableSites(List.of(savedCertificate.getName()));
    assertTrue(
        deployableSites.isSuccess(),
        "Failed to resolve deployable sites: " + deployableSites.getMsg());
    assertTrue(
        deployableSites.getData().getAllSites().contains(testDomain),
        "Deployable site list should include the temporary test site");

    BtResult<SslBatchDeploymentResult> deployResult =
        apiManager
            .ssl()
            .deploySavedCertificate(
                savedCertificate.getHash(), testDomain, savedCertificate.getName());

    assertTrue(deployResult.isSuccess(), "Failed to deploy saved SSL certificate");
    assertNotNull(deployResult.getData(), "Deployment result payload should not be null");
    assertTrue(
        deployResult.getData().isFullySuccessful(),
        "Saved certificate deployment should succeed for the temporary site");

    SslSiteStatus status = waitForWebsiteSslEnabled(5, 1000L);
    assertNotNull(status, "Website SSL status should become enabled after deployment");
    assertTrue(status.isEnabled(), "Website SSL should be enabled after deployment");
    assertNotNull(
        status.getCertificateDetail(), "Deployed website should expose certificate detail");
  }

  private void ensureWebsiteExists() throws BtApiException {
    prepareWebsiteFixture();
    if (getWebsiteIdByName(testDomain) != null) {
      return;
    }

    try {
      BtResult<CreateWebsiteResult> result =
          apiManager.execute(
              new CreateWebsiteApi(
                      testDomain, testWebroot, 0, "81", 80, "SSL integration test website")
                  .setType("PHP")
                  .setCreateFtp(false)
                  .setCreateDatabase(false));

      assertTrue(result.isSuccess(), "Failed to prepare SSL test website: " + result.getMsg());
      assertNotNull(result.getData(), "Website creation result should not be null");
      assertTrue(result.getData().isSiteStatus(), "Website creation should report success");
    } catch (BtApiException exception) {
      if (isAlreadyExists(exception) && getWebsiteIdByName(testDomain) != null) {
        return;
      }
      throw exception;
    }
  }

  private void prepareWebsiteFixture() {
    if (testDomain != null && testWebroot != null) {
      return;
    }

    assumeConfigurationPresent(ENV_TEST_DOMAIN_SUFFIX, "test.domain");
    assumeConfigurationPresent(ENV_TEST_WEBROOT_BASE, "test.webroot");

    String suffix = uniqueSuffix();
    String domainSuffix = getRequiredConfiguration(ENV_TEST_DOMAIN_SUFFIX, "test.domain");
    String webrootBase = getRequiredRemotePath(ENV_TEST_WEBROOT_BASE, "test.webroot");

    testDomain = buildIsolatedTestDomain(domainSuffix, "ssl-" + suffix);
    testWebroot = buildIsolatedTestWebroot(webrootBase, domainSuffix, testDomain);

    logger.info(
        "SSL integration test initialized, testDomain={}, testWebroot={}", testDomain, testWebroot);
  }

  private SslCertificate findSavedCertificate(String domain) throws BtApiException {
    BtResult<List<SslCertificate>> result = apiManager.ssl().list();
    if (!result.isSuccess() || result.getData() == null) {
      return null;
    }
    return result.getData().stream()
        .filter(certificate -> isCertificateFor(certificate, domain))
        .findFirst()
        .orElse(null);
  }

  private static boolean isCertificateFor(SslCertificate certificate, String domain) {
    if (certificate == null) {
      return false;
    }
    List<String> domains = certificate.getDomains();
    return domain.equalsIgnoreCase(certificate.getName())
        || (domains != null && domains.contains(domain));
  }

  /** 删除本测试安装进证书夹的证书。只删除域名与临时站点一致的证书，不影响面板上的其他证书。 */
  private void deleteSavedCertificatesQuietly() {
    if (!certificateInstalled || apiManager == null || testDomain == null) {
      return;
    }
    try {
      BtResult<List<SslCertificate>> result = apiManager.ssl().list();
      if (!result.isSuccess() || result.getData() == null) {
        return;
      }
      for (SslCertificate certificate : result.getData()) {
        if (isCertificateFor(certificate, testDomain)) {
          apiManager.ssl().delete(certificate.getId());
        }
      }
    } catch (Exception exception) {
      logger.warn(
          "Certificate cleanup failed, testDomain={}, reason={}",
          testDomain,
          exception.getMessage());
    }
  }

  private SslSiteStatus waitForWebsiteSslEnabled(int attempts, long intervalMillis)
      throws BtApiException {
    for (int attempt = 0; attempt < attempts; attempt++) {
      BtResult<SslSiteStatus> result = apiManager.ssl().getWebsiteStatus(testDomain);
      if (result.isSuccess() && result.getData() != null && result.getData().isEnabled()) {
        return result.getData();
      }

      if (attempt + 1 < attempts) {
        sleepQuietly(intervalMillis);
      }
    }
    return null;
  }

  private void deleteWebsiteIfExists() {
    if (apiManager == null || testDomain == null || testDomain.isBlank()) {
      return;
    }

    try {
      Integer websiteId = getWebsiteIdByName(testDomain);
      if (websiteId == null) {
        return;
      }

      BtResult<Boolean> result =
          apiManager.execute(
              new DeleteWebsiteApi(websiteId, testDomain)
                  .setDeletePath(true)
                  .setDeleteDatabase(false)
                  .setDeleteFtp(false));

      if (!result.isSuccess()) {
        logger.warn(
            "Website cleanup failed, testDomain={}, reason={}", testDomain, result.getMsg());
      }
    } catch (Exception exception) {
      logger.warn(
          "Website cleanup raised an exception, testDomain={}, reason={}",
          testDomain,
          exception.getMessage());
    }
  }

  private Integer getWebsiteIdByName(String domain) throws BtApiException {
    BtResult<List<Map<String, Object>>> result =
        apiManager.execute(new GetWebsiteListApi().setPage(1).setLimit(100));

    if (!result.isSuccess() || result.getData() == null) {
      return null;
    }

    for (Map<String, Object> website : result.getData()) {
      if (domain.equals(website.get("name"))) {
        return toInteger(website.get("id"));
      }
    }
    return null;
  }

  private Integer toInteger(Object value) {
    if (value instanceof Number numberValue) {
      return numberValue.intValue();
    }
    if (value instanceof String stringValue && !stringValue.isBlank()) {
      return Integer.parseInt(stringValue);
    }
    return null;
  }

  private void sleepQuietly(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(
          "Interrupted while waiting for SSL status propagation", exception);
    }
  }
}
