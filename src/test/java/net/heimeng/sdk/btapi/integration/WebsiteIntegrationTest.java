package net.heimeng.sdk.btapi.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    String webrootBase = getRequiredConfiguration(ENV_TEST_WEBROOT_BASE, "test.webroot");
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
