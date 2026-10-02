package net.heimeng.sdk.btapi.client;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import net.heimeng.sdk.btapi.config.BtSdkConfig;

/** SSL 上下文配置器，负责把 {@link BtSdkConfig.SslMode} 应用到 HttpClient。 */
final class SslContextConfigurer {

  private SslContextConfigurer() {}

  static void configure(HttpClient.Builder builder, BtSdkConfig config) {
    switch (config.getSslMode()) {
      case SYSTEM_TRUST:
        return;
      case INSECURE_TRUST_ALL:
        builder.sslContext(buildInsecureSslContext());
        return;
      case CUSTOM_TRUST_STORE:
        builder.sslContext(buildCustomTrustStoreSslContext(config));
        return;
      default:
        throw new IllegalStateException("Unsupported SSL mode: " + config.getSslMode());
    }
  }

  private static SSLContext buildInsecureSslContext() {
    try {
      TrustManager[] trustAllCerts =
          new TrustManager[] {
            new X509TrustManager() {
              @Override
              public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
              }

              @Override
              public void checkClientTrusted(X509Certificate[] certs, String authType) {}

              @Override
              public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
          };

      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustAllCerts, new SecureRandom());
      return sslContext;
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to initialize insecure SSL context", exception);
    }
  }

  private static SSLContext buildCustomTrustStoreSslContext(BtSdkConfig config) {
    Path trustStorePath = Path.of(config.getTrustStorePath());
    if (!Files.exists(trustStorePath)) {
      throw new IllegalStateException("Trust store file does not exist: " + trustStorePath);
    }

    char[] trustStorePasswordChars =
        config.getTrustStorePassword() == null
            ? null
            : config.getTrustStorePassword().toCharArray();
    try {
      KeyStore trustStore = KeyStore.getInstance(config.getTrustStoreType());
      try (var inputStream = Files.newInputStream(trustStorePath)) {
        trustStore.load(inputStream, trustStorePasswordChars);
      }

      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);

      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
      return sslContext;
    } catch (Exception exception) {
      throw new IllegalStateException(
          "Failed to initialize custom trust store SSL context", exception);
    } finally {
      if (trustStorePasswordChars != null) {
        Arrays.fill(trustStorePasswordChars, '\0');
      }
    }
  }
}
