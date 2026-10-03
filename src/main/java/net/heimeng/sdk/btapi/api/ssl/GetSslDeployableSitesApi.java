package net.heimeng.sdk.btapi.api.ssl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslDeployableSites;

/** Resolves which sites a saved certificate can be deployed to. */
public class GetSslDeployableSitesApi extends BaseBtApi<BtResult<SslDeployableSites>> {

  private static final String ENDPOINT = "ssl?action=GetSiteDomain";
  private static final String SUCCESS_MESSAGE = "Success";
  private static final String FAILURE_MESSAGE = "Failed to resolve deployable sites";

  public GetSslDeployableSitesApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  public GetSslDeployableSitesApi(List<String> certificateNames) {
    this();
    setCertificateNames(certificateNames);
  }

  public GetSslDeployableSitesApi setCertificateNames(List<String> certificateNames) {
    List<String> normalizedNames = normalizeNames(certificateNames);
    addParam("cert_list", JSONUtil.toJsonStr(normalizedNames));
    return this;
  }

  @Override
  public BtResult<SslDeployableSites> parseResponse(String response) {
    JSON json = SslApiResponseSupport.parseJsonResponse(response, "deployable SSL sites");
    if (!(json instanceof JSONObject jsonObject)) {
      throw new BtApiException("Deployable SSL sites response must be a JSON object");
    }
    if (jsonObject.containsKey("status") && !jsonObject.getBool("status", false)) {
      return SslApiResponseSupport.failureResult(null, jsonObject.getStr("msg", FAILURE_MESSAGE));
    }

    JSONArray allSitesArray = jsonObject.getJSONArray("all");
    JSONArray matchedSitesArray = jsonObject.getJSONArray("site");
    if (allSitesArray == null && matchedSitesArray == null) {
      throw new BtApiException("Deployable SSL sites response is missing required all/site arrays");
    }

    SslDeployableSites deployableSites = new SslDeployableSites();
    deployableSites.setAllSites(readStringArray(allSitesArray));
    deployableSites.setMatchedSites(readStringArray(matchedSitesArray));
    return SslApiResponseSupport.successResult(
        deployableSites, jsonObject.getStr("msg", SUCCESS_MESSAGE));
  }

  @Override
  protected boolean validateParams() {
    Object certList = params.get("cert_list");
    return certList instanceof String stringValue
        && !stringValue.isBlank()
        && !"[]".equals(stringValue);
  }

  private List<String> normalizeNames(List<String> certificateNames) {
    if (certificateNames == null || certificateNames.isEmpty()) {
      throw new IllegalArgumentException("certificateNames cannot be empty");
    }

    LinkedHashSet<String> normalized = new LinkedHashSet<>();
    for (String certificateName : certificateNames) {
      if (certificateName != null && !certificateName.isBlank()) {
        normalized.add(certificateName.trim());
      }
    }
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("certificateNames cannot be empty");
    }
    return List.copyOf(normalized);
  }

  private List<String> readStringArray(JSONArray jsonArray) {
    if (jsonArray == null) {
      return List.of();
    }

    List<String> values = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      Object rawValue = jsonArray.get(i);
      String value = SslApiResponseSupport.readOptionalString(rawValue);
      if (value != null) {
        values.add(value);
      }
    }
    return List.copyOf(values);
  }
}
