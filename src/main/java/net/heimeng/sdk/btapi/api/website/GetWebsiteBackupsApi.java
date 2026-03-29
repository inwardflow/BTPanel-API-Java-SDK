package net.heimeng.sdk.btapi.api.website;

/**
 * 获取网站备份列表API实现
 *
 * <p>用于获取宝塔面板中指定网站的备份列表。
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteBackupsApi extends AbstractWebsiteMapListQueryApi {

  /** API端点路径 */
  private static final String ENDPOINT = "data?action=getData&table=backup";

  /** 构造函数，创建一个新的GetWebsiteBackupsApi实例 */
  public GetWebsiteBackupsApi() {
    super(ENDPOINT, "website backups", "Success", "Failed to fetch website backups", "data", false);
    // 设置默认参数
    addParam("p", 1);
    addParam("limit", 5);
    addParam("type", 0);
  }

  /**
   * 设置当前分页
   *
   * @param page 当前分页
   * @return 当前API实例，支持链式调用
   */
  public GetWebsiteBackupsApi setPage(Integer page) {
    addParam("p", page);
    return this;
  }

  /**
   * 设置每页取回的数据行数
   *
   * @param limit 数据行数
   * @return 当前API实例，支持链式调用
   */
  public GetWebsiteBackupsApi setLimit(Integer limit) {
    addParam("limit", limit);
    return this;
  }

  /**
   * 设置网站ID
   *
   * @param siteId 网站ID
   * @return 当前API实例，支持链式调用
   */
  public GetWebsiteBackupsApi setSiteId(Integer siteId) {
    addParam("search", siteId);
    return this;
  }

  /**
   * 设置分页JS回调
   *
   * @param callback JS回调函数名
   * @return 当前API实例，支持链式调用
   */
  public GetWebsiteBackupsApi setCallback(String callback) {
    addParam("tojs", callback);
    return this;
  }

  /**
   * 验证请求参数是否有效
   *
   * @return 如果请求参数有效则返回true，否则返回false
   */
  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("limit") && hasPositiveIntegerParam("search");
  }
}
