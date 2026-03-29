package net.heimeng.sdk.btapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heimeng.sdk.btapi.api.file.CreateFileDirectoryApi;
import net.heimeng.sdk.btapi.api.file.DeleteFileApi;
import net.heimeng.sdk.btapi.api.ftp.GetFtpAccountsApi;
import net.heimeng.sdk.btapi.api.website.DeleteWebsiteApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteListApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.facade.FtpCreateRequest;
import net.heimeng.sdk.btapi.facade.FtpDeleteRequest;
import net.heimeng.sdk.btapi.facade.FtpPasswordUpdateRequest;
import net.heimeng.sdk.btapi.facade.WebsiteCreateRequest;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ftp.FtpAccount;
import net.heimeng.sdk.btapi.model.website.CreateWebsiteResult;

@DisplayName("FTP integration tests")
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
@Timeout(value = 90, unit = TimeUnit.SECONDS)
class FtpIntegrationTest extends AbstractIntegrationTestSupport {

  private static final Logger logger = LoggerFactory.getLogger(FtpIntegrationTest.class);

  private BtApiManager apiManager;
  private String websiteDomain;
  private String websiteWebroot;
  private String ftpUsername;
  private String ftpPassword;
  private String updatedFtpPassword;
  private String ftpBasePath;
  private String ftpHomePath;

  @BeforeEach
  void setUp() {
    assumeConfigurationPresent(ENV_BASE_URL, "baseUrl");
    assumeConfigurationPresent(ENV_API_KEY, "apiKey");

    apiManager = createApiManager();
    assumePanelApiAccessible(apiManager);

    String suffix = uniqueSuffix();
    prepareWebsiteFixture(suffix);

    String ftpBaseDirectory = resolveFtpBaseDirectory();
    Assumptions.assumeTrue(
        ftpBaseDirectory != null,
        () ->
            "Skipping integration test because missing FTP root configuration. Configure "
                + ENV_TEST_FTP_ROOT
                + " or provide website/file integration test configuration.");

    ftpUsername = "itftp" + suffix;
    ftpPassword = "BtIt" + suffix + "Pwd1";
    updatedFtpPassword = "BtIt" + suffix + "Pwd2";
    ftpBasePath = appendChildPath(ftpBaseDirectory, "ftp-it-" + suffix);
    ftpHomePath = appendChildPath(ftpBasePath, ftpUsername);

    logger.info(
        "FTP integration test initialized, websiteDomain={}, ftpUsername={}, ftpBasePath={}, ftpHomePath={}",
        websiteDomain,
        ftpUsername,
        ftpBasePath,
        ftpHomePath);
  }

  @AfterEach
  void tearDown() {
    try {
      deleteFtpAccountIfExists();
      deletePathQuietly(ftpHomePath);
      deletePathQuietly(ftpBasePath);
      deleteWebsiteIfExists();
    } finally {
      closeQuietly(apiManager);
    }
  }

  @Test
  @DisplayName("Should query FTP accounts")
  void testGetFtpAccounts() throws BtApiException {
    BtResult<List<FtpAccount>> result = apiManager.execute(new GetFtpAccountsApi());

    assertTrue(result.isSuccess(), "Failed to get FTP accounts: " + result.getMsg());
    assertNotNull(result.getData(), "FTP accounts should not be null");
  }

  @Test
  @DisplayName("Should create FTP account")
  void testCreateFtpAccount() throws BtApiException {
    createFtpAccountFixture();

    FtpAccount ftpAccount = getFtpAccountByUsername(ftpUsername);
    assertNotNull(ftpAccount, "Created FTP account should be queryable");
    assertEquals(ftpUsername, ftpAccount.getUsername(), "FTP username mismatch");
    assertEquals(ftpHomePath, ftpAccount.getPath(), "FTP home path mismatch");
  }

