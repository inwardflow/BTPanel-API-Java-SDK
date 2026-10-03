package net.heimeng.sdk.btapi.facade;

import java.util.Objects;

/**
 * Website SSL installation options.
 *
 * @param domain website domain name
 * @param certificate PEM certificate content
 * @param privateKey PEM private key content
 * @param forceHttps whether HTTPS redirection should be enabled
 */
public record WebsiteSslCertificateOptions(
    String domain, String certificate, String privateKey, Boolean forceHttps) {

  public WebsiteSslCertificateOptions {
    requireNonBlank(domain, "domain");
    requireNonBlank(certificate, "certificate");
    requireNonBlank(privateKey, "privateKey");
  }

  private static void requireNonBlank(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName + " cannot be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
  }
}
