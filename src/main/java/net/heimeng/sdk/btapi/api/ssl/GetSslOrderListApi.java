package net.heimeng.sdk.btapi.api.ssl;

import java.util.List;
import java.util.Map;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * Lists commercial SSL orders associated with a site.
 *
 * <p>The current panel returns a direct JSON array for this endpoint, and the per-item schema has
 * not yet been fully observed under developer-signature auth, so raw maps are preserved.
 */
public class GetSslOrderListApi extends BaseBtApi<BtResult<List<Map<String, Object>>>> {

  private static final String ENDPOINT = "ssl?action=get_order_list";
  private static final String SUCCESS_MESSAGE = "Success";
  private static final String FAILURE_MESSAGE = "Failed to fetch SSL order list";

  public GetSslOrderListApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  public GetSslOrderListApi(String siteName) {
    this();
    setSiteName(siteName);
  }

  public GetSslOrderListApi setSiteName(String siteName) {
    requireNonBlank(siteName, "siteName");
    addParam("siteName", siteName);
    return this;
  }

  @Override
  public BtResult<List<Map<String, Object>>> parseResponse(String response) {
    JSON json = SslApiResponseSupport.parseJsonResponse(response, "SSL order list");
    if (json instanceof JSONArray jsonArray) {
      return SslApiResponseSupport.successResult(
          SslApiResponseSupport.toMapList(jsonArray), SUCCESS_MESSAGE);
    }
    if (!(json instanceof JSONObject jsonObject)) {
      throw new BtApiException("SSL order list response must be a JSON object or array");
    }
    if (jsonObject.containsKey("status") && !jsonObject.getBool("status", false)) {
      return SslApiResponseSupport.failureResult(
          List.of(), jsonObject.getStr("msg", FAILURE_MESSAGE));
    }

    JSONArray dataArray = jsonObject.getJSONArray("data");
    if (dataArray == null) {
      throw new BtApiException("SSL order list response is missing required data array");
    }
    return SslApiResponseSupport.successResult(
        SslApiResponseSupport.toMapList(dataArray), jsonObject.getStr("msg", SUCCESS_MESSAGE));
  }

  @Override
  protected boolean validateParams() {
    Object siteName = params.get("siteName");
    return siteName instanceof String stringValue && !stringValue.isBlank();
  }

  private void requireNonBlank(String value, String paramName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(paramName + " cannot be blank");
    }
  }
}
