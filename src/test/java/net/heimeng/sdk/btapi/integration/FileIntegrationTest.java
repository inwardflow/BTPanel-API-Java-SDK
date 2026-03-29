package net.heimeng.sdk.btapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayDeque;
import java.util.Deque;
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

import net.heimeng.sdk.btapi.api.file.CreateFileApi;
import net.heimeng.sdk.btapi.api.file.CreateFileDirectoryApi;
import net.heimeng.sdk.btapi.api.file.DeleteFileApi;
import net.heimeng.sdk.btapi.api.file.GetFileContentApi;
import net.heimeng.sdk.btapi.api.file.SaveFileContentApi;
import net.heimeng.sdk.btapi.api.website.DeleteWebsiteApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteListApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.facade.WebsiteCreateRequest;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.CreateWebsiteResult;

/**
 * 文件模块集成测试。
 *
 * <p>测试会真实调用宝塔面板文件接口，并在结束后尝试清理测试资源。
 */
@DisplayName("文件模块集成测试")
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
@Timeout(value = 90, unit = TimeUnit.SECONDS)
class FileIntegrationTest extends AbstractIntegrationTestSupport {

  private static final Logger logger = LoggerFactory.getLogger(FileIntegrationTest.class);

  private final Deque<String> cleanupPaths = new ArrayDeque<>();

  private BtApiManager apiManager;
  private String websiteDomain;
  private String websiteWebroot;
  private String testRootPath;
  private String testFilePath;
  private String testDirectoryPath;
  private String testContent;

  @BeforeEach
  void setUp() {
    assumeConfigurationPresent(ENV_BASE_URL, "baseUrl");
    assumeConfigurationPresent(ENV_API_KEY, "apiKey");
    assumeConfigurationPresent(ENV_TEST_FILE_PATH, "test.filePath");

    apiManager = createApiManager();
    assumePanelApiAccessible(apiManager);

    String suffix = uniqueSuffix();
    String baseFilePath = getRequiredConfiguration(ENV_TEST_FILE_PATH, "test.filePath");
    String fileName = getFileName(baseFilePath);

    prepareWebsiteFixture(suffix);
    testRootPath = appendChildPath(resolveFileBaseDirectory(baseFilePath), "it-file-" + suffix);
    testFilePath = appendChildPath(testRootPath, fileName);
    testDirectoryPath = appendChildPath(testRootPath, "test-dir");
    testContent = "这是测试文件内容 - " + uniqueSuffix();

    logger.info(
        "文件集成测试初始化完成，websiteDomain={}, testRootPath={}, testFilePath={}, testDirectoryPath={}",
        websiteDomain,
        testRootPath,
        testFilePath,
        testDirectoryPath);
  }

  @AfterEach
  void tearDown() {
    try {
      while (!cleanupPaths.isEmpty()) {
        deletePathQuietly(cleanupPaths.pop());
      }
      deleteWebsiteIfExists();
    } finally {
      closeQuietly(apiManager);
    }
  }

