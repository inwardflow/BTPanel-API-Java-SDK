package net.heimeng.sdk.btapi.integration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.Assumptions;

import net.heimeng.sdk.btapi.api.system.GetSystemInfoApi;
import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.client.BtClientFactory;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;

abstract class AbstractIntegrationTestSupport {

  protected static final String PROPERTIES_FILE = "application-test.properties";
  protected static final String ENV_BASE_URL = "BT_PANEL_BASE_URL";
  protected static final String ENV_API_KEY = "BT_PANEL_API_KEY";
  protected static final String ENV_TEST_DB_NAME = "BT_PANEL_TEST_DB_NAME";
  protected static final String ENV_TEST_DB_USER = "BT_PANEL_TEST_DB_USER";
  protected static final String ENV_TEST_DB_PASSWORD = "BT_PANEL_TEST_DB_PASSWORD";
  protected static final String ENV_TEST_FILE_PATH = "BT_PANEL_TEST_FILE_PATH";
  protected static final String ENV_TEST_FTP_ROOT = "BT_PANEL_TEST_FTP_ROOT";
  protected static final String ENV_TEST_DOMAIN_SUFFIX = "BT_PANEL_TEST_DOMAIN_SUFFIX";
  protected static final String ENV_TEST_WEBROOT_BASE = "BT_PANEL_TEST_WEBROOT_BASE";
  protected static final String ENV_TEST_SSL_CERT_COMMON_NAME =
      "BT_PANEL_TEST_SSL_CERT_COMMON_NAME";
  protected static final String ENV_TEST_SSL_CERT_DOMAINS = "BT_PANEL_TEST_SSL_CERT_DOMAINS";
  protected static final String ENV_CONNECT_TIMEOUT = "BT_PANEL_CONNECT_TIMEOUT";
  protected static final String ENV_READ_TIMEOUT = "BT_PANEL_READ_TIMEOUT";
  protected static final String ENV_RETRY_COUNT = "BT_PANEL_RETRY_COUNT";
  protected static final String ENV_VERIFY_SSL = "BT_PANEL_VERIFY_SSL";

  private Properties testProperties;

  protected final BtApiManager createApiManager() {
    return BtClientFactory.createApiManager(createSdkConfig());
  }

  protected final BtSdkConfig createSdkConfig() {
    return createSdkConfig(
        getRequiredConfiguration(ENV_BASE_URL, "baseUrl"),
        getRequiredConfiguration(ENV_API_KEY, "apiKey"));
  }

  /** 使用与正常测试相同的 SSL、超时和重试配置，仅替换地址与密钥，便于构造失败场景。 */
  protected final BtApiManager createApiManager(String baseUrl, String apiKey) {
    return BtClientFactory.createApiManager(createSdkConfig(baseUrl, apiKey));
  }

  private BtSdkConfig createSdkConfig(String baseUrl, String apiKey) {
    BtSdkConfig.Builder builder =
        BtSdkConfig.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .connectTimeout(getIntConfiguration(ENV_CONNECT_TIMEOUT, "connectTimeout", 5000))
            .readTimeout(getIntConfiguration(ENV_READ_TIMEOUT, "readTimeout", 10000))
            .retryCount(getIntConfiguration(ENV_RETRY_COUNT, "retryCount", 3));

    if (!getBooleanConfiguration(ENV_VERIFY_SSL, "verifySsl", false)) {
      builder.verifySsl(false);
    }

    return builder.build();
  }

