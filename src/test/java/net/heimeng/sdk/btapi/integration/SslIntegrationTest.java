package net.heimeng.sdk.btapi.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
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

@DisplayName("SSL integration tests")
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
@Timeout(value = 90, unit = TimeUnit.SECONDS)
class SslIntegrationTest extends AbstractIntegrationTestSupport {

  private static final Logger logger = LoggerFactory.getLogger(SslIntegrationTest.class);

  private BtApiManager apiManager;
  private String testDomain;
  private String testWebroot;

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

    CertificateFixture certificateFixture = loadCertificateFixture();
    SslCertificate savedCertificate = findSavedFixtureCertificate(certificateFixture);
    Assumptions.assumeTrue(
        savedCertificate != null,
        "Skipping SSL deployment integration test because the expected saved certificate is not present in the panel");
    Assumptions.assumeTrue(
        savedCertificate.getHash() != null && !savedCertificate.getHash().isBlank(),
        "Skipping SSL deployment integration test because the saved certificate hash is missing");
    Assumptions.assumeTrue(
        savedCertificate.getName() != null && !savedCertificate.getName().isBlank(),
        "Skipping SSL deployment integration test because the saved certificate name is missing");

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
    String webrootBase = getRequiredConfiguration(ENV_TEST_WEBROOT_BASE, "test.webroot");

    testDomain = buildIsolatedTestDomain(domainSuffix, "ssl-" + suffix);
    testWebroot = buildIsolatedTestWebroot(webrootBase, domainSuffix, testDomain);

    logger.info(
        "SSL integration test initialized, testDomain={}, testWebroot={}", testDomain, testWebroot);
  }

  private SslCertificate findSavedFixtureCertificate(CertificateFixture certificateFixture)
      throws BtApiException {
    BtResult<List<SslCertificate>> result = apiManager.ssl().list();
    if (!result.isSuccess() || result.getData() == null) {
      return null;
    }

    return result.getData().stream()
        .filter((certificate) -> matchesCertificateFixture(certificate, certificateFixture))
        .findFirst()
        .orElse(null);
  }

  private boolean matchesCertificateFixture(
      SslCertificate certificate, CertificateFixture certificateFixture) {
    if (certificate == null) {
      return false;
    }
    List<String> domains = certificate.getDomains();
    if (domains != null && !domains.isEmpty()) {
      return domains.containsAll(certificateFixture.domains());
    }
    return certificateFixture.commonName().equalsIgnoreCase(certificate.getName());
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

  private CertificateFixture loadCertificateFixture() {
    String commonName =
        normalizeValue(
            getOptionalConfiguration(
                ENV_TEST_SSL_CERT_COMMON_NAME, "test.ssl.certificate.common-name"));
    String configuredDomains =
        normalizeValue(
            getOptionalConfiguration(ENV_TEST_SSL_CERT_DOMAINS, "test.ssl.certificate.domains"));

    Assumptions.assumeTrue(
        commonName != null || configuredDomains != null,
        () ->
            "Skipping SSL deployment integration test because missing certificate metadata configuration: "
                + ENV_TEST_SSL_CERT_COMMON_NAME
                + " / "
                + ENV_TEST_SSL_CERT_DOMAINS
                + " or "
                + PROPERTIES_FILE
                + " -> test.ssl.certificate.common-name / test.ssl.certificate.domains");

    List<String> domains =
        configuredDomains == null
            ? List.of()
            : Arrays.stream(configuredDomains.split(","))
                .map(String::trim)
                .filter((domain) -> !domain.isBlank())
                .distinct()
                .toList();

    if (commonName == null && !domains.isEmpty()) {
      commonName = domains.get(0);
    }

    if (commonName != null && !domains.contains(commonName)) {
      domains = Stream.concat(Stream.of(commonName), domains.stream()).distinct().toList();
    }

    return new CertificateFixture(commonName == null ? "" : commonName, List.copyOf(domains));
  }

  private String normalizeValue(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private record CertificateFixture(String commonName, List<String> domains) {}
}
