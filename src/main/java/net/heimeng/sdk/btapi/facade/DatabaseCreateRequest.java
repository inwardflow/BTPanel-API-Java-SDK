package net.heimeng.sdk.btapi.facade;

/**
 * Immutable request model for database creation through the facade layer.
 *
 * <p>Defaults mirror the current panel conventions while keeping optional database provisioning
 * details explicit and discoverable.
 */
public record DatabaseCreateRequest(
    String databaseName,
    String username,
    String password,
    Type type,
    String charset,
    String remark,
    String dataAccess,
    String address,
    String listenIp,
    String host,
    int sid) {

  private static final Type DEFAULT_TYPE = Type.MYSQL;
  private static final String DEFAULT_CHARSET = "utf8mb4";
  private static final String DEFAULT_DATA_ACCESS = "%";
  private static final String DEFAULT_ADDRESS = "%";
  private static final String DEFAULT_LISTEN_IP = "0.0.0.0/0";
  private static final String DEFAULT_HOST = "%";
  private static final int DEFAULT_SID = 0;

  public DatabaseCreateRequest {
    databaseName = requireNonBlank(databaseName, "databaseName");
    username = requireNonBlank(username, "username");
    password = requireNonBlank(password, "password");
    type = requireNonNull(type, "type");
    charset = requireNonBlank(charset, "charset");
    remark = requireNonBlank(remark, "remark");
    dataAccess = requireNonBlank(dataAccess, "dataAccess");
    address = requireNonBlank(address, "address");
    listenIp = requireNonBlank(listenIp, "listenIp");
    host = requireNonBlank(host, "host");
    if (sid < 0) {
      throw new IllegalArgumentException("sid cannot be negative");
    }
  }

  public static Builder builder(String databaseName, String username, String password) {
    return new Builder(databaseName, username, password);
  }

  public enum Type {
    MYSQL("MySQL"),
    MONGODB("MongoDb");

    private final String apiValue;

    Type(String apiValue) {
      this.apiValue = apiValue;
    }

    public String apiValue() {
      return apiValue;
    }
  }

  public static final class Builder {

    private final String databaseName;
    private final String username;
    private final String password;

    private Type type = DEFAULT_TYPE;
    private String charset = DEFAULT_CHARSET;
    private String remark;
    private String dataAccess = DEFAULT_DATA_ACCESS;
    private String address = DEFAULT_ADDRESS;
    private String listenIp = DEFAULT_LISTEN_IP;
    private String host = DEFAULT_HOST;
    private int sid = DEFAULT_SID;

    private Builder(String databaseName, String username, String password) {
      this.databaseName = requireNonBlank(databaseName, "databaseName");
      this.username = requireNonBlank(username, "username");
      this.password = requireNonBlank(password, "password");
      this.remark = this.databaseName;
    }

    public Builder type(Type type) {
      this.type = requireNonNull(type, "type");
      return this;
    }

    public Builder charset(String charset) {
      this.charset = requireNonBlank(charset, "charset");
      return this;
    }

    public Builder remark(String remark) {
      this.remark = requireNonBlank(remark, "remark");
      return this;
    }

    public Builder dataAccess(String dataAccess) {
      this.dataAccess = requireNonBlank(dataAccess, "dataAccess");
      return this;
    }

    public Builder address(String address) {
      this.address = requireNonBlank(address, "address");
      return this;
    }

    public Builder listenIp(String listenIp) {
      this.listenIp = requireNonBlank(listenIp, "listenIp");
      return this;
    }

    public Builder host(String host) {
      this.host = requireNonBlank(host, "host");
      return this;
    }

    public Builder sid(int sid) {
      if (sid < 0) {
        throw new IllegalArgumentException("sid cannot be negative");
      }
      this.sid = sid;
      return this;
    }

    public DatabaseCreateRequest build() {
      return new DatabaseCreateRequest(
          databaseName, username, password, type, charset, remark, dataAccess, address, listenIp,
          host, sid);
    }
  }

  private static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
    return value;
  }

  private static <T> T requireNonNull(T value, String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + " cannot be null");
    }
    return value;
  }
}
