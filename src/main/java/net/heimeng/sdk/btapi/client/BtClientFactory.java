package net.heimeng.sdk.btapi.client;

import java.util.Objects;

import net.heimeng.sdk.btapi.config.BtSdkConfig;

/**
 * 用于创建底层客户端与高层管理器的工厂类。
 *
 * <p>该类统一收敛推荐的创建方式，便于业务代码快速构建 SDK 配置、客户端和门面入口。
 */
public final class BtClientFactory {

  private BtClientFactory() {}

  /** 根据已校验的配置创建底层客户端。 */
  public static BtClient createClient(BtSdkConfig config) {
    return new DefaultBtClient(Objects.requireNonNull(config, "config cannot be null"));
  }

  /** 根据基础地址和 API 密钥创建底层客户端。 */
  public static BtClient createClient(String baseUrl, String apiKey) {
    return createClient(configBuilder().baseUrl(baseUrl).apiKey(apiKey).build());
  }

  /** 根据基础地址、API 密钥和超时配置创建底层客户端。 */
  public static BtClient createClient(
      String baseUrl, String apiKey, int connectTimeout, int readTimeout) {
    return createClient(
        configBuilder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .connectTimeout(connectTimeout)
            .readTimeout(readTimeout)
            .build());
  }

  /** 根据基础地址、API 密钥、超时和重试次数创建底层客户端。 */
  public static BtClient createClient(
      String baseUrl, String apiKey, int connectTimeout, int readTimeout, int retryCount) {
    return createClient(
        configBuilder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .connectTimeout(connectTimeout)
            .readTimeout(readTimeout)
            .retryCount(retryCount)
            .build());
  }

  /** 根据已校验的配置创建高层 API 管理器。 */
  public static BtApiManager createApiManager(BtSdkConfig config) {
    return new BtApiManager(createClient(config));
  }

  /** 根据基础地址和 API 密钥创建高层 API 管理器。 */
  public static BtApiManager createApiManager(String baseUrl, String apiKey) {
    return new BtApiManager(createClient(baseUrl, apiKey));
  }

  /** 创建 SDK 配置构建器。 */
  public static BtSdkConfig.Builder configBuilder() {
    return BtSdkConfig.builder();
  }
}
