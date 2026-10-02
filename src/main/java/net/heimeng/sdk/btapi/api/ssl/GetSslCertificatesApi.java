package net.heimeng.sdk.btapi.api.ssl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;

/**
 * 查询 SSL 证书列表的 API。
 *
 * <p>当前面板使用 {@code ssl?action=get_cert_list} 返回证书列表，响应可能是直接返回 JSON 数组， 也可能是带有 {@code
 * status/msg/data} 的包装对象。
 *
 * <p>解析时会兼容新旧字段名，并优先从证书自身的 CN / SAN 信息中提取域名列表，而不是站点名。
 */
public class GetSslCertificatesApi extends BaseBtApi<BtResult<List<SslCertificate>>> {

  private static final String ENDPOINT = "ssl?action=get_cert_list";
  private static final String SUCCESS_MESSAGE = "Success";
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  public GetSslCertificatesApi() {
    super(ENDPOINT, HttpMethod.POST);
  }

  @Override
  public BtResult<List<SslCertificate>> parseResponse(String response) {
    if (response == null || response.isBlank()) {
      throw new BtApiException("Empty response received");
    }

    String normalizedResponse = response.trim();
    try {
      if (!JSONUtil.isTypeJSON(normalizedResponse)) {
        throw new BtApiException("Invalid JSON response: " + normalizedResponse);
      }

      JSON json = JSONUtil.parse(normalizedResponse);
      if (json instanceof JSONArray jsonArray) {
        return successResult(parseCertificates(jsonArray), SUCCESS_MESSAGE);
      }
      if (json instanceof JSONObject jsonObject) {
        return parseObjectResponse(jsonObject);
      }
      throw new BtApiException("SSL certificates response must be a JSON object or array");
    } catch (JSONException exception) {
      throw new BtApiException("Invalid JSON response: " + normalizedResponse, exception);
    }
  }

  private BtResult<List<SslCertificate>> parseObjectResponse(JSONObject jsonObject) {
    if (jsonObject.containsKey("status") && !jsonObject.getBool("status", false)) {
      return failureResult(jsonObject.getStr("msg", "Failed to fetch SSL certificates"));
    }

    Object rawData = jsonObject.get("data");
    if (rawData instanceof JSONArray jsonArray) {
      String message = jsonObject.getStr("msg", SUCCESS_MESSAGE);
      return successResult(parseCertificates(jsonArray), message);
    }

    if (jsonObject.containsKey("status")) {
      throw new BtApiException("SSL certificates response is missing required data array");
    }
    throw new BtApiException("SSL certificates response must contain a data array");
  }

