package net.heimeng.sdk.btapi.api.ssl;

/**
 * 删除 SSL 证书的 API。
 *
 * <p>当前面板使用 {@code ssl?action=remove_cloud_cert} 删除证书，必需参数是：
 *
 * <ul>
 *   <li>{@code ssl_id}: 证书 ID
 *   <li>{@code local=1}: 表示删除本地证书记录
 * </ul>
 *
 * <p>注意：面板对 {@code ssl_hash} 参数会返回“删除成功”，但证书实际仍在证书夹中（BTPanel 9.0.0 实测），因此只能用 {@code ssl_id}。
 *
 * <p>历史调用里可能会同时传入域名，但当前路由实际只依赖证书 ID，因此 {@link #setDomain(String)} 仅作为兼容方法保留。
 */
public class DeleteSslCertificateApi extends AbstractSslBooleanApi {

  private static final String ENDPOINT = "ssl?action=remove_cloud_cert";

  public DeleteSslCertificateApi() {
    super(ENDPOINT, "SSL certificate deleted successfully", "Failed to delete SSL certificate");
    addParam("local", 1);
  }

  public DeleteSslCertificateApi(int id) {
    this();
    setId(id);
  }

  /** 设置要删除的证书 ID。 */
  public DeleteSslCertificateApi setId(int id) {
    if (id <= 0) {
      throw new IllegalArgumentException("id must be positive");
    }
    addParam("ssl_id", id);
    return this;
  }

  /** 当前删除路由不消费域名参数，保留此方法仅用于兼容旧调用代码。 */
  public DeleteSslCertificateApi setDomain(String domain) {
    return this;
  }

  @Override
  protected boolean validateParams() {
    Object id = params.get("ssl_id");
    return id instanceof Number numberValue && numberValue.intValue() > 0;
  }
}
