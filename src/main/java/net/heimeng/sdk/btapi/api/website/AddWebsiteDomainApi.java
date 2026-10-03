package net.heimeng.sdk.btapi.api.website;

/**
 * 为站点添加域名的 API。
 *
 * <p>用于向指定站点追加新的域名绑定。
 */
public class AddWebsiteDomainApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=AddDomain";

  public AddWebsiteDomainApi() {
    super(ENDPOINT, "域名添加成功", "域名添加失败");
  }

  public AddWebsiteDomainApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public AddWebsiteDomainApi setWebname(String webname) {
    requireNonBlank(webname, "webname");
    addParam("webname", webname);
    return this;
  }

  public AddWebsiteDomainApi setDomain(String domain) {
    requireNonBlank(domain, "domain");
    addParam("domain", domain);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasRequiredParams("webname", "domain");
  }
}
