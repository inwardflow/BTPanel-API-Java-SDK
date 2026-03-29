package net.heimeng.sdk.btapi.api.website;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.WebsiteInfo;

/**
 * 获取网站列表API实现
 *
 * <p>用于获取宝塔面板中所有网站的列表信息，支持分页查询。
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsitesApi extends BaseBtApi<BtResult<List<WebsiteInfo>>> {

  /** API端点路径 */
  private static final String ENDPOINT = "data?action=getData&table=sites";

  /** 日期格式解析器 */
  private static final SimpleDateFormat DATE_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

  /**
   * 构造函数，创建一个新的GetWebsitesApi实例
   *
   * @param page 页码，从1开始
   * @param limit 每页记录数
   */
  public GetWebsitesApi(int page, int limit) {
    super(ENDPOINT, HttpMethod.POST);
    addParam("p", page);
    addParam("limit", limit);
  }

  /** 构造函数，创建一个新的GetWebsitesApi实例，使用默认分页参数 */
  public GetWebsitesApi() {
    this(1, 10);
  }

  /**
   * 设置页码
   *
   * @param page 页码，从1开始
   * @return 当前API实例，支持链式调用
   */
  public GetWebsitesApi setPage(int page) {
    addParam("p", page);
    return this;
  }

  /**
   * 设置每页记录数
   *
   * @param limit 每页记录数
   * @return 当前API实例，支持链式调用
   */
  public GetWebsitesApi setLimit(int limit) {
    addParam("limit", limit);
    return this;
  }

  /**
   * 解析API响应字符串为BtResult<List<WebsiteInfo>>对象
   *
   * @param response API响应字符串
   * @return BtResult<List<WebsiteInfo>>对象
   * @throws BtApiException 当解析失败时抛出
   */
  @Override
  public BtResult<List<WebsiteInfo>> parseResponse(String response) {
    if (response == null || response.isEmpty()) {
      throw new BtApiException("Empty response received");
    }

    try {
      if (!JSONUtil.isTypeJSON(response)) {
        throw new BtApiException("Invalid JSON response: " + response);
      }

      JSONObject json = JSONUtil.parseObj(response);
      if (!json.containsKey("data")) {
        throw new BtApiException("Missing required data field in websites response");
      }

      BtResult<List<WebsiteInfo>> result = new BtResult<>();
      result.setStatus(true);
      result.setMsg(json.getStr("msg", "Success"));

      JSONArray dataArray = json.getJSONArray("data");
      if (dataArray == null) {
        throw new BtApiException("data field is not a valid array");
      }

      List<WebsiteInfo> websites = new ArrayList<>(dataArray.size());
      for (int i = 0; i < dataArray.size(); i++) {
        JSONObject websiteJson = dataArray.getJSONObject(i);
        if (websiteJson == null) {
          continue;
        }

        WebsiteInfo website = new WebsiteInfo();
        website.setId(parseLong(websiteJson, "id"));
        website.setName(websiteJson.get("name", String.class));
        website.setDomain(websiteJson.get("name", String.class));
        website.setPath(websiteJson.get("path", String.class));
        website.setType(websiteJson.get("project_type", String.class));
        website.setStatus(parseInteger(websiteJson, "status"));
        website.setSsl(parseInteger(websiteJson, "ssl"));
        website.setCreateTime(parseTimestamp(websiteJson.get("addtime", String.class)));
        websites.add(website);
      }

      result.setData(websites);
      return result;
    } catch (JSONException exception) {
      throw new BtApiException("Invalid JSON response: " + response, exception);
    } catch (Exception e) {
      if (e instanceof BtApiException) {
        throw (BtApiException) e;
      }
      throw new BtApiException("Failed to parse websites response: " + e.getMessage(), e);
    }
  }

  private Integer parseInteger(JSONObject jsonObject, String fieldName) {
    Object value = jsonObject.get(fieldName);
    if (value == null) {
      return null;
    }
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    String stringValue = String.valueOf(value);
    if (stringValue.isBlank() || "null".equalsIgnoreCase(stringValue)) {
      return null;
    }
    return Integer.parseInt(stringValue);
  }

  private Long parseLong(JSONObject jsonObject, String fieldName) {
    Object value = jsonObject.get(fieldName);
    if (value == null) {
      return null;
    }
    if (value instanceof Number) {
      return ((Number) value).longValue();
    }
    String stringValue = String.valueOf(value);
    if (stringValue.isBlank() || "null".equalsIgnoreCase(stringValue)) {
      return null;
    }
    return Long.parseLong(stringValue);
  }

  private Long parseTimestamp(String dateTime) {
    if (dateTime == null || dateTime.isBlank()) {
      return null;
    }
    try {
      Date date = DATE_FORMAT.parse(dateTime);
      return date == null ? null : date.getTime() / 1000;
    } catch (ParseException exception) {
      return null;
    }
  }
}
