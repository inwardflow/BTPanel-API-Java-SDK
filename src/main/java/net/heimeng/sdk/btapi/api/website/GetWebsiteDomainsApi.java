package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站域名列表的 API。
 *
 * <p>兼容面板直接返回数组和包装在 {@code data} 字段中的两种返回格式。
 */
public class GetWebsiteDomainsApi extends AbstractWebsiteMapListQueryApi {

  private static final String ENDPOINT = "data?action=getData&table=domain";

  public GetWebsiteDomainsApi() {
    super(ENDPOINT, "website domains", "Success", "Failed to fetch website domains", "data", true);
    addParam("list", true);
  }

  public GetWebsiteDomainsApi setSiteId(Integer siteId) {
    addParam("search", siteId);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("search") && Boolean.TRUE.equals(params.get("list"));
  }
}
