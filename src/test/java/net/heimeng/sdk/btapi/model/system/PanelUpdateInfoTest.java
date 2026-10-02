package net.heimeng.sdk.btapi.model.system;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PanelUpdateInfo 模型测试")
class PanelUpdateInfoTest {

  @Test
  @DisplayName("应根据状态判断是否存在可用更新")
  void reportsUpdateAvailability() {
    PanelUpdateInfo panelUpdateInfo = new PanelUpdateInfo();
    panelUpdateInfo.setStatus(true);
    assertTrue(panelUpdateInfo.isUpdateAvailable());

    panelUpdateInfo.setStatus(false);
    assertFalse(panelUpdateInfo.isUpdateAvailable());
  }
}