  @Test
  @DisplayName("应能创建测试目录")
  void testCreateDirectory() {
    logger.info("开始创建测试目录：{}", testDirectoryPath);

    try {
      ensureWebsiteExists();
      ensureRootDirectory();

      BtResult<Boolean> result =
          apiManager.execute(new CreateFileDirectoryApi().setPath(testDirectoryPath));

      assertTrue(result.isSuccess(), "创建目录失败: " + result.getMsg());
      assertTrue(result.getData(), "创建目录操作返回失败");
      registerCleanup(testDirectoryPath);
    } catch (BtApiException exception) {
      logger.error("创建目录时发生 API 异常", exception);
      fail("创建目录时发生 API 异常: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("应能保存文件内容")
  void testSaveFileContent() {
    logger.info("开始保存测试文件：{}", testFilePath);

    try {
      ensureWebsiteExists();
      ensureRootDirectory();
      saveFile(testFilePath, testContent);
    } catch (BtApiException exception) {
      logger.error("保存文件内容时发生 API 异常", exception);
      fail("保存文件内容时发生 API 异常: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("应能读取刚写入的文件内容")
  void testGetFileContent() {
    logger.info("开始读取测试文件：{}", testFilePath);

    try {
      ensureWebsiteExists();
      ensureRootDirectory();
      saveFile(testFilePath, testContent);

      BtResult<String> result = apiManager.execute(new GetFileContentApi().setPath(testFilePath));

      assertTrue(result.isSuccess(), "读取文件内容失败: " + result.getMsg());
      assertNotNull(result.getData(), "返回的文件内容不能为空");
      assertEquals(testContent, result.getData(), "返回的文件内容与预期不一致");
    } catch (BtApiException exception) {
      logger.error("读取文件内容时发生 API 异常", exception);
      fail("读取文件内容时发生 API 异常: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("应能删除已创建的文件")
  void testDeleteFile() {
    logger.info("开始删除测试文件：{}", testFilePath);

    try {
      ensureWebsiteExists();
      ensureRootDirectory();
      saveFile(testFilePath, testContent);

      BtResult<Boolean> result = apiManager.execute(new DeleteFileApi().setPath(testFilePath));

      assertTrue(result.isSuccess(), "删除文件失败: " + result.getMsg());
      assertTrue(result.getData(), "删除文件操作返回失败");
      cleanupPaths.remove(testFilePath);
    } catch (BtApiException exception) {
      logger.error("删除文件时发生 API 异常", exception);
      fail("删除文件时发生 API 异常: " + exception.getMessage());
    }
  }

  @Test
  @DisplayName("应能完成目录内文件的完整操作流")
  void testFileOperationFlow() {
    logger.info("开始执行文件操作流测试");

    try {
      ensureWebsiteExists();
      ensureRootDirectory();

      BtResult<Boolean> createDirectoryResult =
          apiManager.execute(new CreateFileDirectoryApi().setPath(testDirectoryPath));
      assertTrue(createDirectoryResult.isSuccess(), "创建目录失败: " + createDirectoryResult.getMsg());
      registerCleanup(testDirectoryPath);

      String fileInDirectory = appendChildPath(testDirectoryPath, "test-file.txt");
      saveFile(fileInDirectory, "目录内文件内容");

      BtResult<String> readResult =
          apiManager.execute(new GetFileContentApi().setPath(fileInDirectory));
      assertTrue(readResult.isSuccess(), "读取目录内文件失败: " + readResult.getMsg());
      assertEquals("目录内文件内容", readResult.getData(), "目录内文件内容与预期不一致");

      BtResult<Boolean> deleteFileResult =
          apiManager.execute(new DeleteFileApi().setPath(fileInDirectory));
      assertTrue(deleteFileResult.isSuccess(), "删除目录内文件失败: " + deleteFileResult.getMsg());
      cleanupPaths.remove(fileInDirectory);

      BtResult<Boolean> deleteDirectoryResult =
          apiManager.execute(new DeleteFileApi().setPath(testDirectoryPath));
      assertTrue(deleteDirectoryResult.isSuccess(), "删除目录失败: " + deleteDirectoryResult.getMsg());
      cleanupPaths.remove(testDirectoryPath);
    } catch (BtApiException exception) {
      logger.error("文件操作流测试发生 API 异常", exception);
      fail("文件操作流测试发生 API 异常: " + exception.getMessage());
    }
  }

  private void ensureRootDirectory() throws BtApiException {
    if (cleanupPaths.contains(testRootPath)) {
      return;
    }

    BtResult<Boolean> result =
        apiManager.execute(new CreateFileDirectoryApi().setPath(testRootPath));
    assertTrue(result.isSuccess(), "创建文件测试根目录失败: " + result.getMsg());
    assertTrue(result.getData(), "创建文件测试根目录操作返回失败");
    registerCleanup(testRootPath);
  }

  private void saveFile(String path, String content) throws BtApiException {
    BtResult<Boolean> createFileResult = apiManager.execute(new CreateFileApi().setPath(path));
    assertTrue(createFileResult.isSuccess(), "创建测试文件失败: " + createFileResult.getMsg());
    assertTrue(createFileResult.getData(), "创建测试文件操作返回失败");

    BtResult<Boolean> result =
        apiManager.execute(new SaveFileContentApi().setPath(path).setData(content));

    assertTrue(result.isSuccess(), "保存文件内容失败: " + result.getMsg());
    assertTrue(result.getData(), "保存文件内容操作返回失败");
    registerCleanup(path);
  }

  private void registerCleanup(String path) {
    cleanupPaths.remove(path);
    cleanupPaths.push(path);
  }

  private void prepareWebsiteFixture(String suffix) {
    if (!hasConfiguration(ENV_TEST_DOMAIN_SUFFIX, "test.domain")
        || !hasConfiguration(ENV_TEST_WEBROOT_BASE, "test.webroot")) {
      return;
    }

    String configuredDomain = getRequiredConfiguration(ENV_TEST_DOMAIN_SUFFIX, "test.domain");
    String configuredWebroot = getRequiredConfiguration(ENV_TEST_WEBROOT_BASE, "test.webroot");
    websiteDomain = buildIsolatedTestDomain(configuredDomain, "file-" + suffix);
    websiteWebroot = buildIsolatedTestWebroot(configuredWebroot, configuredDomain, websiteDomain);
  }

  private void ensureWebsiteExists() throws BtApiException {
    if (websiteDomain == null || websiteWebroot == null) {
      return;
    }
    if (getWebsiteIdByName(websiteDomain) != null) {
      return;
    }

    BtResult<CreateWebsiteResult> result =
        apiManager
            .website()
            .create(
                WebsiteCreateRequest.builder(websiteDomain, websiteWebroot)
                    .phpVersion("81")
                    .remark("文件集成测试网站")
                    .build());

    assertTrue(result.isSuccess(), "准备文件测试网站失败: " + result.getMsg());
    assertNotNull(result.getData(), "准备文件测试网站时返回数据不能为空");
    assertTrue(result.getData().isSiteStatus(), "准备文件测试网站应返回成功状态");
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
        logger.warn("清理文件测试网站失败：{}，原因：{}", websiteDomain, result.getMsg());
      }
    } catch (Exception exception) {
      logger.warn("清理文件测试网站时发生异常：{}，原因：{}", websiteDomain, exception.getMessage());
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
      logger.warn("清理测试路径失败：{}，原因：{}", path, exception.getMessage());
    }
  }

  private String resolveFileBaseDirectory(String configuredFilePath) {
    if (websiteWebroot != null && !websiteWebroot.isBlank()) {
      return websiteWebroot;
    }
    return getParentPath(configuredFilePath);
  }

  private String getFileName(String path) {
    int lastSlashIndex = path.lastIndexOf('/');
    if (lastSlashIndex < 0) {
      return path;
    }
    return path.substring(lastSlashIndex + 1);
  }
}
