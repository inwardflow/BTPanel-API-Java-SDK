package net.heimeng.sdk.btapi.api.ssl;

/**
 * 安装站点 SSL 证书的 API。
 *
 * <p>当前面板通过 {@code site?action=SetSSL} 为指定站点部署证书：
 *
 * <ul>
 *   <li>{@code siteName}: 站点名称，而不是证书域名
 *   <li>{@code key}: 私钥 PEM
 *   <li>{@code csr}: 证书 PEM
 * </ul>
 *
 * <p>部分历史参数如 {@code force_https}、{@code auto_renew} 在当前路由下不再生效，因此这里保留 链式方法但不再发送这两个字段。
 *
 * <p>面板会同时把证书保存到证书夹，之后可通过 {@code get_cert_list} 查到并部署到其他站点（已在 BTPanel 9.0.0 上验证）。
 *
 * @see <a href="https://docs.bt.cn/api/site/actions/">宝塔官方文档：网站管理 SetSSL</a>
 */
public class InstallSslCertificateApi extends AbstractSslBooleanApi {

  private static final String ENDPOINT = "site?action=SetSSL";

  public InstallSslCertificateApi() {
    super(ENDPOINT, "SSL certificate installed successfully", "Failed to install SSL certificate");
    setForceHttps(false);
    setAutoRenew(false);
  }

  public InstallSslCertificateApi(String domain, String key, String cert) {
    this();
    setDomain(domain);
    setKey(key);
    setCert(cert);
  }

  /**
   * 设置要部署证书的站点名称。
   *
   * <p>这里对应的是面板中的站点标识 {@code siteName}，并不要求和证书 CN / SAN 完全一致。
   */
  public InstallSslCertificateApi setDomain(String domain) {
    requireNonBlank(domain, "domain");
    addParam("siteName", domain);
    return this;
  }

  /** 设置 PEM 格式的私钥内容。 */
  public InstallSslCertificateApi setKey(String key) {
    requireNonBlank(key, "key");
    addParam("key", key);
    return this;
  }

  /** 设置 PEM 格式的证书内容，当前面板字段名为 {@code csr}。 */
  public InstallSslCertificateApi setCert(String cert) {
    requireNonBlank(cert, "cert");
    addParam("csr", cert);
    return this;
  }

  /** 当前路由不消费该参数，保留此方法仅用于兼容旧调用代码。 */
  public InstallSslCertificateApi setForceHttps(boolean forceHttps) {
    removeParam("force_https");
    return this;
  }

  /** 当前路由不消费该参数，保留此方法仅用于兼容旧调用代码。 */
  public InstallSslCertificateApi setAutoRenew(boolean autoRenew) {
    removeParam("auto_renew");
    return this;
  }

  /** 可选地附带中间证书 / CA 链。 */
  public InstallSslCertificateApi setCa(String ca) {
    if (ca != null && !ca.isBlank()) {
      addParam("ca", ca);
    } else {
      removeParam("ca");
    }
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("siteName", "key", "csr");
  }

  private void requireNonBlank(String value, String paramName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(paramName + " cannot be blank");
    }
  }
}
