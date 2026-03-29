package net.heimeng.sdk.btapi.api.website;

import java.util.ArrayList;
import java.util.List;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.WebsiteType;

/**
 * 获取网站分类列表的 API。
 *
 * <p>兼容面板直接返回数组以及带 {@code status/msg/data} 包装的响应。
 */
public class GetWebsiteTypesApi extends BaseBtApi<BtResult<List<WebsiteType>>> {

  private static final String ENDPOINT = "site?action=get_site_types";

  public GetWebsiteTypesApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  @Override
  public BtResult<List<WebsiteType>> parseResponse(String response) {
    JSON json = WebsiteApiResponseSupport.parseJsonResponse(response, "website types");
    if (json instanceof JSONArray jsonArray) {
      return WebsiteApiResponseSupport.successListResult(parseTypes(jsonArray), "Success");
    }
    if (!(json instanceof JSONObject jsonObject)) {
      throw new BtApiException("Website types response must be a JSON object or array");
    }
    if (jsonObject.containsKey("status") && !jsonObject.getBool("status", false)) {
      return WebsiteApiResponseSupport.failureListResult(
          jsonObject.getStr("msg", "Failed to fetch website types"));
    }

    JSONArray dataArray = jsonObject.getJSONArray("data");
    if (dataArray == null) {
      throw new BtApiException("Website types response is missing required data array");
    }

    return WebsiteApiResponseSupport.successListResult(
        parseTypes(dataArray), jsonObject.getStr("msg", "Success"));
  }

  private List<WebsiteType> parseTypes(JSONArray jsonArray) {
    List<WebsiteType> websiteTypes = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject typeJson = jsonArray.getJSONObject(i);
      if (typeJson == null) {
        continue;
      }

      WebsiteType websiteType = new WebsiteType();
      websiteType.setId(typeJson.getInt("id", 0));
      websiteType.setName(typeJson.getStr("name", ""));
      websiteTypes.add(websiteType);
    }
    return List.copyOf(websiteTypes);
  }
}
