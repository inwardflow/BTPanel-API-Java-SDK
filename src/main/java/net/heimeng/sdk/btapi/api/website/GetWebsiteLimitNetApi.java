package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站流量限制配置的 API。
 *
 * <p>成功时通常直接返回配置对象；失败场景下也可能返回带 {@code status/msg} 的包装响应。
 */
public class GetWebsiteLimitNetApi extends AbstractWebsiteMapQueryApi {

  private static final String ENDPOINT = "site?action=GetLimitNet";

  public GetWebsiteLimitNetApi() {
    super(ENDPOINT, "website limit net", "获取成功", "获取失败", false);
  }

  public GetWebsiteLimitNetApi setId(Integer id) {
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
