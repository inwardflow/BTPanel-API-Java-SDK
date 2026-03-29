package net.heimeng.sdk.btapi.facade;

import java.util.Objects;

/**
 * Website Nginx config update options.
 *
 * @param domain website domain whose config should be updated
 * @param content config content, may be blank to clear content
 */
public record WebsiteNginxConfigOptions(String domain, String content) {

  public WebsiteNginxConfigOptions {
    requireNonBlank(domain, "domain");
    Objects.requireNonNull(content, "content cannot be null");
  }

  private static void requireNonBlank(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName + " cannot be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
  }
}
