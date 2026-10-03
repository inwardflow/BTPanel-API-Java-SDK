package net.heimeng.sdk.btapi.api.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.PanelUpdateInfo;

@DisplayName("CheckPanelUpdateApi 单元测试")
class CheckPanelUpdateApiTest {

  @Test
  @DisplayName("默认构造函数不应强制检查更新")
  void defaultConstructorDoesNotForceCheck() {
    CheckPanelUpdateApi api = new CheckPanelUpdateApi();

    assertEquals("ajax?action=UpdatePanel", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertFalse(api.getParams().containsKey("check"));
  }

  @Test
  @DisplayName("强制检查构造函数应添加 check 参数")
  void forceCheckConstructorAddsCheckParam() {
    CheckPanelUpdateApi api = new CheckPanelUpdateApi(true);

    assertEquals(Boolean.TRUE, api.getParams().get("check"));
  }

  @Test
  @DisplayName("setForceCheck 应支持参数开关")
  void setForceCheckTogglesParam() {
    CheckPanelUpdateApi api = new CheckPanelUpdateApi();

    api.setForceCheck(true);
    assertEquals(Boolean.TRUE, api.getParams().get("check"));

    api.setForceCheck(false);
    assertFalse(api.getParams().containsKey("check"));
  }

  @Test
  @DisplayName("应正确解析有可用更新的响应")
  void parsesAvailableUpdateResponse() {
    CheckPanelUpdateApi api = new CheckPanelUpdateApi();
    BtResult<PanelUpdateInfo> result =
        api.parseResponse(
            """
            {
              "status": true,
              "version": "6.8.2",
              "updateMsg": "升级说明"
            }
            """);

    assertTrue(result.isSuccess());
    assertTrue(result.getData().isUpdateAvailable());
    assertEquals("6.8.2", result.getData().getVersion());
    assertEquals("升级说明", result.getData().getUpdateMsg());
  }

  @Test
  @DisplayName("应正确解析无更新响应")
  void parsesNoUpdateResponse() {
    CheckPanelUpdateApi api = new CheckPanelUpdateApi();
    BtResult<PanelUpdateInfo> result =
        api.parseResponse(
            """
            {
              "status": false,
              "msg": "already latest",
              "version": "6.8.2",
              "updateMsg": ""
            }
            """);

    assertTrue(result.isSuccess());
    assertFalse(result.getData().isUpdateAvailable());
    assertEquals("already latest", result.getMsg());
  }

  @Test
  @DisplayName("缺少状态字段时应兼容新版响应")
  void supportsResponseWithoutStatusField() {
    CheckPanelUpdateApi api = new CheckPanelUpdateApi();
    BtResult<PanelUpdateInfo> result =
        api.parseResponse("{\"version\":\"6.8.2\",\"updateMsg\":\"升级说明\"}");

    assertTrue(result.isSuccess());
    assertTrue(result.getData().isUpdateAvailable());
    assertEquals("6.8.2", result.getData().getVersion());
  }
}
