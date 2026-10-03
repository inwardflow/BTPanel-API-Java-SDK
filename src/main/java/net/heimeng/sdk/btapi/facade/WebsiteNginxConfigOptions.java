package net.heimeng.sdk.btapi.facade;

import java.util.Objects;

/**
 * Website Nginx config update options.
 *
 * @param domain website domain whose config should be updated
 * @param content config content, may be blank to clear content
 * @deprecated 仅供已弃用的 {@link WebsiteOperations#updateNginxConfig(Integer,
 *     WebsiteNginxConfigOptions)} 使用。请改用 {@link WebsiteOperations#updateNginxConfig(String,
 *     String)}。
 */
@Deprecated(since = "0.2.0", forRemoval = true)
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
