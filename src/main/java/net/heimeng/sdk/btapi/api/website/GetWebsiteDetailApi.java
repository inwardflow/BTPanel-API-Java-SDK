package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站详情的 API。
 *
 * <p>支持解析带 {@code status/msg} 的包装响应，也兼容直接返回网站详情对象的响应格式。
 *
 * @deprecated 面板 9.0 UI 不调用 {@code site?action=GetSiteStatus}，站点详情来自站点列表。请改用 {@link
 *     net.heimeng.sdk.btapi.facade.WebsiteOperations#listRaw(Integer, Integer, Integer, String,
 *     String)} 按站点名搜索，或用 {@link GetWebsiteConfigApi} 获取目录配置。
 */
@Deprecated(since = "0.2.0", forRemoval = true)
public class GetWebsiteDetailApi extends AbstractWebsiteMapQueryApi {

  private static final String ENDPOINT = "site?action=GetSiteStatus";

  public GetWebsiteDetailApi() {
    super(ENDPOINT, "website detail", "Success", "Failed to fetch website detail", true);
  }

  public GetWebsiteDetailApi setId(Integer id) {
    addParam("id", id);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
