package net.heimeng.sdk.btapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import net.heimeng.sdk.btapi.api.system.GetTaskCountApi;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.config.CertificatePins;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.testutil.TestCertificates;

/** 用本地 HTTPS 服务器和一次性自签名证书，模拟宝塔面板默认的自签名证书场景。 */
@DisplayName("DefaultBtClient TLS tests")
class DefaultBtClientTlsTest {

  private final AtomicInteger requestCount = new AtomicInteger();
  private TestCertificates.SelfSigned serverCertificate;
  private HttpsServer server;
  private String baseUrl;

  @BeforeEach
  void startServer() throws Exception {
    serverCertificate = TestCertificates.generate("127.0.0.1", Duration.ofDays(1), true);
    char[] password = UUID.randomUUID().toString().toCharArray();
    KeyManagerFactory keyManagerFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(serverCertificate.keyStore(password), password);
    SSLContext serverContext = SSLContext.getInstance("TLS");
    serverContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

    server = HttpsServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.setHttpsConfigurator(new HttpsConfigurator(serverContext));
    server.createContext(
        "/",
        exchange -> {
          requestCount.incrementAndGet();
          byte[] body = "3".getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    server.start();
    baseUrl = "https://127.0.0.1:" + server.getAddress().getPort();
  }

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  @DisplayName("PINNED_PUBLIC_KEY should accept a self-signed certificate with a matching pin")
  void pinnedKeyAcceptsMatchingCertificate() throws IOException {
    BtSdkConfig config =
        baseConfig()
            .pinnedPublicKeys(CertificatePins.sha256(serverCertificate.certificate()))
            .build();

    try (DefaultBtClient client = new DefaultBtClient(config)) {
      BtResult<Integer> result = client.execute(new GetTaskCountApi());
      assertTrue(result.isSuccess());
      assertEquals(3, result.getData());
    }
  }

  @Test
  @DisplayName("PINNED_PUBLIC_KEY should reject other keys, report the server pin, and not retry")
  void pinnedKeyRejectsOtherCertificates() {
    String otherPin =
        CertificatePins.sha256(
            TestCertificates.generate("127.0.0.1", Duration.ofDays(1), true).certificate());
    BtSdkConfig config = baseConfig().pinnedPublicKeys(otherPin).build();

    try (DefaultBtClient client = new DefaultBtClient(config)) {
      BtApiException exception =
          assertThrows(BtApiException.class, () -> client.execute(new GetTaskCountApi()));
      assertTrue(exception.getMessage().contains("pinning failed"), exception.getMessage());
      assertTrue(
          exception.getMessage().contains(CertificatePins.sha256(serverCertificate.certificate())),
          "error should show the server's actual pin");
    }
    assertEquals(0, requestCount.get(), "no request should reach the server");
  }

  @Test
  @DisplayName("SYSTEM_TRUST should explain how to trust a self-signed panel and not retry")
  void systemTrustExplainsSelfSignedFailure() {
    BtSdkConfig config = baseConfig().build();

    try (DefaultBtClient client = new DefaultBtClient(config)) {
      BtApiException exception =
          assertThrows(BtApiException.class, () -> client.execute(new GetTaskCountApi()));
      assertTrue(exception.getMessage().contains("self-signed"), exception.getMessage());
      assertTrue(exception.getMessage().contains("pinnedPublicKeys"), exception.getMessage());
    }
    assertEquals(0, requestCount.get());
  }

  private BtSdkConfig.Builder baseConfig() {
    return BtSdkConfig.builder()
        .baseUrl(baseUrl)
        .apiKey("test-api-key")
        .retryMode(BtSdkConfig.RetryMode.ALL_REQUESTS)
        .retryCount(3)
        .retryInterval(Duration.ofMillis(10));
  }
}
