package net.heimeng.sdk.btapi.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import net.heimeng.sdk.btapi.api.system.CheckPanelUpdateApi;
import net.heimeng.sdk.btapi.api.system.GetDiskInfoApi;
import net.heimeng.sdk.btapi.api.system.GetNetworkStatusApi;
import net.heimeng.sdk.btapi.api.system.GetSystemInfoApi;
import net.heimeng.sdk.btapi.api.system.GetTaskCountApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.exception.BtAuthenticationException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.DiskInfo;
import net.heimeng.sdk.btapi.model.system.NetworkStatus;
import net.heimeng.sdk.btapi.model.system.PanelUpdateInfo;
import net.heimeng.sdk.btapi.model.system.SystemInfo;

/** System 模块集成测试，覆盖系统信息、磁盘、网络、任务与更新接口。 */
@DisplayName("System 模块集成测试")
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
@Timeout(value = 60, unit = TimeUnit.SECONDS)
class SystemIntegrationTest extends AbstractIntegrationTestSupport {

  private BtApiManager apiManager;

  @BeforeEach
  void setUp() {
    assumeConfigurationPresent(ENV_BASE_URL, "baseUrl");
    assumeConfigurationPresent(ENV_API_KEY, "apiKey");

    apiManager = createApiManager();
    assumePanelApiAccessible(apiManager);
  }

  @AfterEach
  void tearDown() {
    closeQuietly(apiManager);
  }

  @Test
  @DisplayName("使用有效配置时应能获取系统信息")
  void testGetSystemInfo() throws BtApiException {
    BtResult<SystemInfo> result = apiManager.execute(new GetSystemInfoApi());

    assertNotNull(result, "响应结果不能为空");
    assertTrue(result.isSuccess(), "获取系统信息失败: " + result.getMsg());

    SystemInfo systemInfo = result.getData();
    assertNotNull(systemInfo, "系统信息数据不能为空");
    assertNotNull(systemInfo.getOs(), "操作系统信息不能为空");
    assertTrue(
        systemInfo.getCpuUsage() >= 0 && systemInfo.getCpuUsage() <= 100, "CPU 使用率应在 0-100 之间");
    assertTrue(systemInfo.getMemoryTotal() >= 0, "总内存应大于等于 0");
    assertTrue(systemInfo.getMemoryUsed() >= 0, "已用内存应大于等于 0");
    assertNotNull(systemInfo.getPanelVersion(), "面板版本不能为空");
  }

  @Test
  @DisplayName("应能查询磁盘分区信息")
  void testGetDiskInfo() throws BtApiException {
    BtResult<List<DiskInfo>> result = apiManager.execute(new GetDiskInfoApi());

    assertTrue(result.isSuccess(), "获取磁盘分区信息失败: " + result.getMsg());
    assertNotNull(result.getData(), "磁盘分区信息不能为空");
    if (!result.getData().isEmpty()) {
      DiskInfo diskInfo = result.getData().get(0);
      assertNotNull(diskInfo.getPath(), "磁盘挂载点不能为空");
      assertNotNull(diskInfo.getSize(), "磁盘容量信息不能为空");
      assertNotNull(diskInfo.getInodes(), "磁盘 inode 信息不能为空");
    }
  }

  @Test
  @DisplayName("应能查询网络状态信息")
  void testGetNetworkStatus() throws BtApiException {
    BtResult<NetworkStatus> result = apiManager.execute(new GetNetworkStatusApi());

    assertTrue(result.isSuccess(), "获取网络状态信息失败: " + result.getMsg());
    assertNotNull(result.getData(), "网络状态信息不能为空");
    assertTrue(result.getData().getDownTotal() >= 0, "下行总流量不能为负数");
    assertTrue(result.getData().getUpTotal() >= 0, "上行总流量不能为负数");
    assertNotNull(result.getData().getCpu(), "CPU 状态不能为空");
    assertNotNull(result.getData().getMem(), "内存状态不能为空");
    assertNotNull(result.getData().getLoad(), "负载状态不能为空");
  }

  @Test
  @DisplayName("应能查询安装任务数量")
  void testGetTaskCount() throws BtApiException {
    BtResult<Integer> result = apiManager.execute(new GetTaskCountApi());

    assertTrue(result.isSuccess(), "获取安装任务数量失败: " + result.getMsg());
    assertNotNull(result.getData(), "安装任务数量不能为空");
    assertTrue(result.getData() >= 0, "安装任务数量不能为负数");
  }

  @Test
  @DisplayName("应能查询面板更新信息")
  void testCheckPanelUpdate() throws BtApiException {
    BtResult<PanelUpdateInfo> result = apiManager.execute(new CheckPanelUpdateApi());

    assertNotNull(result, "面板更新信息响应不能为空");
    assertNotNull(result.getData(), "面板更新信息数据不能为空");
    assertNotNull(result.getMsg(), "面板更新信息消息不能为空");
    assertNotNull(result.getData().getVersion(), "面板版本号不能为空");
    assertNotNull(result.getData().getUpdateMsg(), "面板更新说明不能为空");
  }

  @Test
  @DisplayName("使用无效 API Key 时应抛出业务异常")
  void testGetSystemInfoWithInvalidApiKey() {
    // 复用测试的 SSL 配置，确保失败原因是密钥校验，而不是证书校验。
    BtApiManager invalidApiManager =
        createApiManager(getRequiredConfiguration(ENV_BASE_URL, "baseUrl"), "invalid-api-key");

    try {
      BtAuthenticationException exception =
          assertThrows(
              BtAuthenticationException.class,
              () -> invalidApiManager.execute(new GetSystemInfoApi()),
              "使用无效 API Key 时应抛出 BtAuthenticationException");
      assertNotNull(exception.getMessage(), "异常消息不能为空");
      assertFalse(exception.getMessage().isBlank(), "异常消息不能为空白");
    } finally {
      closeQuietly(invalidApiManager);
    }
  }

  @Test
  @DisplayName("使用无效 URL 时应抛出异常")
  void testGetSystemInfoWithInvalidUrl() {
    BtApiManager invalidApiManager =
        createApiManager(
            "http://invalid.invalid:8888", getRequiredConfiguration(ENV_API_KEY, "apiKey"));

    try {
      assertThrows(
          BtApiException.class,
          () -> invalidApiManager.execute(new GetSystemInfoApi()),
          "使用无效 URL 时应抛出 BtApiException");
    } finally {
      closeQuietly(invalidApiManager);
    }
  }
}
