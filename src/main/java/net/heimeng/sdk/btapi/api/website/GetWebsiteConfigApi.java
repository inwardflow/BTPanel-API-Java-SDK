package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站配置的 API。
 *
 * <p>根据本地文档，该接口成功时通常直接返回配置对象；失败场景下也可能返回带 {@code status/msg} 的包装响应。
 */
public class GetWebsiteConfigApi extends AbstractWebsiteMapQueryApi {

  private static final String ENDPOINT = "site?action=GetDirUserINI";

  public GetWebsiteConfigApi() {
    super(ENDPOINT, "website config", "获取成功", "获取失败", false);
  }

  public GetWebsiteConfigApi setId(Integer id) {
    addParam("id", id);
    return this;
  }

  public GetWebsiteConfigApi setPath(String path) {
    addParam("path", path);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasNonBlankStringParam("path");
  }
}
