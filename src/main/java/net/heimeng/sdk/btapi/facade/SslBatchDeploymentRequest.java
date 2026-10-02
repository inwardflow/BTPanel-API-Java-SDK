package net.heimeng.sdk.btapi.facade;

/**
 * Immutable request model for deploying a saved certificate to a site.
 *
 * @param sslHash certificate hash from {@code ssl?action=get_cert_list}
 * @param siteName panel site name
 * @param certName certificate display name or subject name
 */
public record SslBatchDeploymentRequest(String sslHash, String siteName, String certName) {

  public SslBatchDeploymentRequest {
    sslHash = requireNonBlank(sslHash, "sslHash");
    siteName = requireNonBlank(siteName, "siteName");
    certName = requireNonBlank(certName, "certName");
  }

  public static SslBatchDeploymentRequest of(String sslHash, String siteName, String certName) {
    return new SslBatchDeploymentRequest(sslHash, siteName, certName);
  }

  private static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
    return value;
  }
}
