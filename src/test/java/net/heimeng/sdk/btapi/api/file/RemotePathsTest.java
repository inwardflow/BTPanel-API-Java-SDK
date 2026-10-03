package net.heimeng.sdk.btapi.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("RemotePaths tests")
class RemotePathsTest {

  @Test
  @DisplayName("should normalize duplicate and trailing slashes")
  void normalizesSlashes() {
    assertEquals("/www/wwwroot/site", RemotePaths.requireSafeAbsolutePath("//www//wwwroot/site/"));
    assertEquals("/", RemotePaths.requireSafeAbsolutePath("/"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        " ",
        "www/wwwroot/site",
        "C:/Program Files/Git/www/wwwroot",
        "/www/wwwroot/../../etc",
        "/www/wwwroot/./site",
        "/www/wwwroot/site\n/etc"
      })
  @DisplayName("should reject relative, traversal, and control-character paths")
  void rejectsUnsafePaths(String path) {
    assertThrows(IllegalArgumentException.class, () -> RemotePaths.requireSafeAbsolutePath(path));
  }

  @Test
  @DisplayName("should reject null paths")
  void rejectsNull() {
    assertThrows(IllegalArgumentException.class, () -> RemotePaths.requireSafeAbsolutePath(null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/", "/etc", "/etc/", "/www", "/www/wwwroot", "//www/server/", "/root"})
  @DisplayName("should refuse to delete protected directories")
  void refusesProtectedDirectories(String path) {
    assertThrows(IllegalArgumentException.class, () -> RemotePaths.requireDeletablePath(path));
  }

  @Test
  @DisplayName("should allow deleting paths below protected directories")
  void allowsChildrenOfProtectedDirectories() {
    assertEquals(
        "/www/wwwroot/example.com/tmp",
        RemotePaths.requireDeletablePath("/www/wwwroot/example.com/tmp"));
  }

  @Test
  @DisplayName("isWithin should compare whole path segments")
  void isWithinComparesSegments() {
    assertTrue(RemotePaths.isWithin("/www/wwwroot/site-a", "/www/wwwroot/site-a"));
    assertTrue(RemotePaths.isWithin("/www/wwwroot/site-a/", "/www/wwwroot/site-a/logs/x.log"));
    assertFalse(RemotePaths.isWithin("/www/wwwroot/site-a", "/www/wwwroot/site-a2"));
    assertFalse(RemotePaths.isWithin("/www/wwwroot/site-a", "/www/wwwroot"));
    assertTrue(RemotePaths.isWithin("/", "/etc"));
    assertThrows(
        IllegalArgumentException.class,
        () -> RemotePaths.isWithin("/www/wwwroot/site-a", "/www/wwwroot/site-a/../site-b"));
  }

  @Test
  @DisplayName("delete APIs should reject protected and traversal paths before any request")
  void deleteApisApplyGuard() {
    assertThrows(IllegalArgumentException.class, () -> new DeleteFileDirectoryApi().setPath("/"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new DeleteFileDirectoryApi().setPath("/www/wwwroot/site/../../server"));
    assertThrows(IllegalArgumentException.class, () -> new DeleteFileApi().setPath("etc/passwd"));
    assertEquals(
        "/www/wwwroot/site/old",
        new DeleteFileDirectoryApi().setPath("/www/wwwroot/site/old/").getParams().get("path"));
  }
}