  @Test
  @DisplayName("Should update FTP password")
  void testUpdateFtpPassword() throws BtApiException {
    createFtpAccountFixture();
    FtpAccount ftpAccount = getFtpAccountByUsername(ftpUsername);
    assertNotNull(ftpAccount, "FTP account should exist before password change");

    BtResult<Boolean> result =
        apiManager
            .ftp()
            .updatePassword(
                new FtpPasswordUpdateRequest(
                    ftpAccount.getId(), ftpUsername, ftpAccount.getPath(), updatedFtpPassword));

    assertTrue(result.isSuccess(), "Failed to update FTP password: " + result.getMsg());
    assertTrue(Boolean.TRUE.equals(result.getData()), "Password change should return true");
    assertNotNull(getFtpAccountByUsername(ftpUsername), "FTP account should still exist");
  }

  @Test
  @DisplayName("Should delete FTP account")
  void testDeleteFtpAccount() throws BtApiException {
    createFtpAccountFixture();
    FtpAccount ftpAccount = getFtpAccountByUsername(ftpUsername);
    assertNotNull(ftpAccount, "FTP account should exist before delete");

    BtResult<Boolean> result =
        apiManager.ftp().delete(new FtpDeleteRequest(ftpAccount.getId(), ftpUsername));

    assertTrue(result.isSuccess(), "Failed to delete FTP account: " + result.getMsg());
    assertTrue(Boolean.TRUE.equals(result.getData()), "Delete FTP account should return true");
    assertTrue(getFtpAccountByUsername(ftpUsername) == null, "FTP account should no longer exist");
  }

  private void createFtpAccountFixture() throws BtApiException {
    ensureWebsiteExists();
    createFtpHomeDirectory();

    try {
      BtResult<Boolean> createResult =
          apiManager
              .ftp()
              .create(new FtpCreateRequest(ftpUsername, ftpPassword, ftpHomePath, ftpUsername));

      assertTrue(createResult.isSuccess(), "Failed to prepare FTP fixture: " + createResult.getMsg());
      assertTrue(Boolean.TRUE.equals(createResult.getData()), "FTP fixture should return true");
    } catch (BtApiException exception) {
      if (isInvalidParameter(exception)) {
        Assumptions.assumeTrue(
            false,
            "Skipping FTP write integration test because current panel rejects FTP account creation parameters");
      }
      if (isAlreadyExists(exception) && getFtpAccountByUsername(ftpUsername) != null) {
        return;
      }
      throw exception;
    }
  }

  private void createFtpHomeDirectory() throws BtApiException {
    try {
      BtResult<Boolean> baseDirectoryResult =
          apiManager.execute(new CreateFileDirectoryApi().setPath(ftpBasePath));
      assertTrue(
          baseDirectoryResult.isSuccess(),
          "Failed to create FTP base directory: " + baseDirectoryResult.getMsg());
      assertTrue(Boolean.TRUE.equals(baseDirectoryResult.getData()), "Base directory should return true");

      BtResult<Boolean> result =
          apiManager.execute(new CreateFileDirectoryApi().setPath(ftpHomePath));
      assertTrue(result.isSuccess(), "Failed to create FTP home directory: " + result.getMsg());
      assertTrue(Boolean.TRUE.equals(result.getData()), "Home directory should return true");
    } catch (BtApiException exception) {
      if (isInvalidParameter(exception)) {
        Assumptions.assumeTrue(
            false,
            "Skipping FTP write integration test because current panel rejects FTP home directory preparation");
      }
      if (isAlreadyExists(exception)) {
        return;
      }
      throw exception;
    }
  }

  private void prepareWebsiteFixture(String suffix) {
    if (!hasConfiguration(ENV_TEST_DOMAIN_SUFFIX, "test.domain")
        || !hasConfiguration(ENV_TEST_WEBROOT_BASE, "test.webroot")) {
      return;
    }

    String configuredDomain = getRequiredConfiguration(ENV_TEST_DOMAIN_SUFFIX, "test.domain");
    String configuredWebroot = getRequiredConfiguration(ENV_TEST_WEBROOT_BASE, "test.webroot");
    websiteDomain = buildIsolatedTestDomain(configuredDomain, "ftp-" + suffix);
    websiteWebroot = buildIsolatedTestWebroot(configuredWebroot, configuredDomain, websiteDomain);
  }

