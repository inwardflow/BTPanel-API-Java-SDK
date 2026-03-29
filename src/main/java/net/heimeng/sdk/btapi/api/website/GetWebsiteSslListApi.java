package net.heimeng.sdk.btapi.api.website;

/**
 * 获取SSL证书列表API实现
 *
 * <p>用于获取宝塔面板中指定网站的SSL证书列表。
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteSslListApi extends AbstractWebsiteMapListQueryApi {

  /** API端点路径 */
  private static final String ENDPOINT = "site?action=GetSSLCertList";

  /** 构造函数，创建一个新的GetWebsiteSslListApi实例 */
  public GetWebsiteSslListApi() {
    super(ENDPOINT, "website SSL list", "获取成功", "获取失败", "certs", false);
  }

  /**
   * 设置网站ID
   *
   * @param id 网站ID
   * @return 当前API实例，支持链式调用
   */
  public GetWebsiteSslListApi setId(Integer id) {
    addParam("id", id);
    return this;
  }

  /**
   * 验证请求参数是否有效
   *
   * @return 如果请求参数有效则返回true，否则返回false
   */
  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id");
  }
}
