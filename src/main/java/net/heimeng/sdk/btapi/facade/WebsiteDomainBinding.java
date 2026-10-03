package net.heimeng.sdk.btapi.facade;

import java.util.Objects;

/**
 * Website domain binding request.
 *
 * @param websiteName website display or primary domain name expected by BT Panel
 * @param domain domain to bind
 */
public record WebsiteDomainBinding(String websiteName, String domain) {

  public WebsiteDomainBinding {
    requireNonBlank(websiteName, "websiteName");
    requireNonBlank(domain, "domain");
  }

  private static void requireNonBlank(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName + " cannot be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
  }
}
