package net.heimeng.sdk.btapi.facade;

import java.util.Objects;

/**
 * Website rewrite rules update options.
 *
 * @param name rewrite template name
 * @param content rewrite content, may be blank to clear current content
 * @deprecated 仅供已弃用的 {@link WebsiteOperations#updateRewriteRules(int, WebsiteRewriteRulesOptions)}
 *     使用。请改用 {@link WebsiteOperations#updateRewriteRules(String, String)}。
 */
@Deprecated(since = "0.2.0", forRemoval = true)
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
