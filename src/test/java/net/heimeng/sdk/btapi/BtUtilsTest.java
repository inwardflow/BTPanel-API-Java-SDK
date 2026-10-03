package net.heimeng.sdk.btapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BtUtils tests")
class BtUtilsTest {

  @Test
  @DisplayName("generateRequestTime should return Unix seconds, not milliseconds")
  void generateRequestTimeReturnsUnixSeconds() {
    long before = Instant.now().getEpochSecond();
    long requestTime = BtUtils.generateRequestTime();
    long after = Instant.now().getEpochSecond();

    assertTrue(requestTime >= before && requestTime <= after, "was " + requestTime);
  }

  @Test
  @DisplayName("generateRequestToken should follow md5(request_time + md5(api_key))")
  void generateRequestTokenMatchesPanelProtocol() {
    // Expected value computed independently with Python hashlib.
    assertEquals(
        "b9d054e2a52c4febb620b2db30213051",
        BtUtils.generateRequestToken("example-api-key", 1700000000L));
  }

  @Test
  @DisplayName("Base64 helpers should use UTF-8 regardless of the platform charset")
  void base64UsesUtf8() {
    // "宝塔" encoded as UTF-8 bytes E5 AE 9D E5 A1 94.
    assertEquals("5a6d5aGU", BtUtils.toBase64("宝塔"));
    assertEquals("宝塔", BtUtils.fromBase64("5a6d5aGU"));
  }

  @Test
  @DisplayName("generateRequestToken should reject a null API key")
  void generateRequestTokenRejectsNullApiKey() {
    assertThrows(NullPointerException.class, () -> BtUtils.generateRequestToken(null, 1L));
  }
}
