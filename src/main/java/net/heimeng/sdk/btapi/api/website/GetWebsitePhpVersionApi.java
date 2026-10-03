package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站 PHP 版本的 API。
 *
 * <p>兼容面板直接返回 PHP 版本文本以及包装在 {@code status/msg/data} 中的响应。
 */
public class GetWebsitePhpVersionApi extends AbstractWebsiteTextQueryApi {

  private static final String ENDPOINT = "site?action=getPhpVersion";

  public GetWebsitePhpVersionApi() {
    super(ENDPOINT, "获取成功", "获取失败");
  }

  public GetWebsitePhpVersionApi setId(Integer id) {
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