  private void ensureWebsiteExists() throws BtApiException {
    if (websiteDomain == null || websiteWebroot == null) {
      return;
    }
    if (getWebsiteIdByName(websiteDomain) != null) {
      return;
    }

    try {
      BtResult<CreateWebsiteResult> result =
          apiManager
              .website()
              .create(
                  WebsiteCreateRequest.builder(websiteDomain, websiteWebroot)
                      .phpVersion("81")
                      .remark("FTP integration test website")
                      .build());

      assertTrue(result.isSuccess(), "Failed to prepare FTP test website: " + result.getMsg());
      assertNotNull(result.getData(), "Website creation result should not be null");
      assertTrue(result.getData().isSiteStatus(), "Website creation should report success");
    } catch (BtApiException exception) {
      if (isAlreadyExists(exception) && getWebsiteIdByName(websiteDomain) != null) {
        return;
      }
      throw exception;
    }
  }

  private FtpAccount getFtpAccountByUsername(String username) throws BtApiException {
    BtResult<List<FtpAccount>> result = apiManager.execute(new GetFtpAccountsApi());
    if (!result.isSuccess() || result.getData() == null) {
      return null;
    }

    return result.getData().stream()
        .filter(account -> username.equals(account.getUsername()))
        .findFirst()
        .orElse(null);
  }

  private void deleteFtpAccountIfExists() {
    if (apiManager == null || ftpUsername == null || ftpUsername.isBlank()) {
      return;
    }

    try {
      FtpAccount ftpAccount = getFtpAccountByUsername(ftpUsername);
      if (ftpAccount == null) {
        return;
      }

      BtResult<Boolean> result =
          apiManager.ftp().delete(new FtpDeleteRequest(ftpAccount.getId(), ftpUsername));
      if (!result.isSuccess()) {
        logger.warn("FTP account cleanup failed, ftpUsername={}, reason={}", ftpUsername, result.getMsg());
      }
    } catch (Exception exception) {
      logger.warn(
          "FTP account cleanup raised an exception, ftpUsername={}, reason={}",
          ftpUsername,
          exception.getMessage());
    }
  }

  private void deleteWebsiteIfExists() {
    if (apiManager == null || websiteDomain == null || websiteDomain.isBlank()) {
      return;
    }

    try {
      Integer websiteId = getWebsiteIdByName(websiteDomain);
      if (websiteId == null) {
        return;
      }

      BtResult<Boolean> result =
          apiManager.execute(
              new DeleteWebsiteApi(websiteId, websiteDomain)
                  .setDeletePath(true)
                  .setDeleteDatabase(false)
                  .setDeleteFtp(false));

      if (!result.isSuccess()) {
        logger.warn(
            "Website cleanup failed, websiteDomain={}, reason={}", websiteDomain, result.getMsg());
      }
    } catch (Exception exception) {
      logger.warn(
          "Website cleanup raised an exception, websiteDomain={}, reason={}",
          websiteDomain,
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

  private void deletePathQuietly(String path) {
    if (apiManager == null || path == null || path.isBlank()) {
      return;
    }

    try {
      apiManager.execute(new DeleteFileApi().setPath(path));
    } catch (Exception exception) {
      if (isFileNotFound(exception)) {
        return;
      }
      logger.warn("FTP directory cleanup failed, path={}, reason={}", path, exception.getMessage());
    }
  }

  private String resolveFtpBaseDirectory() {
    if (websiteWebroot != null && !websiteWebroot.isBlank()) {
      return websiteWebroot;
    }
    if (hasConfiguration(ENV_TEST_FTP_ROOT, "test.ftpRoot")) {
      return stripTrailingSlash(getRequiredConfiguration(ENV_TEST_FTP_ROOT, "test.ftpRoot"));
    }
    if (hasConfiguration(ENV_TEST_FILE_PATH, "test.filePath")) {
      return getParentPath(getRequiredConfiguration(ENV_TEST_FILE_PATH, "test.filePath"));
    }
    if (hasConfiguration(ENV_TEST_WEBROOT_BASE, "test.webroot")) {
      return stripTrailingSlash(getRequiredConfiguration(ENV_TEST_WEBROOT_BASE, "test.webroot"));
    }
    return null;
  }
}
