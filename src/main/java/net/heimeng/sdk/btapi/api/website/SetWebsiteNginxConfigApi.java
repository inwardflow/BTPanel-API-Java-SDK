package net.heimeng.sdk.btapi.api.website;

/**
 * 设置站点 Nginx 配置的 API。
 *
 * <p>配置内容允许为空字符串，用于清空当前配置。
 *
 * @deprecated 面板 9.0 UI 不使用 {@code site?action=setConf}，而是用 {@code files?action=SaveFileBody} 写入
 *     {@link WebsiteVhostPaths#nginxConfig(String)} 指向的文件。请改用 {@link
 *     net.heimeng.sdk.btapi.facade.WebsiteOperations#updateNginxConfig(String, String)}。
 */
@Deprecated(since = "0.2.0", forRemoval = true)
public class SetWebsiteNginxConfigApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=setConf";

  public SetWebsiteNginxConfigApi() {
    super(ENDPOINT, "Nginx 配置设置成功", "Nginx 配置设置失败");
  }

  public SetWebsiteNginxConfigApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public SetWebsiteNginxConfigApi setDomain(String domain) {
    requireNonBlank(domain, "domain");
    addParam("domain", domain);
    return this;
  }

  public SetWebsiteNginxConfigApi setContent(String content) {
    requireNonNull(content, "content");
    addParam("content", content);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasNonBlankStringParam("domain") && hasParam("content");
  }
}
