package net.heimeng.sdk.btapi.model.ssl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SslCertificate 模型测试")
class SslCertificateTest {

  @Test
  @DisplayName("应根据状态判断证书是否有效")
  void detectsCertificateStates() {
    SslCertificate validCertificate = new SslCertificate();
    validCertificate.setStatus("valid");
    assertTrue(validCertificate.isValid());
    assertFalse(validCertificate.isExpired());
    assertFalse(validCertificate.isExpiringSoon());

    SslCertificate expiredCertificate = new SslCertificate();
    expiredCertificate.setStatus("expired");
    assertTrue(expiredCertificate.isExpired());

    SslCertificate expiringSoonCertificate = new SslCertificate();
    expiringSoonCertificate.setStatus("expiring_soon");
    assertTrue(expiringSoonCertificate.isExpiringSoon());
  }

  @Test
  @DisplayName("应格式化证书有效期")
  void formatsValidityPeriod() {
    SslCertificate certificate = new SslCertificate();
    // %tF formats in the JVM default zone, so build fixtures in that zone to stay TZ-independent.
    certificate.setValidFrom(startOfDay(LocalDate.of(2024, 1, 1)));
    certificate.setValidTo(startOfDay(LocalDate.of(2024, 12, 31)));

    assertEquals("2024-01-01 至 2024-12-31", certificate.getFormattedValidityPeriod());
  }

  @Test
  @DisplayName("缺少有效期时应返回未知")
  void returnsUnknownValidityPeriodWhenDatesMissing() {
    SslCertificate certificate = new SslCertificate();

    assertEquals("未知", certificate.getFormattedValidityPeriod());
  }

  private static Date startOfDay(LocalDate date) {
    return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }
}
