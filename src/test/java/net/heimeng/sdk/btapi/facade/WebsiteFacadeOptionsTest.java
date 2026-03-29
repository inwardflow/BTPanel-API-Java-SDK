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
  @DisplayName("WebsiteDomainBinding should require non blank values")
  void domainBindingRequiresNonBlankValues() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new WebsiteDomainBinding("demo.example.com", " "));

    assertEquals("domain cannot be blank", exception.getMessage());
  }

  @Test
  @DisplayName("WebsiteDomainRemoval should require positive port")
  void domainRemovalRequiresPositivePort() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new WebsiteDomainRemoval("demo.example.com", "www.demo.example.com", 0));

    assertEquals("port must be positive", exception.getMessage());
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

  @Test
  @DisplayName("WebsitePasswordProtectionOptions should require username and password")
  void passwordOptionsRequireNonBlankValues() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new WebsitePasswordProtectionOptions("admin", " "));

    assertEquals("password cannot be blank", exception.getMessage());
  }

  @Test
  @DisplayName("WebsiteRewriteRulesOptions should allow blank content but not blank name")
  void rewriteRuleOptionsValidateNameOnly() {
    WebsiteRewriteRulesOptions options = new WebsiteRewriteRulesOptions("none", "");

    assertEquals("none", options.name());
    assertEquals("", options.content());
  }

  @Test
  @DisplayName("WebsiteNginxConfigOptions should require non blank domain")
  void nginxOptionsRequireNonBlankDomain() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new WebsiteNginxConfigOptions(" ", "server { listen 80; }"));

    assertEquals("domain cannot be blank", exception.getMessage());
  }

  @Test
  @DisplayName("WebsiteCreateRequest builder should apply sensible defaults")
  void createRequestBuilderAppliesDefaults() {
    WebsiteCreateRequest request =
        WebsiteCreateRequest.builder("demo.example.com", "/www/wwwroot/demo")
            .phpVersion("82")
            .build();

    assertEquals("demo.example.com", request.domain());
    assertEquals("/www/wwwroot/demo", request.path());
    assertEquals(0, request.typeId());
    assertEquals("PHP", request.projectType());
    assertEquals("82", request.phpVersion());
    assertEquals(80, request.port());
    assertEquals("demo.example.com", request.remark());
    assertFalse(request.createFtp());
    assertFalse(request.createDatabase());
  }

  @Test
  @DisplayName("WebsiteCreateRequest should support optional ftp and database provisioning")
  void createRequestSupportsOptionalProvisioning() {
    WebsiteCreateRequest request =
        WebsiteCreateRequest.builder("demo.example.com", "/www/wwwroot/demo")
            .typeId(2)
            .projectType("Node")
            .phpVersion("no")
            .port(8080)
            .remark("Production site")
            .ftpAccount("demo_ftp", "ftp-secret")
            .database("demo_db", "db-secret", "utf8mb4")
            .build();

    assertTrue(request.createFtp());
    assertEquals("demo_ftp", request.ftpAccount().username());
    assertEquals("ftp-secret", request.ftpAccount().password());
    assertTrue(request.createDatabase());
    assertEquals("demo_db", request.database().username());
    assertEquals("db-secret", request.database().password());
    assertEquals("utf8mb4", request.database().charset());
  }

  @Test
  @DisplayName("WebsiteCreateRequest should require phpVersion before build")
  void createRequestRequiresPhpVersion() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> WebsiteCreateRequest.builder("demo.example.com", "/www/wwwroot/demo").build());

    assertEquals("phpVersion cannot be blank", exception.getMessage());
  }

  @Test
  @DisplayName("WebsiteCreateRequest should reject invalid nested credentials")
  void createRequestRejectsInvalidNestedCredentials() {
    IllegalArgumentException ftpException =
        assertThrows(
            IllegalArgumentException.class,
            () -> new WebsiteCreateRequest.FtpAccount(" ", "ftp-secret"));
    IllegalArgumentException databaseException =
        assertThrows(
            IllegalArgumentException.class,
            () -> new WebsiteCreateRequest.Database("demo_db", "db-secret", " "));

    assertEquals("ftp username cannot be blank", ftpException.getMessage());
    assertEquals("database charset cannot be blank", databaseException.getMessage());
  }
}
