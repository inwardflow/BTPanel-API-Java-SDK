package net.heimeng.sdk.btapi.facade;

import java.util.Objects;

/**
 * Website rewrite rules update options.
 *
 * @param name rewrite template name
 * @param content rewrite content, may be blank to clear current content
 */
public record WebsiteRewriteRulesOptions(String name, String content) {

  public WebsiteRewriteRulesOptions {
    requireNonBlank(name, "name");
    Objects.requireNonNull(content, "content cannot be null");
  }

  private static void requireNonBlank(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName + " cannot be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
  }
}
