package net.heimeng.sdk.btapi.api.website;

import java.util.ArrayList;
import java.util.List;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.PhpVersion;

/**
 * 获取已安装 PHP 版本列表的 API。
 *
 * <p>兼容面板直接返回数组以及带 {@code status/msg/data} 包装的响应。
 */
public class GetPhpVersionsApi extends BaseBtApi<BtResult<List<PhpVersion>>> {

  private static final String ENDPOINT = "site?action=GetPHPVersion";

  public GetPhpVersionsApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  @Override
  public BtResult<List<PhpVersion>> parseResponse(String response) {
    JSON json = WebsiteApiResponseSupport.parseJsonResponse(response, "php versions");
    if (json instanceof JSONArray jsonArray) {
      return WebsiteApiResponseSupport.successListResult(parseVersions(jsonArray), "Success");
    }
    if (!(json instanceof JSONObject jsonObject)) {
      throw new BtApiException("PHP versions response must be a JSON object or array");
    }
    if (jsonObject.containsKey("status") && !jsonObject.getBool("status", false)) {
      return WebsiteApiResponseSupport.failureListResult(
          jsonObject.getStr("msg", "Failed to fetch PHP versions"));
    }

    JSONArray dataArray = jsonObject.getJSONArray("data");
    if (dataArray == null) {
      throw new BtApiException("PHP versions response is missing required data array");
    }

    return WebsiteApiResponseSupport.successListResult(
        parseVersions(dataArray), jsonObject.getStr("msg", "Success"));
  }

  private List<PhpVersion> parseVersions(JSONArray jsonArray) {
    List<PhpVersion> phpVersions = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject versionJson = jsonArray.getJSONObject(i);
      if (versionJson == null) {
        continue;
      }

      PhpVersion phpVersion = new PhpVersion();
      phpVersion.setVersion(versionJson.getStr("version", ""));
      phpVersion.setName(versionJson.getStr("name", ""));
      phpVersions.add(phpVersion);
    }
    return List.copyOf(phpVersions);
  }
}
