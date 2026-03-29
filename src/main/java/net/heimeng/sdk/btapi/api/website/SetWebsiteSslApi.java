package net.heimeng.sdk.btapi.api.website;

/** 设置站点 SSL 证书的 API。 */
public class SetWebsiteSslApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SetSSL";

  public SetWebsiteSslApi() {
    super(ENDPOINT, "SSL 证书设置成功", "SSL 证书设置失败");
  }

  public SetWebsiteSslApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public SetWebsiteSslApi setDomain(String domain) {
    requireNonBlank(domain, "domain");
    addParam("domain", domain);
    return this;
  }

  public SetWebsiteSslApi setCert(String cert) {
    requireNonBlank(cert, "cert");
    addParam("cert", cert);
    return this;
  }

  public SetWebsiteSslApi setKey(String key) {
    requireNonBlank(key, "key");
    addParam("key", key);
    return this;
  }

  public SetWebsiteSslApi setForceHttps(Boolean forceHttps) {
    putOptionalBooleanFlag("force_https", forceHttps);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id")
        && hasRequiredParams("domain", "cert", "key")
        && hasOptionalBooleanFlagIntParam("force_https");
  }
}
