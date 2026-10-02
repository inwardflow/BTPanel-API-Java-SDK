package net.heimeng.sdk.btapi.api.ssl;

import java.util.ArrayList;
import java.util.List;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslSiteCertificateDetail;
import net.heimeng.sdk.btapi.model.ssl.SslSiteStatus;

/** Queries the current SSL state of a site through {@code site?action=GetSSL}. */
public class GetWebsiteSslStatusApi extends BaseBtApi<BtResult<SslSiteStatus>> {

  private static final String ENDPOINT = "site?action=GetSSL";
  private static final String SUCCESS_MESSAGE = "Success";
  private static final String FAILURE_MESSAGE = "Failed to fetch website SSL status";

  public GetWebsiteSslStatusApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  public GetWebsiteSslStatusApi(String siteName) {
    this();
    setSiteName(siteName);
  }

  public GetWebsiteSslStatusApi setSiteName(String siteName) {
    requireNonBlank(siteName, "siteName");
    addParam("siteName", siteName);
    return this;
  }

  @Override
  public BtResult<SslSiteStatus> parseResponse(String response) {
    JSON json = SslApiResponseSupport.parseJsonResponse(response, "website SSL status");
    if (!(json instanceof JSONObject jsonObject)) {
      throw new BtApiException("Website SSL status response must be a JSON object");
    }

    if (!containsStatusPayload(jsonObject)) {
      if (jsonObject.containsKey("msg")) {
        return SslApiResponseSupport.failureResult(null, jsonObject.getStr("msg", FAILURE_MESSAGE));
      }
      throw new BtApiException("Website SSL status response is missing required payload fields");
    }

    return SslApiResponseSupport.successResult(
        parseStatus(jsonObject), jsonObject.getStr("msg", SUCCESS_MESSAGE));
  }

  @Override
  protected boolean validateParams() {
    Object siteName = params.get("siteName");
    return siteName instanceof String stringValue && !stringValue.isBlank();
  }

  private SslSiteStatus parseStatus(JSONObject jsonObject) {
    SslSiteStatus status = new SslSiteStatus();
    status.setEnabled(jsonObject.getBool("status", false));
    status.setOrderId(normalizeOrderId(SslApiResponseSupport.toInteger(jsonObject.get("oid"))));
    status.setDomains(readDomains(jsonObject.getJSONArray("domain")));
    status.setPrivateKeyPem(SslApiResponseSupport.readOptionalString(jsonObject.get("key")));
    status.setCertificatePem(SslApiResponseSupport.readOptionalString(jsonObject.get("csr")));
    status.setType(normalizeType(SslApiResponseSupport.toInteger(jsonObject.get("type"))));
    status.setHttpToHttps(jsonObject.getBool("httpTohttps", false));
    status.setCertificateDetail(parseCertificateDetail(jsonObject.getJSONObject("cert_data")));
    status.setContactEmail(SslApiResponseSupport.readOptionalString(jsonObject.get("email")));
    return status;
  }

  private boolean containsStatusPayload(JSONObject jsonObject) {
    return SslApiResponseSupport.containsAny(
        jsonObject, "domain", "key", "csr", "type", "httpTohttps", "cert_data", "oid", "email");
  }

  private Integer normalizeOrderId(Integer orderId) {
    if (orderId == null || orderId < 0) {
      return null;
    }
    return orderId;
  }

  private int normalizeType(Integer type) {
    return type == null ? -1 : type;
  }

  private List<String> readDomains(JSONArray domainArray) {
    if (domainArray == null) {
      return List.of();
    }

    List<String> domains = new ArrayList<>();
    for (int i = 0; i < domainArray.size(); i++) {
      JSONObject domainObject = domainArray.getJSONObject(i);
      if (domainObject == null) {
        continue;
      }
      String name = domainObject.getStr("name", "").trim();
      if (!name.isEmpty()) {
        domains.add(name);
      }
    }
    return List.copyOf(domains);
  }

  private SslSiteCertificateDetail parseCertificateDetail(JSONObject certData) {
    if (certData == null || certData.isEmpty()) {
      return null;
    }

    SslSiteCertificateDetail detail = new SslSiteCertificateDetail();
    detail.setId(SslApiResponseSupport.toInteger(certData.get("id")));
    detail.setIssuer(SslApiResponseSupport.readOptionalString(certData.get("issuer")));
    detail.setSubject(SslApiResponseSupport.readOptionalString(certData.get("subject")));
    detail.setValidFrom(
        SslApiResponseSupport.parseDate(
            SslApiResponseSupport.readOptionalString(certData.get("notBefore"))));
    detail.setValidTo(
        SslApiResponseSupport.parseDate(
            SslApiResponseSupport.readOptionalString(certData.get("notAfter"))));
    return detail;
  }

  private void requireNonBlank(String value, String paramName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(paramName + " cannot be blank");
    }
  }
}