  protected final String getRequiredConfiguration(String envKey, String propertyKey) {
    String value = getOptionalConfiguration(envKey, propertyKey);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
          "Missing required integration test configuration: "
              + envKey
              + " or "
              + PROPERTIES_FILE
              + " -> "
              + propertyKey);
    }
    return value.trim();
  }

  protected final String getOptionalConfiguration(String envKey, String propertyKey) {
    String environmentValue = System.getenv(envKey);
    if (environmentValue != null && !environmentValue.isBlank()) {
      return environmentValue.trim();
    }
    return loadTestProperties().getProperty(propertyKey);
  }

  protected final boolean hasConfiguration(String envKey, String propertyKey) {
    String value = getOptionalConfiguration(envKey, propertyKey);
    return value != null && !value.isBlank();
  }

  protected final void assumeConfigurationPresent(String envKey, String propertyKey) {
    Assumptions.assumeTrue(
        hasConfiguration(envKey, propertyKey),
        () ->
            "Skipping integration test because missing configuration: "
                + envKey
                + " or "
                + PROPERTIES_FILE
                + " -> "
                + propertyKey);
  }

  protected final void assumePanelApiAccessible(BtApiManager apiManager) {
    try {
      apiManager.execute(new GetSystemInfoApi());
    } catch (BtApiException exception) {
      if (isIpValidationFailure(exception)) {
        Assumptions.assumeTrue(
            false, "Skipping integration test because current IP is not in panel whitelist");
      }
      if (isTemporarilyBlocked(exception)) {
        Assumptions.assumeTrue(
            false, "Skipping integration test because panel temporarily blocks API validation");
      }
      throw exception;
    }
  }

  protected final int getIntConfiguration(String envKey, String propertyKey, int defaultValue) {
    String rawValue = getOptionalConfiguration(envKey, propertyKey);
    if (rawValue == null || rawValue.isBlank()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(rawValue.trim());
    } catch (NumberFormatException exception) {
      throw new IllegalStateException(
          "Invalid integer integration test configuration: "
              + envKey
              + " or "
              + propertyKey
              + "="
              + rawValue,
          exception);
    }
  }

  protected final boolean getBooleanConfiguration(
      String envKey, String propertyKey, boolean defaultValue) {
    String rawValue = getOptionalConfiguration(envKey, propertyKey);
    if (rawValue == null || rawValue.isBlank()) {
      return defaultValue;
    }
    return Boolean.parseBoolean(rawValue.trim());
  }

  protected final String uniqueSuffix() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  protected final String appendChildPath(String baseDirectory, String childName) {
    String normalizedBase = stripTrailingSlash(baseDirectory);
    return normalizedBase + "/" + childName;
  }

  protected final String buildIsolatedTestDomain(String configuredDomain, String prefix) {
    if (configuredDomain == null || configuredDomain.isBlank()) {
      throw new IllegalArgumentException("configuredDomain cannot be blank");
    }
    if (prefix == null || prefix.isBlank()) {
      throw new IllegalArgumentException("prefix cannot be blank");
    }

    String normalizedDomain = configuredDomain.trim();
    while (normalizedDomain.startsWith(".")) {
      normalizedDomain = normalizedDomain.substring(1);
    }
    return prefix + "." + normalizedDomain;
  }

  protected final String buildIsolatedTestWebroot(
      String configuredWebroot, String configuredDomain, String actualDomain) {
    if (configuredWebroot == null || configuredWebroot.isBlank()) {
      throw new IllegalArgumentException("configuredWebroot cannot be blank");
    }
    if (configuredDomain == null || configuredDomain.isBlank()) {
      throw new IllegalArgumentException("configuredDomain cannot be blank");
    }
    if (actualDomain == null || actualDomain.isBlank()) {
      throw new IllegalArgumentException("actualDomain cannot be blank");
    }

    String normalizedWebroot = stripTrailingSlash(configuredWebroot);
    String normalizedDomain = configuredDomain.trim();
    while (normalizedDomain.startsWith(".")) {
      normalizedDomain = normalizedDomain.substring(1);
    }

    if (normalizedWebroot.endsWith("/" + normalizedDomain)) {
      return appendChildPath(getParentPath(normalizedWebroot), actualDomain);
    }
    return appendChildPath(normalizedWebroot, actualDomain);
  }

  protected final String getParentPath(String path) {
    String normalizedPath = stripTrailingSlash(path);
    int lastSlashIndex = normalizedPath.lastIndexOf('/');
    if (lastSlashIndex < 0) {
      throw new IllegalArgumentException("path must contain '/'");
    }
    if (lastSlashIndex == 0) {
      return "/";
    }
    return normalizedPath.substring(0, lastSlashIndex);
  }

  protected final String stripTrailingSlash(String path) {
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException("path cannot be blank");
    }
    String normalized = path.trim();
    while (normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  protected final String loadResourceText(String resourcePath) {
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalStateException("Missing integration test resource: " + resourcePath);
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Failed to load integration test resource: " + resourcePath, exception);
    }
  }

  protected final void closeQuietly(BtApiManager apiManager) {
    if (apiManager == null) {
      return;
    }
    try {
      apiManager.close();
    } catch (Exception ignored) {
      // Do not let cleanup failures hide the original test result.
    }
  }

  protected final boolean isIpValidationFailure(Throwable throwable) {
    if (throwable == null || throwable.getMessage() == null) {
      return false;
    }
    String message = throwable.getMessage();
    return message.contains("IP校验失败") || message.toLowerCase().contains("ip validation failed");
  }

  protected final boolean isTemporarilyBlocked(Throwable throwable) {
    if (throwable == null || throwable.getMessage() == null) {
      return false;
    }
    String message = throwable.getMessage();
    String normalized = message.toLowerCase();
    return message.contains("连续20次验证失败")
        || message.contains("禁止1小时")
        || normalized.contains("temporarily blocked")
        || normalized.contains("blocked for 1 hour");
  }

  protected final boolean isEndpointNotFound(Throwable throwable) {
    if (throwable == null || throwable.getMessage() == null) {
      return false;
    }
    return throwable.getMessage().contains("status: 404");
  }

  protected final boolean isInvalidParameter(Throwable throwable) {
    if (throwable == null || throwable.getMessage() == null) {
      return false;
    }
    String message = throwable.getMessage();
    return message.contains("指定参数无效") || message.toLowerCase().contains("invalid parameter");
  }

  protected final boolean isAlreadyExists(Throwable throwable) {
    if (throwable == null || throwable.getMessage() == null) {
      return false;
    }
    String message = throwable.getMessage();
    String normalized = message.toLowerCase();
    return message.contains("已存在")
        || normalized.contains("already exists")
        || normalized.contains("duplicate");
  }

  protected final boolean isFileNotFound(Throwable throwable) {
    if (throwable == null || throwable.getMessage() == null) {
      return false;
    }
    String message = throwable.getMessage();
    // DeleteFile 返回“指定文件不存在”，DeleteDir 返回“指定目录不存在”。
    return message.contains("指定文件不存在")
        || message.contains("指定目录不存在")
        || message.toLowerCase().contains("file not found");
  }

  private Properties loadTestProperties() {
    if (testProperties != null) {
      return testProperties;
    }

    testProperties = new Properties();
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
      if (stream != null) {
        testProperties.load(stream);
      }
      return testProperties;
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Failed to load integration test properties from " + PROPERTIES_FILE, exception);
    }
  }
}
