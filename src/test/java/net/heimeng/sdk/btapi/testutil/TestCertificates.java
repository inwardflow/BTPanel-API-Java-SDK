package net.heimeng.sdk.btapi.testutil;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/** 生成仅供测试使用的一次性自签名证书，避免在仓库中保存证书或私钥。 */
public final class TestCertificates {

  private TestCertificates() {}

  /** 自签名证书及其私钥。 */
  public record SelfSigned(KeyPair keyPair, X509Certificate certificate) {

    /** PEM 格式证书（{@code BEGIN CERTIFICATE}）。 */
    public String certificatePem() {
      try {
        return pem("CERTIFICATE", certificate.getEncoded());
      } catch (Exception exception) {
        throw new IllegalStateException(exception);
      }
    }

    /** PKCS#8 PEM 格式私钥（{@code BEGIN PRIVATE KEY}），与宝塔 SetSSL 的要求一致。 */
    public String privateKeyPem() {
      return pem("PRIVATE KEY", keyPair.getPrivate().getEncoded());
    }

    /** 包含该证书和私钥的内存 PKCS12 密钥库，供本地 HTTPS 测试服务器使用。 */
    public KeyStore keyStore(char[] password) {
      try {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry(
            "server", keyPair.getPrivate(), password, new Certificate[] {certificate});
        return keyStore;
      } catch (Exception exception) {
        throw new IllegalStateException(exception);
      }
    }
  }

  /**
   * 生成 RSA 2048 自签名证书。
   *
   * @param commonName 证书 CN，同时写入 SAN
   * @param validity 有效期
   * @param ipAddress 若为 {@code true}，SAN 写为 IP 地址，否则写为 DNS 名称
   */
  public static SelfSigned generate(String commonName, Duration validity, boolean ipAddress) {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048, new SecureRandom());
      KeyPair keyPair = generator.generateKeyPair();

      Instant now = Instant.now();
      X500Name subject = new X500Name("CN=" + commonName);
      X509v3CertificateBuilder builder =
          new JcaX509v3CertificateBuilder(
              subject,
              new BigInteger(64, new SecureRandom()),
              Date.from(now.minus(Duration.ofMinutes(5))),
              Date.from(now.plus(validity)),
              subject,
              keyPair.getPublic());
      builder.addExtension(
          Extension.subjectAlternativeName,
          false,
          new GeneralNames(
              new GeneralName(
                  ipAddress ? GeneralName.iPAddress : GeneralName.dNSName, commonName)));

      ContentSigner signer =
          new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
      X509Certificate certificate =
          new JcaX509CertificateConverter().getCertificate(builder.build(signer));
      return new SelfSigned(keyPair, certificate);
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to generate test certificate", exception);
    }
  }

  private static String pem(String type, byte[] der) {
    String body = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der);
    return "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----\n";
  }
}
