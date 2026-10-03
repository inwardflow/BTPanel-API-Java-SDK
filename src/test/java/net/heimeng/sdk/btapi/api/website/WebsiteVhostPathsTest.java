package net.heimeng.sdk.btapi.api.website;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("WebsiteVhostPaths tests")
class WebsiteVhostPathsTest {

  @Test
  @DisplayName("builds the paths the 9.0 UI reads and writes")
  void buildsUiPaths() {
    assertEquals(
        "/www/server/panel/vhost/nginx/example.com.conf",
        WebsiteVhostPaths.nginxConfig("example.com"));
    assertEquals(
        "/www/server/panel/vhost/rewrite/example.com.conf",
        WebsiteVhostPaths.rewriteConfig(" example.com "));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "../etc/passwd", "a/b.com", "a\b.com", "a..b", "a\nb.com"})
  @DisplayName("rejects blank names and path segments")
  void rejectsUnsafeSiteNames(String siteName) {
    assertThrows(IllegalArgumentException.class, () -> WebsiteVhostPaths.rewriteConfig(siteName));
    assertThrows(IllegalArgumentException.class, () -> WebsiteVhostPaths.nginxConfig(siteName));
  }

  @Test
  @DisplayName("rejects null")
  void rejectsNull() {
    assertThrows(IllegalArgumentException.class, () -> WebsiteVhostPaths.rewriteConfig(null));
  }
}
