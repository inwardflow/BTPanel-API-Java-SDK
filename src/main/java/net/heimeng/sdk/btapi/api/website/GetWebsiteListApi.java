package net.heimeng.sdk.btapi.api.website;

import java.util.Set;

/**
 * 获取网站原始列表的 API。
 *
 * <p>用于按分页和筛选条件查询网站列表，并以原始 Map 结构返回，适合在 SDK 尚未完全模型化的场景下直接消费面板字段。
 */
public class GetWebsiteListApi extends AbstractWebsiteMapListQueryApi {

  private static final String ENDPOINT = "data?action=getData&table=sites";
  private static final Set<Integer> ALLOWED_TYPES = Set.of(-1, 0);

  public GetWebsiteListApi() {
    super(ENDPOINT, "website list", "Success", "Failed to fetch website list", "data", true);
  }

  public GetWebsiteListApi setPage(Integer page) {
    addParam("p", page);
    return this;
  }

  public GetWebsiteListApi setLimit(Integer limit) {
    addParam("limit", limit);
    return this;
  }

  public GetWebsiteListApi setType(Integer type) {
    addParam("type", type);
    return this;
  }

  public GetWebsiteListApi setOrder(String order) {
    addParam("order", order);
    return this;
  }

  public GetWebsiteListApi setTojs(String tojs) {
    addParam("tojs", tojs);
    return this;
  }

  public GetWebsiteListApi setSearch(String search) {
    addParam("search", search);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredPositiveIntegerParam("limit")
        && hasOptionalPositiveIntegerParam("p")
        && hasOptionalAllowedType("type")
        && hasOptionalStringParam("order")
        && hasOptionalStringParam("tojs")
        && hasOptionalStringParam("search");
  }

  private boolean hasRequiredPositiveIntegerParam(String paramName) {
    return hasPositiveIntegerParam(paramName);
  }

  private boolean hasOptionalAllowedType(String paramName) {
    if (!params.containsKey(paramName)) {
      return true;
    }
    Object value = params.get(paramName);
    return value instanceof Integer integerValue && ALLOWED_TYPES.contains(integerValue);
  }
}
