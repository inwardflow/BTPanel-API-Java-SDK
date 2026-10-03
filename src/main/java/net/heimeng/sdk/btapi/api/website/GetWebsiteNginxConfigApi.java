package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站 Nginx 配置内容的 API。
 *
 * <p>兼容面板直接返回配置文本以及包装在 {@code status/msg/data} 中的响应。
 */
public class GetWebsiteNginxConfigApi extends AbstractWebsiteTextQueryApi {

  private static final String ENDPOINT = "site?action=getConf";

  public GetWebsiteNginxConfigApi() {
    super(ENDPOINT, "获取成功", "获取失败");
  }

  public GetWebsiteNginxConfigApi setId(Integer id) {
    addParam("id", id);
    return this;
  }

  public GetWebsiteNginxConfigApi setDomain(String domain) {
    addParam("domain", domain);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasNonBlankStringParam("domain");
  }
}
