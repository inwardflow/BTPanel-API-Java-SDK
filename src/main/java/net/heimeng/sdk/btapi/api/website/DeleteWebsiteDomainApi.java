package net.heimeng.sdk.btapi.api.website;

/**
 * 删除站点域名的 API。
 *
 * <p>用于移除指定站点中的单个域名绑定。
 */
public class DeleteWebsiteDomainApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=DelDomain";

  public DeleteWebsiteDomainApi() {
    super(ENDPOINT, "域名删除成功", "域名删除失败");
  }

  public DeleteWebsiteDomainApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public DeleteWebsiteDomainApi setWebname(String webname) {
    requireNonBlank(webname, "webname");
    addParam("webname", webname);
    return this;
  }

  public DeleteWebsiteDomainApi setDomain(String domain) {
    requireNonBlank(domain, "domain");
    addParam("domain", domain);
    return this;
  }

  public DeleteWebsiteDomainApi setPort(Integer port) {
    requirePositiveInteger(port, "port");
    addParam("port", port);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id")
        && hasRequiredParams("webname", "domain")
        && hasPositiveIntegerParam("port");
  }
}
