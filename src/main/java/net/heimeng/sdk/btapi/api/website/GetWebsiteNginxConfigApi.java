package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站 Nginx 配置内容的 API。
 *
 * <p>兼容面板直接返回配置文本以及包装在 {@code status/msg/data} 中的响应。
 *
 * @deprecated 面板 9.0 UI 不使用 {@code site?action=getConf}，而是用 {@code files?action=GetFileBody} 读取
 *     {@link WebsiteVhostPaths#nginxConfig(String)} 指向的文件。请改用 {@link
 *     net.heimeng.sdk.btapi.facade.WebsiteOperations#getNginxConfig(String)}。
 */
@Deprecated(since = "0.2.0", forRemoval = true)
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
