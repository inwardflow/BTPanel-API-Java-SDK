package net.heimeng.sdk.btapi.facade;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.heimeng.sdk.btapi.api.ssl.DeleteSslCertificateApi;
import net.heimeng.sdk.btapi.api.ssl.GetSslCertificatesApi;
import net.heimeng.sdk.btapi.api.ssl.GetSslDeployableSitesApi;
import net.heimeng.sdk.btapi.api.ssl.GetSslOrderListApi;
import net.heimeng.sdk.btapi.api.ssl.GetWebsiteSslStatusApi;
import net.heimeng.sdk.btapi.api.ssl.InstallSslCertificateApi;
import net.heimeng.sdk.btapi.api.ssl.SetBatchSslCertificateToSiteApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslBatchDeploymentResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;
import net.heimeng.sdk.btapi.model.ssl.SslDeployableSites;
import net.heimeng.sdk.btapi.model.ssl.SslSiteStatus;

/**
 * Facade entry point for SSL-related capabilities.
 *
 * <p>The current validated developer-signature flow for site deployment is: {@code GetSSL ->
 * GetSiteDomain -> SetBatchCertToSite}.
 */
public final class SslOperations extends AbstractOperations {

  public SslOperations(BtClient client) {
    super(client);
  }

  public BtResult<List<SslCertificate>> list() {
    return execute(new GetSslCertificatesApi());
  }

  public BtResult<SslSiteStatus> getWebsiteStatus(String siteName) {
    return execute(new GetWebsiteSslStatusApi(siteName));
  }

  public BtResult<List<Map<String, Object>>> listOrders(String siteName) {
    return execute(new GetSslOrderListApi(siteName));
  }

  public BtResult<SslDeployableSites> getDeployableSites(List<String> certificateNames) {
    Objects.requireNonNull(certificateNames, "certificateNames cannot be null");
    return execute(new GetSslDeployableSitesApi(certificateNames));
  }

  public BtResult<SslBatchDeploymentResult> deploySavedCertificate(
      String sslHash, String siteName, String certName) {
    return execute(new SetBatchSslCertificateToSiteApi(sslHash, siteName, certName));
  }

  public BtResult<SslBatchDeploymentResult> deploySavedCertificates(
      List<SslBatchDeploymentRequest> requests) {
    Objects.requireNonNull(requests, "requests cannot be null");
    return execute(new SetBatchSslCertificateToSiteApi().setBatch(requests));
  }

  /**
   * Legacy direct PEM installation path.
   *
   * <p>This route is preserved for backward compatibility, but it was not validated on the current
   * BTPanel 9.0.0 panel where the saved-certificate deployment flow is the confirmed route.
   */
  @Deprecated(since = "0.1.0", forRemoval = false)
  public BtResult<Boolean> install(String domain, String key, String cert) {
    return execute(new InstallSslCertificateApi().setDomain(domain).setKey(key).setCert(cert));
  }

  public BtResult<Boolean> delete(int id) {
    return execute(new DeleteSslCertificateApi().setId(id));
  }
}
