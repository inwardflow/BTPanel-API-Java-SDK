package net.heimeng.sdk.btapi.facade;

/**
 * Website traffic limiting options.
 *
 * @param enabled whether rate limiting is enabled
 * @param perServer maximum concurrent connections per server
 * @param perIp maximum concurrent connections per IP
 * @param limitRate bandwidth limit
 */
public record WebsiteLimitNetOptions(
    boolean enabled, Integer perServer, Integer perIp, Integer limitRate) {

  public WebsiteLimitNetOptions {
    validateNonNegative(perServer, "perServer");
    validateNonNegative(perIp, "perIp");
    validateNonNegative(limitRate, "limitRate");
  }

  public static WebsiteLimitNetOptions disabled() {
    return new WebsiteLimitNetOptions(false, null, null, null);
  }

  private static void validateNonNegative(Integer value, String fieldName) {
    if (value != null && value < 0) {
      throw new IllegalArgumentException(fieldName + " cannot be negative");
    }
  }
}
