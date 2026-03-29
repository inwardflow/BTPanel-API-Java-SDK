package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.testutil.TestValueFactory;

@DisplayName("FTP facade request objects")
class FtpFacadeRequestsTest {

  @Test
  @DisplayName("FtpCreateRequest factory should default remark to username")
  void createRequestFactoryAppliesDefaults() {
    FtpCreateRequest request =
        FtpCreateRequest.of(
            TestValueFactory.sampleFtpUser(),
            TestValueFactory.samplePassword(),
            TestValueFactory.sampleFtpPath());

    assertEquals(TestValueFactory.sampleFtpUser(), request.username());
    assertEquals(TestValueFactory.samplePassword(), request.password());
    assertEquals(TestValueFactory.sampleFtpPath(), request.path());
    assertEquals(TestValueFactory.sampleFtpUser(), request.remark());
  }

  @Test
  @DisplayName("FtpDeleteRequest should require positive account id")
  void deleteRequestRequiresPositiveId() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new FtpDeleteRequest(0, TestValueFactory.sampleFtpUser()));

    assertEquals("accountId must be positive", exception.getMessage());
  }

  @Test
  @DisplayName("FtpPasswordUpdateRequest should require non blank path")
  void passwordUpdateRequestRequiresPath() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new FtpPasswordUpdateRequest(
                    1, TestValueFactory.sampleFtpUser(), " ", TestValueFactory.updatedSamplePassword()));

    assertEquals("path cannot be blank", exception.getMessage());
  }
}
