package net.heimeng.sdk.btapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

import net.heimeng.sdk.btapi.api.website.DeleteWebsiteApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteListApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.facade.WebsiteCreateRequest;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.CreateWebsiteResult;
import net.heimeng.sdk.btapi.model.website.PhpVersion;

@DisplayName("Website integration tests")
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
@Timeout(value = 90, unit = TimeUnit.SECONDS)
class WebsiteIntegrationTest extends AbstractIntegrationTestSupport {

  private static final Logger logger = LoggerFactory.getLogger(WebsiteIntegrationTest.class);

  private BtApiManager apiManager;
  private String testDomain;
  private String testWebroot;

  @BeforeEach
  void setUp() {
    assumeConfigurationPresent(ENV_BASE_URL, "baseUrl");
    assumeConfigurationPresent(ENV_API_KEY, "apiKey");
    assumeConfigurationPresent(ENV_TEST_DOMAIN_SUFFIX, "test.domain");
    assumeConfigurationPresent(ENV_TEST_WEBROOT_BASE, "test.webroot");

    apiManager = createApiManager();
    assumePanelApiAccessible(apiManager);

    String domainSuffix = getRequiredConfiguration(ENV_TEST_DOMAIN_SUFFIX, "test.domain");
    String webrootBase = getRequiredRemotePath(ENV_TEST_WEBROOT_BASE, "test.webroot");
    String uniqueDomainPrefix = uniqueSuffix();

    testDomain = buildIsolatedTestDomain(domainSuffix, uniqueDomainPrefix);
    testWebroot = buildIsolatedTestWebroot(webrootBase, domainSuffix, testDomain);

    logger.info(
        "Website integration test initialized, testDomain={}, testWebroot={}",
        testDomain,
        testWebroot);
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
  @DisplayName("Should query website list")
  void testGetWebsiteList() {
    try {
      BtResult<List<Map<String, Object>>> result =
          apiManager.execute(new GetWebsiteListApi().setPage(1).setLimit(20));

      assertTrue(result.isSuccess(), "Failed to get website list: " + result.getMsg());
      assertNotNull(result.getData(), "Website list should not be null");
    } catch (BtApiException exception) {
      logger.error("Failed while querying website list", exception);
      fail("Failed while querying website list: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("Should create website")
  void testCreateWebsite() {
    try {
      BtResult<CreateWebsiteResult> result = createWebsite();

      assertTrue(result.isSuccess(), "Failed to create website: " + result.getMsg());
      assertNotNull(result.getData(), "Create website result should not be null");
      assertTrue(result.getData().isSiteStatus(), "Website creation should report success");
      assertNotNull(getWebsiteIdByName(testDomain), "Created website should be queryable");
    } catch (BtApiException exception) {
      logger.error("Failed while creating website", exception);
      fail("Failed while creating website: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("Should delete website")
  void testDeleteWebsite() {
    try {
      BtResult<CreateWebsiteResult> createResult = createWebsite();
      assertTrue(createResult.isSuccess(), "Failed to prepare website: " + createResult.getMsg());

      Integer websiteId = getWebsiteIdByName(testDomain);
      assertNotNull(websiteId, "Unable to resolve website id");

      BtResult<Boolean> deleteResult =
          apiManager.execute(
              new DeleteWebsiteApi(websiteId, testDomain)
                  .setDeletePath(true)
                  .setDeleteDatabase(false)
                  .setDeleteFtp(false));

      assertTrue(deleteResult.isSuccess(), "Failed to delete website: " + deleteResult.getMsg());
      assertTrue(Boolean.TRUE.equals(deleteResult.getData()), "Delete website should return true");
      assertTrue(getWebsiteIdByName(testDomain) == null, "Website should no longer exist");
    } catch (BtApiException exception) {
      logger.error("Failed while deleting website", exception);
      fail("Failed while deleting website: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("Should stop and start website via SiteStop/SiteStart")
  void testStopAndStartWebsite() {
    try {
      Integer websiteId = createWebsiteAndResolveId();

      BtResult<Boolean> stopResult = apiManager.website().stop(websiteId, testDomain);
      assertTrue(stopResult.isSuccess(), "Failed to stop website: " + stopResult.getMsg());
      assertEquals("0", websiteField(testDomain, "status"), "Website should be stopped");

      BtResult<Boolean> startResult = apiManager.website().start(websiteId, testDomain);
      assertTrue(startResult.isSuccess(), "Failed to start website: " + startResult.getMsg());
      assertEquals("1", websiteField(testDomain, "status"), "Website should be running again");
    } catch (BtApiException exception) {
      logger.error("Failed while stopping/starting website", exception);
      fail("Failed while stopping/starting website: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("Should read and switch website PHP version via GetSitePHPVersion/SetPHPVersion")
  void testSwitchWebsitePhpVersion() {
    try {
      createWebsiteAndResolveId();

      BtResult<String> before = apiManager.website().getPhpVersion(testDomain);
      assertTrue(before.isSuccess(), "Failed to read PHP version: " + before.getMsg());
      assertEquals("81", before.getData());

      BtResult<Boolean> toStatic = apiManager.website().updatePhpVersion(testDomain, "00");
      assertTrue(toStatic.isSuccess(), "Failed to switch to static: " + toStatic.getMsg());
      assertEquals("00", apiManager.website().getPhpVersion(testDomain).getData());

      BtResult<Boolean> back = apiManager.website().updatePhpVersion(testDomain, "81");
      assertTrue(back.isSuccess(), "Failed to switch back to PHP 8.1: " + back.getMsg());
      assertEquals("81", apiManager.website().getPhpVersion(testDomain).getData());
    } catch (BtApiException exception) {
      logger.error("Failed while switching website PHP version", exception);
      fail("Failed while switching website PHP version: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("Should write, read and clear website rewrite rules via the vhost rewrite file")
  void testRewriteRulesRoundTrip() {
    try {
      createWebsiteAndResolveId();
      String rules = "# sdk integration test\nlocation /it-probe { return 204; }";

      BtResult<Boolean> saveResult = apiManager.website().updateRewriteRules(testDomain, rules);
      assertTrue(saveResult.isSuccess(), "Failed to save rewrite rules: " + saveResult.getMsg());

      BtResult<String> readResult = apiManager.website().getRewriteRules(testDomain);
      assertTrue(readResult.isSuccess(), "Failed to read rewrite rules: " + readResult.getMsg());
      assertEquals(rules, readResult.getData());

      BtResult<Boolean> clearResult = apiManager.website().updateRewriteRules(testDomain, "");
      assertTrue(clearResult.isSuccess(), "Failed to clear rewrite rules: " + clearResult.getMsg());
      assertEquals("", apiManager.website().getRewriteRules(testDomain).getData());
    } catch (BtApiException exception) {
      logger.error("Failed while updating rewrite rules", exception);
      fail("Failed while updating rewrite rules: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("Should read and save website nginx config via the vhost nginx file")
  void testNginxConfigRoundTrip() {
    try {
      createWebsiteAndResolveId();

      BtResult<String> readResult = apiManager.website().getNginxConfig(testDomain);
      assertTrue(readResult.isSuccess(), "Failed to read nginx config: " + readResult.getMsg());
      String config = readResult.getData();
      assertTrue(
          config.contains("server_name " + testDomain),
          "Config should belong to the temporary site");

      // 原样写回，避免写入无效配置导致 Nginx 重载失败。
      BtResult<Boolean> saveResult = apiManager.website().updateNginxConfig(testDomain, config);
      assertTrue(saveResult.isSuccess(), "Failed to save nginx config: " + saveResult.getMsg());
      assertEquals(config, apiManager.website().getNginxConfig(testDomain).getData());
    } catch (BtApiException exception) {
      logger.error("Failed while round-tripping nginx config", exception);
      fail("Failed while round-tripping nginx config: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("Should toggle website .user.ini protection via SetDirUserINI with id and path")
  void testToggleUserIni() {
    try {
      Integer websiteId = createWebsiteAndResolveId();
      Object before = userIniEnabled(websiteId);

      BtResult<Boolean> first = apiManager.website().toggleUserIni(websiteId, testWebroot);
      assertTrue(first.isSuccess(), "Failed to toggle .user.ini: " + first.getMsg());
      assertTrue(!before.equals(userIniEnabled(websiteId)), ".user.ini state should flip");

      BtResult<Boolean> second = apiManager.website().toggleUserIni(websiteId, testWebroot);
      assertTrue(second.isSuccess(), "Failed to toggle .user.ini back: " + second.getMsg());
      assertEquals(before, userIniEnabled(websiteId), ".user.ini state should be restored");
    } catch (BtApiException exception) {
      logger.error("Failed while toggling .user.ini", exception);
      fail("Failed while toggling .user.ini: " + exception.getMessage());
    }
  }

  private Object userIniEnabled(Integer websiteId) throws BtApiException {
    BtResult<Map<String, Object>> config = apiManager.website().getConfig(websiteId, testWebroot);
    assertTrue(config.isSuccess(), "Failed to read site directory config: " + config.getMsg());
    return config.getData().get("userini");
  }

  @Test
  @DisplayName("Should read PHP runtime config via GetPHPConfig for an installed PHP version")
  void testGetPhpRuntimeConfig() {
    try {
      BtResult<List<PhpVersion>> versions = apiManager.website().listPhpVersions();
      assertTrue(versions.isSuccess(), "Failed to list PHP versions: " + versions.getMsg());
      String installed =
          versions.getData().stream()
              .map(PhpVersion::getVersion)
              .filter(version -> version != null && version.matches("\\d{2}"))
              .filter(version -> !"00".equals(version))
              .findFirst()
              .orElse(null);
      assumeTrue(installed != null, "No PHP runtime is installed on the panel");

      BtResult<Map<String, Object>> config = apiManager.website().getPhpRuntimeConfig(installed);
      assertTrue(config.isSuccess(), "Failed to read PHP config: " + config.getMsg());
      assertTrue(
          config.getData().containsKey("disable_functions"),
          "PHP config should contain disable_functions");
    } catch (BtApiException exception) {
      logger.error("Failed while reading PHP runtime config", exception);
      fail("Failed while reading PHP runtime config: " + exception.getMessage());
    }
  }

  private Integer createWebsiteAndResolveId() throws BtApiException {
    BtResult<CreateWebsiteResult> createResult = createWebsite();
    assertTrue(createResult.isSuccess(), "Failed to prepare website: " + createResult.getMsg());
    Integer websiteId = getWebsiteIdByName(testDomain);
    assertNotNull(websiteId, "Unable to resolve website id");
    return websiteId;
  }

  private String websiteField(String domain, String field) throws BtApiException {
    Map<String, Object> website = findWebsite(domain);
    return website == null ? null : String.valueOf(website.get(field));
  }

  private BtResult<CreateWebsiteResult> createWebsite() throws BtApiException {
    WebsiteCreateRequest request =
        WebsiteCreateRequest.builder(testDomain, testWebroot)
            .phpVersion("81")
            .remark("Integration test website")
            .build();

    try {
      return apiManager.website().create(request);
    } catch (BtApiException exception) {
      if (isAlreadyExists(exception) && getWebsiteIdByName(testDomain) != null) {
        return successfulCreateWebsiteResult();
      }
      throw exception;
    }
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
    Map<String, Object> website = findWebsite(domain);
    return website == null ? null : toInteger(website.get("id"));
  }

  private Map<String, Object> findWebsite(String domain) throws BtApiException {
    BtResult<List<Map<String, Object>>> result =
        apiManager.execute(new GetWebsiteListApi().setPage(1).setLimit(100));

    if (!result.isSuccess() || result.getData() == null) {
      return null;
    }

    for (Map<String, Object> website : result.getData()) {
      if (domain.equals(website.get("name"))) {
        return website;
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

  private BtResult<CreateWebsiteResult> successfulCreateWebsiteResult() {
    CreateWebsiteResult createWebsiteResult = new CreateWebsiteResult();
    createWebsiteResult.setSiteStatus(true);

    BtResult<CreateWebsiteResult> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg("Website already existed after a retry; treating fixture as successful");
    result.setData(createWebsiteResult);
    return result;
  }
}
