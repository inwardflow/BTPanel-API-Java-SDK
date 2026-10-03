package net.heimeng.sdk.btapi.api.website;

/**
 * 设置站点伪静态规则的 API。
 *
 * <p>规则内容允许为空字符串，用于清空当前配置。
 *
 * @deprecated 面板 9.0 UI 不使用 {@code site?action=setRewrite}，而是用 {@code files?action=SaveFileBody} 写入
 *     {@link WebsiteVhostPaths#rewriteConfig(String)} 指向的文件。请改用 {@link
 *     net.heimeng.sdk.btapi.facade.WebsiteOperations#updateRewriteRules(String, String)}。
 */
@Deprecated(since = "0.2.0", forRemoval = true)
public class SetWebsiteRewriteRulesApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=setRewrite";

  public SetWebsiteRewriteRulesApi() {
    super(ENDPOINT, "伪静态规则设置成功", "伪静态规则设置失败");
  }

  public SetWebsiteRewriteRulesApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public SetWebsiteRewriteRulesApi setName(String name) {
    requireNonBlank(name, "name");
    addParam("name", name);
    return this;
  }

  public SetWebsiteRewriteRulesApi setContent(String content) {
    requireNonNull(content, "content");
    addParam("content", content);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasNonBlankStringParam("name") && hasParam("content");
  }
}
