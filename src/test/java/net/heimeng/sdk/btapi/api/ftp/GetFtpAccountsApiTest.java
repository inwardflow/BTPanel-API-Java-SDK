package net.heimeng.sdk.btapi.api.ftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ftp.FtpAccount;

@DisplayName("GetFtpAccountsApi 单元测试")
class GetFtpAccountsApiTest {

  private final GetFtpAccountsApi api = new GetFtpAccountsApi();

  @Test
  @DisplayName("应暴露正确的接口元数据")
  void exposesMetadata() {
    assertEquals("/datalist/data/get_data_list", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals("ftps", api.getParams().get("table"));
    assertEquals(1, api.getParams().get("p"));
    assertEquals(100, api.getParams().get("limit"));
    assertEquals("", api.getParams().get("search"));
  }

  @Test
  @DisplayName("应正确解析成功的 FTP 列表响应")
  void parsesSuccessfulAccountList() {
    BtResult<List<FtpAccount>> result =
        api.parseResponse(
            """
            {
              "status": true,
              "msg": "ok",
              "data": [
                {
                  "id": 1,
                  "name": "demo",
                  "path": "/www/wwwroot/demo",
                  "quota": {
                    "size": 2048,
                    "used": 512
                  },
                  "status": "1",
                  "ps": "remark",
                  "domain": "demo.example.com",
                  "addtime": "2024-01-02 03:04:05"
                }
              ]
            }
            """);

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(1, result.getData().size());

    FtpAccount ftpAccount = result.getData().get(0);
    assertEquals(1, ftpAccount.getId());
    assertEquals("demo", ftpAccount.getUsername());
    assertEquals("/www/wwwroot/demo", ftpAccount.getPath());
    assertEquals(2048L, ftpAccount.getSize());
    assertEquals(512L, ftpAccount.getUsedSize());
    assertTrue(ftpAccount.isNormal());
    assertFalse(ftpAccount.isCanViewAll());
    assertEquals("demo.example.com", ftpAccount.getWebsiteDomain());
    assertNotNull(ftpAccount.getCreateTime());
  }

  @Test
  @DisplayName("应支持没有状态字段但带 data 数组的列表响应")
  void parsesDataOnlyPayload() {
    BtResult<List<FtpAccount>> result =
        api.parseResponse(
            "{\"data\":[{\"name\":\"demo\",\"status\":1,\"quota\":{\"size\":8,\"used\":2}}]}");

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).isNormal());
    assertEquals(8L, result.getData().get(0).getSize());
    assertEquals(2L, result.getData().get(0).getUsedSize());
  }

  @Test
  @DisplayName("应保留失败响应的状态与消息")
  void preservesFailurePayload() {
    BtResult<List<FtpAccount>> result =
        api.parseResponse("{\"status\":false,\"msg\":\"panel busy\"}");

    assertFalse(result.isSuccess());
    assertEquals("panel busy", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("成功响应缺少 data 数组时应抛出异常")
  void rejectsSuccessfulPayloadWithoutData() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("data array"));
  }

  @Test
  @DisplayName("非法时间格式应被忽略而不是导致解析失败")
  void ignoresInvalidCreateTime() {
    BtResult<List<FtpAccount>> result =
        api.parseResponse(
            "{\"data\":[{\"name\":\"demo\",\"addtime\":\"not-a-date\",\"status\":0}]}");

    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertEquals(null, result.getData().get(0).getCreateTime());
  }

  @Test
  @DisplayName("空响应应抛出异常")
  void rejectsBlankResponse() {
    BtApiException exception = assertThrows(BtApiException.class, () -> api.parseResponse(" "));

    assertTrue(exception.getMessage().contains("Empty response"));
  }
}
