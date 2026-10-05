package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 获取网站 PHP 版本的 API。
 *
 * <p>对应面板 9.0 站点设置“PHP”页签打开时的请求：{@code site?action=GetSitePHPVersion}，按站点名 {@code siteName} 查询，返回
 * {@code {"phpversion":"81","tomcat":-1,...}}。结果数据为 {@code phpversion} 字段，例如 {@code 81}；纯静态站点为
 * {@code 00}。
 *
 * <p>同时兼容包装在 {@code status/msg/data} 中的响应。
 */
public class GetWebsitePhpVersionApi extends AbstractWebsiteTextQueryApi {

  private static final String ENDPOINT = "site?action=GetSitePHPVersion";

  public GetWebsitePhpVersionApi() {
    super(ENDPOINT, "获取成功", "获取失败");
  }

  /**
   * 设置站点名（主域名）。
   *
   * @param siteName 站点名，例如 {@code example.com}
   * @return 当前 API 实例
   */
  public GetWebsitePhpVersionApi setSiteName(String siteName) {
    addParam("siteName", siteName);
    return this;
  }

  /**
   * 设置站点 ID。
   *
   * @param id 站点 ID
   * @return 当前 API 实例
   * @deprecated 面板 9.0 按站点名查询 PHP 版本，旧的 {@code site?action=getPhpVersion} 按 ID 查询的接口不在 9.0 UI 中。只设置
   *     ID 而不设置 {@code siteName} 时，客户端在发送请求前抛出 {@link
   *     net.heimeng.sdk.btapi.exception.BtApiException}（错误代码 {@code INVALID_PARAMETERS}）。请改用 {@link
   *     #setSiteName(String)}。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public GetWebsitePhpVersionApi setId(Integer id) {
    addParam("id", id);
    return this;
  }

  @Override
  public BtResult<String> parseResponse(String response) {
    if (response != null && JSONUtil.isTypeJSONObject(response.trim())) {
      JSONObject jsonObject = JSONUtil.parseObj(response.trim());
      if (jsonObject.containsKey("phpversion") && !jsonObject.containsKey("status")) {
        return WebsiteApiResponseSupport.successStringResult(
            jsonObject.getStr("phpversion"), "获取成功");
      }
    }
    return super.parseResponse(response);
  }

  @Override
  protected boolean validateParams() {
    return hasNonBlankStringParam("siteName");
  }
}
