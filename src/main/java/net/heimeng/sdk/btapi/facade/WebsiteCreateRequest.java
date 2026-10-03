package net.heimeng.sdk.btapi.facade;

import java.util.Objects;

/**
 * Immutable request model for website creation through the facade layer.
 *
 * <p>The builder keeps the common path concise while still allowing optional FTP and database
 * provisioning to be expressed explicitly.
 */
public record WebsiteCreateRequest(
    String domain,
    String path,
    int typeId,
    String projectType,
    String phpVersion,
    int port,
    String remark,
    FtpAccount ftpAccount,
    Database database) {

  private static final String DEFAULT_PROJECT_TYPE = "PHP";
  private static final int DEFAULT_TYPE_ID = 0;
  private static final int DEFAULT_PORT = 80;

  public WebsiteCreateRequest {
    domain = requireNonBlank(domain, "domain");
    path = requireNonBlank(path, "path");
    if (typeId < 0) {
      throw new IllegalArgumentException("typeId cannot be negative");
    }
    projectType = requireNonBlank(projectType, "projectType");
    phpVersion = requireNonBlank(phpVersion, "phpVersion");
    if (port <= 0) {
      throw new IllegalArgumentException("port must be positive");
    }
    remark = requireNonBlank(remark, "remark");
  }

  public static Builder builder(String domain, String path) {
    return new Builder(domain, path);
  }

  public boolean createFtp() {
    return ftpAccount != null;
  }

  public boolean createDatabase() {
    return database != null;
  }

  public record FtpAccount(String username, String password) {

    public FtpAccount {
      username = requireNonBlank(username, "ftp username");
      password = requireNonBlank(password, "ftp password");
    }
  }

  public record Database(String username, String password, String charset) {

    public Database {
      username = requireNonBlank(username, "database username");
      password = requireNonBlank(password, "database password");
      charset = requireNonBlank(charset, "database charset");
    }
  }

  public static final class Builder {

    private final String domain;
    private final String path;

    private int typeId = DEFAULT_TYPE_ID;
    private String projectType = DEFAULT_PROJECT_TYPE;
    private String phpVersion;
    private int port = DEFAULT_PORT;
    private String remark;
    private FtpAccount ftpAccount;
    private Database database;

    private Builder(String domain, String path) {
      this.domain = requireNonBlank(domain, "domain");
      this.path = requireNonBlank(path, "path");
      this.remark = this.domain;
    }

    public Builder typeId(int typeId) {
      if (typeId < 0) {
        throw new IllegalArgumentException("typeId cannot be negative");
      }
      this.typeId = typeId;
      return this;
    }

    public Builder projectType(String projectType) {
      this.projectType = requireNonBlank(projectType, "projectType");
      return this;
    }

    public Builder phpVersion(String phpVersion) {
      this.phpVersion = requireNonBlank(phpVersion, "phpVersion");
      return this;
    }

    public Builder port(int port) {
      if (port <= 0) {
        throw new IllegalArgumentException("port must be positive");
      }
      this.port = port;
      return this;
    }

    public Builder remark(String remark) {
      this.remark = requireNonBlank(remark, "remark");
      return this;
    }

    public Builder ftpAccount(String username, String password) {
      return ftpAccount(new FtpAccount(username, password));
    }

    public Builder ftpAccount(FtpAccount ftpAccount) {
      this.ftpAccount = Objects.requireNonNull(ftpAccount, "ftpAccount cannot be null");
      return this;
    }

    public Builder database(String username, String password, String charset) {
      return database(new Database(username, password, charset));
    }

    public Builder database(Database database) {
      this.database = Objects.requireNonNull(database, "database cannot be null");
      return this;
    }

    public WebsiteCreateRequest build() {
      return new WebsiteCreateRequest(
          domain, path, typeId, projectType, phpVersion, port, remark, ftpAccount, database);
    }
  }

  private static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
    return value;
  }
}
