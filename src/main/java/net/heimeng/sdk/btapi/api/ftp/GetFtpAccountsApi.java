package net.heimeng.sdk.btapi.api.ftp;

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
import net.heimeng.sdk.btapi.model.ftp.FtpAccount;

/**
 * 查询 FTP 账号列表的 API。
 *
 * <p>面板新版本已将 FTP 列表迁移到统一 datalist 入口，因此这里默认补齐分页参数， 避免真实集成测试因缺少列表参数而被面板判定为非法请求。
 */
public class GetFtpAccountsApi extends BaseBtApi<BtResult<List<FtpAccount>>> {

  private static final String ENDPOINT = "/datalist/data/get_data_list";
  private static final String TABLE = "ftps";
  private static final String SUCCESS_MESSAGE = "Success";
  private static final DateTimeFormatter CREATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public GetFtpAccountsApi() {
    super(ENDPOINT, HttpMethod.POST);
    addParam("table", TABLE);
    addParam("p", 1);
    addParam("limit", 100);
    addParam("search", "");
  }

  @Override
  public BtResult<List<FtpAccount>> parseResponse(String response) {
    if (response == null || response.isBlank()) {
      throw new BtApiException("Empty response received");
    }

    String normalizedResponse = response.trim();
    try {
      if (!JSONUtil.isTypeJSON(normalizedResponse)) {
        throw new BtApiException("Invalid JSON response: " + normalizedResponse);
      }

      JSON json = JSONUtil.parse(normalizedResponse);
      if (!(json instanceof JSONObject jsonObject)) {
        throw new BtApiException("FTP accounts response must be a JSON object");
      }
      return parseObjectResponse(jsonObject);
    } catch (JSONException exception) {
      throw new BtApiException("Invalid JSON response: " + normalizedResponse, exception);
    }
  }

  private BtResult<List<FtpAccount>> parseObjectResponse(JSONObject jsonObject) {
    if (jsonObject.containsKey("status") && !jsonObject.getBool("status", false)) {
      return failureResult(jsonObject.getStr("msg", "Failed to fetch FTP accounts"));
    }

    Object rawData = jsonObject.get("data");
    if (!(rawData instanceof JSONArray jsonArray)) {
      if (jsonObject.containsKey("status")) {
        throw new BtApiException("FTP accounts response is missing required data array");
      }
      throw new BtApiException("FTP accounts response must contain a data array");
    }

    String message = jsonObject.getStr("msg", SUCCESS_MESSAGE);
    return successResult(parseAccounts(jsonArray), message);
  }

  private List<FtpAccount> parseAccounts(JSONArray jsonArray) {
    List<FtpAccount> ftpAccounts = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject ftpJson = jsonArray.getJSONObject(i);
      if (ftpJson == null) {
        continue;
      }

      FtpAccount ftpAccount = new FtpAccount();
      ftpAccount.setId(ftpJson.getInt("id", 0));
      ftpAccount.setUsername(ftpJson.getStr("name", ""));
      ftpAccount.setPath(ftpJson.getStr("path", ""));
      ftpAccount.setSize(readQuotaValue(ftpJson, "size"));
      ftpAccount.setUsedSize(readQuotaValue(ftpJson, "used"));
      ftpAccount.setStatus(readStatus(ftpJson.get("status")));
      ftpAccount.setCanViewAll(readBooleanFlag(ftpJson.get("can_view_all")));
      ftpAccount.setWebsiteDomain(ftpJson.getStr("domain", ""));
      ftpAccount.setCreateTime(
          parseCreateTime(
              ftpJson.getStr(
                  "create_time", ftpJson.getStr("addtime", ftpJson.getStr("add_time", "")))));
      ftpAccounts.add(ftpAccount);
    }
    return List.copyOf(ftpAccounts);
  }

  private long readQuotaValue(JSONObject ftpJson, String fieldName) {
    JSONObject quotaJson = ftpJson.getJSONObject("quota");
    if (quotaJson != null) {
      return quotaJson.getLong(fieldName, 0L);
    }
    return ftpJson.getLong(fieldName, 0L);
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

  private String readStatus(Object rawStatus) {
    if (rawStatus instanceof Number numberStatus) {
      return numberStatus.intValue() == 1 ? "normal" : "disabled";
    }
    if (rawStatus instanceof String stringStatus) {
      if ("1".equals(stringStatus) || "true".equalsIgnoreCase(stringStatus)) {
        return "normal";
      }
      if ("0".equals(stringStatus) || "false".equalsIgnoreCase(stringStatus)) {
        return "disabled";
      }
      return stringStatus;
    }
    return "normal";
  }

  private Date parseCreateTime(String createTime) {
    if (createTime == null || createTime.isBlank()) {
      return null;
    }

    try {
      LocalDateTime localDateTime = LocalDateTime.parse(createTime, CREATE_TIME_FORMATTER);
      return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    } catch (DateTimeParseException exception) {
      return null;
    }
  }

  private BtResult<List<FtpAccount>> successResult(List<FtpAccount> ftpAccounts, String message) {
    BtResult<List<FtpAccount>> result = new BtResult<>();
    result.setStatus(true);
    result.setMsg(message == null || message.isBlank() ? SUCCESS_MESSAGE : message);
    result.setData(ftpAccounts);
    return result;
  }

  private BtResult<List<FtpAccount>> failureResult(String message) {
    BtResult<List<FtpAccount>> result = new BtResult<>();
    result.setStatus(false);
    result.setMsg(message);
    result.setData(List.of());
    return result;
  }
}
