package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站根目录的 API。
 *
 * <p>兼容面板直接返回根目录文本以及包装在 {@code status/msg/data} 中的响应。
 */
public class GetWebsiteRootPathApi extends AbstractWebsiteTextQueryApi {

  private static final String ENDPOINT = "data?action=getKey&table=sites&key=path";

  public GetWebsiteRootPathApi() {
    super(ENDPOINT, "获取成功", "获取失败");
  }

  public GetWebsiteRootPathApi setId(Integer id) {
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
