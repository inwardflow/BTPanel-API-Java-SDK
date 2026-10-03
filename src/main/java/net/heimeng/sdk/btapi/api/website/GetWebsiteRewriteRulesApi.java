package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站伪静态规则的 API。
 *
 * <p>兼容面板直接返回规则文本以及包装在 {@code status/msg/data} 中的响应。
 */
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
