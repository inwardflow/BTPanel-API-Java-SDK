package net.heimeng.sdk.btapi.facade;

import java.util.Objects;

/**
 * Website password protection options.
 *
 * @param username protected area username
 * @param password protected area password
 */
public record WebsitePasswordProtectionOptions(String username, String password) {

  public WebsitePasswordProtectionOptions {
    requireNonBlank(username, "username");
    requireNonBlank(password, "password");
  }

  private static void requireNonBlank(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName + " cannot be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
  }
}
