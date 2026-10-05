package net.heimeng.sdk.btapi.api.ssl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.facade.SslBatchDeploymentRequest;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslBatchDeploymentItem;
import net.heimeng.sdk.btapi.model.ssl.SslBatchDeploymentResult;

/** Deploys one or more saved certificates to panel sites. */
public class SetBatchSslCertificateToSiteApi extends BaseBtApi<BtResult<SslBatchDeploymentResult>> {

  private static final String ENDPOINT = "ssl?action=SetBatchCertToSite";
  private static final String SUCCESS_MESSAGE = "Success";
  private static final String FAILURE_MESSAGE = "Failed to deploy saved SSL certificate";

  private final List<SslBatchDeploymentRequest> batchRequests = new ArrayList<>();

  public SetBatchSslCertificateToSiteApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  public SetBatchSslCertificateToSiteApi(String sslHash, String siteName, String certName) {
    this();
    addDeployment(sslHash, siteName, certName);
  }

  public SetBatchSslCertificateToSiteApi addDeployment(
      String sslHash, String siteName, String certName) {
    return addDeployment(SslBatchDeploymentRequest.of(sslHash, siteName, certName));
  }

  public SetBatchSslCertificateToSiteApi addDeployment(SslBatchDeploymentRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request cannot be null");
    }
    batchRequests.add(request);
    syncBatchInfoParam();
    return this;
  }

  public SetBatchSslCertificateToSiteApi setBatch(List<SslBatchDeploymentRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      throw new IllegalArgumentException("requests cannot be empty");
    }
    batchRequests.clear();
    requests.forEach(this::addDeployment);
    syncBatchInfoParam();
    return this;
  }

  @Override
  public BtResult<SslBatchDeploymentResult> parseResponse(String response) {
    JSON json = SslApiResponseSupport.parseJsonResponse(response, "SSL batch deployment");
    if (!(json instanceof JSONObject jsonObject)) {
      throw new BtApiException("SSL batch deployment response must be a JSON object");
    }
    if (jsonObject.containsKey("status")
        && !jsonObject.getBool("status", false)
        && !jsonObject.containsKey("total")) {
      return SslApiResponseSupport.failureResult(null, jsonObject.getStr("msg", FAILURE_MESSAGE));
    }

    if (!jsonObject.containsKey("total")
        && !jsonObject.containsKey("success")
        && !jsonObject.containsKey("faild")) {
      throw new BtApiException("SSL batch deployment response is missing required summary fields");
    }

    SslBatchDeploymentResult result = new SslBatchDeploymentResult();
    result.setTotal(defaultZero(SslApiResponseSupport.toInteger(jsonObject.get("total"))));
    result.setSuccessCount(defaultZero(SslApiResponseSupport.toInteger(jsonObject.get("success"))));
    result.setFailedCount(defaultZero(SslApiResponseSupport.toInteger(jsonObject.get("faild"))));
    result.setSuccessList(parseItems(jsonObject.getJSONArray("successList")));
    result.setFailedList(parseItems(jsonObject.getJSONArray("faildList")));

    return SslApiResponseSupport.successResult(
        result,
        jsonObject.getStr("msg", result.isFullySuccessful() ? SUCCESS_MESSAGE : "Completed"));
  }

  @Override
  protected boolean validateParams() {
    Object batchInfo = params.get("BatchInfo");
    return batchInfo instanceof String stringValue
        && !stringValue.isBlank()
        && !"[]".equals(stringValue);
  }

  private void syncBatchInfoParam() {
    List<Map<String, String>> payload = new ArrayList<>();
    for (SslBatchDeploymentRequest request : batchRequests) {
      Map<String, String> item = new LinkedHashMap<>();
      item.put("ssl_hash", request.sslHash());
      item.put("siteName", request.siteName());
      item.put("certName", request.certName());
      payload.add(item);
    }
    addParam("BatchInfo", JSONUtil.toJsonStr(payload));
  }

  private List<SslBatchDeploymentItem> parseItems(JSONArray jsonArray) {
    if (jsonArray == null) {
      return List.of();
    }

    List<SslBatchDeploymentItem> items = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject jsonObject = jsonArray.getJSONObject(i);
      if (jsonObject == null) {
        continue;
      }

      SslBatchDeploymentItem item = new SslBatchDeploymentItem();
      item.setStatus(jsonObject.getBool("status", false));
      item.setCertName(jsonObject.getStr("certName", ""));
      item.setSiteName(jsonObject.getStr("siteName", ""));
      item.setMessage(firstNonBlank(jsonObject.getStr("msg"), jsonObject.getStr("message")));
      items.add(item);
    }
    return List.copyOf(items);
  }

  private int defaultZero(Integer value) {
    return value == null ? 0 : value;
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return "";
  }
}
