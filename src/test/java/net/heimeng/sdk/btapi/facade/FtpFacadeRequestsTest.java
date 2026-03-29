package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FTP facade request objects")
class FtpFacadeRequestsTest {

  @Test
  @DisplayName("FtpCreateRequest factory should default remark to username")
  void createRequestFactoryAppliesDefaults() {
    FtpCreateRequest request = FtpCreateRequest.of("demo", "secret", "/www/wwwroot/demo");

    assertEquals("demo", request.username());
    assertEquals("secret", request.password());
    assertEquals("/www/wwwroot/demo", request.path());
    assertEquals("demo", request.remark());
  }

  @Test
  @DisplayName("FtpDeleteRequest should require positive account id")
  void deleteRequestRequiresPositiveId() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new FtpDeleteRequest(0, "demo"));

    assertEquals("accountId must be positive", exception.getMessage());
  }

  @Test
  @DisplayName("FtpPasswordUpdateRequest should require non blank path")
  void passwordUpdateRequestRequiresPath() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new FtpPasswordUpdateRequest(1, "demo", " ", "new-secret"));

    assertEquals("path cannot be blank", exception.getMessage());
  }
}
