package net.heimeng.sdk.btapi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.testutil.TestCertificates;

@DisplayName("Certificate pinning configuration tests")
class CertificatePinsConfigTest {

  private static final String VALID_PIN = "sha256/AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";

  @Test
  @DisplayName("pinnedPublicKeys should switch to PINNED_PUBLIC_KEY and de-duplicate pins")
  void configuresPinnedMode() {
    BtSdkConfig config = builder().pinnedPublicKeys(" " + VALID_PIN + " ", VALID_PIN).build();

    assertEquals(BtSdkConfig.SslMode.PINNED_PUBLIC_KEY, config.getSslMode());
    assertEquals(List.of(VALID_PIN), config.getPinnedPublicKeys());
    assertTrue(config.toString().contains(VALID_PIN));
  }

  @Test
  @DisplayName("invalid pins should be rejected at build time")
  void rejectsInvalidPins() {
    assertThrows(IllegalArgumentException.class, () -> builder().pinnedPublicKeys("abc").build());
    assertThrows(
        IllegalArgumentException.class, () -> builder().pinnedPublicKeys("sha256/!!").build());
    assertThrows(
        IllegalArgumentException.class, () -> builder().pinnedPublicKeys("sha256/AAAA").build());
    assertThrows(
        IllegalArgumentException.class,
        () -> builder().sslMode(BtSdkConfig.SslMode.PINNED_PUBLIC_KEY).build());
  }

  @Test
  @DisplayName("switching to another SSL mode should clear configured pins")
  void switchingModeClearsPins() {
    BtSdkConfig config =
        builder().pinnedPublicKeys(VALID_PIN).sslMode(BtSdkConfig.SslMode.SYSTEM_TRUST).build();

    assertEquals(List.of(), config.getPinnedPublicKeys());
  }

  @Test
  @DisplayName("sha256 should hash the certificate's SubjectPublicKeyInfo")
  void computesSpkiPin() {
    TestCertificates.SelfSigned certificate =
        TestCertificates.generate("pin.example.com", Duration.ofDays(1), false);

    String pin = CertificatePins.sha256(certificate.certificate());

    assertTrue(pin.startsWith("sha256/"));
    assertEquals(pin, CertificatePins.requireValid(pin));
  }

  @Test
  @DisplayName("maxRetryInterval cannot be shorter than retryInterval")
  void validatesMaxRetryInterval() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            builder()
                .retryInterval(Duration.ofSeconds(5))
                .maxRetryInterval(Duration.ofSeconds(1))
                .build());
  }

  private static BtSdkConfig.Builder builder() {
    return BtSdkConfig.builder().baseUrl("https://127.0.0.1:8888").apiKey("test-api-key");
  }
}
