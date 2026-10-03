package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站伪静态规则的 API。
 *
 * <p>兼容面板直接返回规则文本以及包装在 {@code status/msg/data} 中的响应。
 *
 * @deprecated 面板 9.0 UI 不使用 {@code site?action=getRewrite}，而是用 {@code files?action=GetFileBody} 读取
 *     {@link WebsiteVhostPaths#rewriteConfig(String)} 指向的文件。请改用 {@link
 *     net.heimeng.sdk.btapi.facade.WebsiteOperations#getRewriteRules(String)}。
 */
@Deprecated(since = "0.2.0", forRemoval = true)
public class GetWebsiteRewriteRulesApi extends AbstractWebsiteTextQueryApi {

  private static final String ENDPOINT = "site?action=getRewrite";

  public GetWebsiteRewriteRulesApi() {
    super(ENDPOINT, "获取成功", "获取失败");
  }

  public GetWebsiteRewriteRulesApi setId(Integer id) {
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