  private List<SslCertificate> parseCertificates(JSONArray jsonArray) {
    List<SslCertificate> certificates = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject certJson = jsonArray.getJSONObject(i);
      if (certJson == null) {
        continue;
      }

      JSONObject infoJson = certJson.getJSONObject("info");
      SslCertificate certificate = new SslCertificate();
      certificate.setId(certJson.getInt("id", 0));
      certificate.setName(firstNonBlank(certJson.getStr("name"), certJson.getStr("subject")));
      certificate.setType(certJson.getStr("type", ""));
      certificate.setDomains(readDomains(certJson.get("domains"), certJson.get("dns"), infoJson));
      certificate.setIssuer(
          firstNonBlank(
              certJson.getStr("issuer"), infoJson == null ? null : infoJson.getStr("issuer")));
      certificate.setValidFrom(
          parseDate(
              firstNonBlank(
                  certJson.getStr("valid_from"),
                  certJson.getStr("not_before"),
                  infoJson == null ? null : infoJson.getStr("notBefore"))));
      certificate.setValidTo(
          parseDate(
              firstNonBlank(
                  certJson.getStr("valid_to"),
                  certJson.getStr("not_after"),
                  infoJson == null ? null : infoJson.getStr("notAfter"))));
      certificate.setAutoRenew(readBooleanFlag(certJson.get("auto_renew")));
      certificate.setHash(certJson.getStr("hash", ""));
      certificate.setFingerprint(
          firstNonBlank(certJson.getStr("fingerprint"), certJson.getStr("hash")));
      certificate.setStatus(
          resolveStatus(
              certJson.getStr("status", ""),
              readEndTime(
                  certJson.get("endtime"), infoJson == null ? null : infoJson.get("endtime")),
              certificate.getValidTo()));
      certificates.add(certificate);
    }
    return List.copyOf(certificates);
  }

  private List<String> readDomains(Object rawDomains, Object rawDns, JSONObject infoJson) {
    List<String> domains = readDomainValues(rawDomains);
    if (!domains.isEmpty()) {
      return domains;
    }

    domains = readDomainValues(rawDns);
    if (!domains.isEmpty()) {
      return domains;
    }

    if (infoJson != null) {
      domains = readDomainValues(infoJson.get("dns"));
      if (!domains.isEmpty()) {
        return domains;
      }
    }

    return List.of();
  }

  private List<String> readDomainValues(Object rawDomains) {
    if (rawDomains instanceof JSONArray jsonArray) {
      List<String> domains = new ArrayList<>();
      for (int i = 0; i < jsonArray.size(); i++) {
        Object domain = jsonArray.get(i);
        if (domain instanceof String stringDomain && !stringDomain.isBlank()) {
          domains.add(stringDomain.trim());
        }
      }
      return List.copyOf(domains);
    }

    if (rawDomains instanceof String domainsString && !domainsString.isBlank()) {
      List<String> domains = new ArrayList<>();
      for (String domain : domainsString.split(",")) {
        if (!domain.isBlank()) {
          domains.add(domain.trim());
        }
      }
      return List.copyOf(domains);
    }

    return List.of();
  }

  private boolean readBooleanFlag(Object rawFlag) {
    if (rawFlag instanceof Boolean booleanFlag) {
      return booleanFlag;
    }
    if (rawFlag instanceof Number numberFlag) {
      return numberFlag.intValue() != 0;
    }
    if (rawFlag instanceof String stringFlag) {
      return "1".equals(stringFlag) || Boolean.parseBoolean(stringFlag);
    }
    return false;
  }

  private Integer readEndTime(Object rawEndTime, Object nestedEndTime) {
    Integer value = toInteger(rawEndTime);
    if (value != null) {
      return value;
    }
    return toInteger(nestedEndTime);
  }

  private Integer toInteger(Object value) {
    if (value instanceof Number numberValue) {
      return numberValue.intValue();
    }
    if (value instanceof String stringValue && !stringValue.isBlank()) {
      try {
        return Integer.parseInt(stringValue);
      } catch (NumberFormatException ignored) {
        return null;
      }
    }
    return null;
  }

  private Date parseDate(String rawDate) {
    if (rawDate == null || rawDate.isBlank()) {
      return null;
    }

    try {
      LocalDateTime localDateTime = LocalDateTime.parse(rawDate, DATE_TIME_FORMATTER);
      return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    } catch (DateTimeParseException ignored) {
      // Fall through to the date-only format used by newer panel SSL APIs.
    }

    try {
      LocalDate localDate = LocalDate.parse(rawDate, DATE_FORMATTER);
      return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    } catch (DateTimeParseException exception) {
      return null;
    }
  }

  private String resolveStatus(String rawStatus, Integer endTime, Date validTo) {
    if (rawStatus != null && !rawStatus.isBlank()) {
      return rawStatus;
    }
    if (endTime != null) {
      if (endTime < 0) {
        return "expired";
      }
      if (endTime <= 30) {
        return "expiring_soon";
      }
      return "valid";
    }
    if (validTo == null) {
      return "unknown";
    }

    long daysDiff = (validTo.getTime() - System.currentTimeMillis()) / (1000L * 60 * 60 * 24);
    if (daysDiff < 0) {
      return "expired";
    }
    if (daysDiff <= 30) {
      return "expiring_soon";
    }
    return "valid";
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return "";
  }

  private BtResult<List<SslCertificate>> successResult(
      List<SslCertificate> certificates, String message) {
    BtResult<List<SslCertificate>> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg(message == null || message.isBlank() ? SUCCESS_MESSAGE : message);
    result.setData(certificates);
    return result;
  }

  private BtResult<List<SslCertificate>> failureResult(String message) {
    BtResult<List<SslCertificate>> result = new BtResult<>();
    result.setStatus(false);
    result.setMsg(message);
    result.setData(List.of());
    return result;
  }
}
