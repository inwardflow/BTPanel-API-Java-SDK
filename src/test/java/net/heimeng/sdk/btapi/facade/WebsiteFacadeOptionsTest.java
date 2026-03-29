package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Website facade option objects")
class WebsiteFacadeOptionsTest {

  @Test
  @DisplayName("WebsiteDeleteOptions factories should expose intent clearly")
  void deleteOptionsFactoriesExposeIntent() {
    WebsiteDeleteOptions none = WebsiteDeleteOptions.none();
    WebsiteDeleteOptions deleteAll = WebsiteDeleteOptions.deleteAll();

    assertFalse(none.deleteFtp());
    assertFalse(none.deleteDatabase());
    assertFalse(none.deletePath());
    assertTrue(deleteAll.deleteFtp());
    assertTrue(deleteAll.deleteDatabase());
    assertTrue(deleteAll.deletePath());
  }

  @Test
  @DisplayName("WebsiteLimitNetOptions should reject negative values")
  void limitNetOptionsRejectNegativeValues() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> new WebsiteLimitNetOptions(true, -1, 30, 1024));

    assertEquals("perServer cannot be negative", exception.getMessage());
  }

  @Test
  @DisplayName("WebsiteSslCertificateOptions should require non blank values")
  void sslOptionsRequireNonBlankValues() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new WebsiteSslCertificateOptions("demo.example.com", " ", "key", Boolean.TRUE));

    assertEquals("certificate cannot be blank", exception.getMessage());
  }
}
