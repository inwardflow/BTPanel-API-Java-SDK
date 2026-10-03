package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站PHP扩展列表API实现
 *
 * <p>用于获取宝塔面板中指定网站PHP版本的扩展列表。
 *
 * @author InwardFlow
 * @since 2.0.0
 * @deprecated 面板 9.0 没有按站点查询 PHP 扩展的接口，{@code site?action=GetPHPModules} 不在 UI 中。PHP 扩展属于 PHP
 *     版本，请改用 {@link GetPhpRuntimeConfigApi}。
 */
@Deprecated(since = "0.2.0", forRemoval = true)
public class GetWebsitePhpExtensionsApi extends AbstractWebsiteMapListQueryApi {

  /** API端点路径 */
  private static final String ENDPOINT = "site?action=GetPHPModules";

  /** 构造函数，创建一个新的GetWebsitePhpExtensionsApi实例 */
  public GetWebsitePhpExtensionsApi() {
    super(ENDPOINT, "website PHP extensions", "获取成功", "获取失败", "data", false);
  }

  /**
   * 设置网站ID
   *
   * @param id 网站ID
   * @return 当前API实例，支持链式调用
   */
  public GetWebsitePhpExtensionsApi setId(Integer id) {
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
