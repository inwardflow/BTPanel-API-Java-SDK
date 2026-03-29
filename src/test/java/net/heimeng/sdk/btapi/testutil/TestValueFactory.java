package net.heimeng.sdk.btapi.testutil;

public final class TestValueFactory {

  private static final String SAMPLE_DATABASE_NAME = "sample-database";
  private static final String SAMPLE_DATABASE_USER = "sample-user";
  private static final String SAMPLE_PASSWORD = "sample-value";
  private static final String UPDATED_SAMPLE_PASSWORD = "changed-value";
  private static final String SAMPLE_FTP_USER = "sample-ftp-user";
  private static final String SAMPLE_FTP_PATH = "/www/wwwroot/sample-site";
  private static final String SAMPLE_FTP_REMARK = "Sample FTP";
  private static final String SAMPLE_DOMAIN = "sample.example.com";
  private static final String SAMPLE_WWW_DOMAIN = "www.sample.example.com";
  private static final String SAMPLE_SITE_PATH = "/www/wwwroot/sample-site";

  private TestValueFactory() {}

  public static String sampleDatabaseName() {
    return SAMPLE_DATABASE_NAME;
  }

  public static String sampleDatabaseUser() {
    return SAMPLE_DATABASE_USER;
  }

  public static String samplePassword() {
    return SAMPLE_PASSWORD;
  }

  public static String updatedSamplePassword() {
    return UPDATED_SAMPLE_PASSWORD;
  }

  public static String sampleFtpUser() {
    return SAMPLE_FTP_USER;
  }

  public static String sampleFtpPath() {
    return SAMPLE_FTP_PATH;
  }

  public static String sampleFtpRemark() {
    return SAMPLE_FTP_REMARK;
  }

  public static String sampleDomain() {
    return SAMPLE_DOMAIN;
  }

  public static String sampleWwwDomain() {
    return SAMPLE_WWW_DOMAIN;
  }

  public static String sampleSitePath() {
    return SAMPLE_SITE_PATH;
  }

  public static String sampleDatabaseRemark() {
    return SAMPLE_DATABASE_NAME;
  }

  public static String integrationFtpUser(String suffix) {
    return "ftp" + suffix;
  }

  public static String integrationPassword(String suffix, char variant) {
    return "pw" + suffix + Character.toUpperCase(variant) + "9Z";
  }

  public static String integrationFtpBaseSegment(String suffix) {
    return "ftpcase-" + suffix;
  }
}
