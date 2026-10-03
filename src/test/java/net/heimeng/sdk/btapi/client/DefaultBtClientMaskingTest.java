package net.heimeng.sdk.btapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DefaultBtClient log masking tests")
class DefaultBtClientMaskingTest {

  @Test
  @DisplayName("isSensitiveKey should match prefixed and suffixed credential names")
  void detectsSensitiveKeys() {
    assertTrue(DefaultBtClient.isSensitiveKey("request_token"));
    assertTrue(DefaultBtClient.isSensitiveKey("ftp_password"));
    assertTrue(DefaultBtClient.isSensitiveKey("new_password"));
    assertTrue(DefaultBtClient.isSensitiveKey("API_KEY"));
    assertFalse(DefaultBtClient.isSensitiveKey("request_time"));
    assertFalse(DefaultBtClient.isSensitiveKey("siteName"));
  }

  @Test
  @DisplayName("maskUrl should hide every sensitive query parameter")
  void masksSensitiveQueryParameters() {
    String masked =
        DefaultBtClient.maskUrl(
            "http://localhost:8888/ftp?action=SetUserPassword"
                + "&ftp_username=demo&new_password=p1&request_token=t1&request_time=1700000000");

    assertEquals(
        "http://localhost:8888/ftp?action=SetUserPassword"
            + "&ftp_username=demo&new_password=***&request_token=***&request_time=1700000000",
        masked);
  }

  @Test
  @DisplayName("maskUrl and maskParams should agree on which keys are sensitive")
  void maskParamsUsesSameRules() {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("ftp_username", "demo");
    params.put("ftp_password", "p1");

    assertEquals("{ftp_username=demo, ftp_password=***}", DefaultBtClient.maskParams(params));
  }
}
