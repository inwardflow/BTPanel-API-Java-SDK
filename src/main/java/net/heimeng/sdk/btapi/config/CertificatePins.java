package net.heimeng.sdk.btapi.config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Objects;

/**
 * 证书公钥指纹（pin）工具，格式与 OkHttp {@code CertificatePinner}、curl {@code --pinnedpubkey} 一致： {@code
 * sha256/<base64(SHA-256(SubjectPublicKeyInfo))>}。
 */
public final class CertificatePins {

  public static final String SHA256_PREFIX = "sha256/";
  private static final int SHA256_LENGTH = 32;

  private CertificatePins() {}

  /**
   * 计算证书公钥的 SHA-256 指纹。
   *
   * @param certificate 服务端证书
   * @return {@code sha256/<base64>} 格式的指纹
   */
  public static String sha256(Certificate certificate) {
    Objects.requireNonNull(certificate, "certificate cannot be null");
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(certificate.getPublicKey().getEncoded());
      return SHA256_PREFIX + Base64.getEncoder().encodeToString(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }

  /**
   * 校验指纹格式并返回规范化结果。
   *
   * @param pin {@code sha256/<base64>} 格式的指纹
   * @return 去掉首尾空白后的指纹
   * @throws IllegalArgumentException 格式不正确
   */
  static String requireValid(String pin) {
    if (pin == null || pin.isBlank()) {
      throw new IllegalArgumentException("Pinned public key cannot be blank");
    }
    String trimmed = pin.trim();
    if (!trimmed.startsWith(SHA256_PREFIX)) {
      throw new IllegalArgumentException(
          "Pinned public key must start with '" + SHA256_PREFIX + "': " + trimmed);
    }
    byte[] decoded;
    try {
      decoded = Base64.getDecoder().decode(trimmed.substring(SHA256_PREFIX.length()));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Pinned public key is not valid base64: " + trimmed);
    }
    if (decoded.length != SHA256_LENGTH) {
      throw new IllegalArgumentException(
          "Pinned public key must be a base64 SHA-256 digest (32 bytes): " + trimmed);
    }
    return trimmed;
  }
}
