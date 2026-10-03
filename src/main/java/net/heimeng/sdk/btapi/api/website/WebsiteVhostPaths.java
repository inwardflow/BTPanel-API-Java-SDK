package net.heimeng.sdk.btapi.api.website;

import net.heimeng.sdk.btapi.api.file.RemotePaths;

/**
 * 站点 vhost 配置文件的服务器路径。
 *
 * <p>面板 9.0 的站点设置并不通过 {@code site} 模块读写伪静态和配置文件，而是直接调用文件模块的 {@code files?action=GetFileBody} /
 * {@code files?action=SaveFileBody}，路径为：
 *
 * <ul>
 *   <li>Nginx 配置文件：{@code /www/server/panel/vhost/nginx/<站点名>.conf}
 *   <li>Nginx 伪静态规则：{@code /www/server/panel/vhost/rewrite/<站点名>.conf}
 * </ul>
 *
 * <p>以上路径基于宝塔默认安装目录 {@code /www/server/panel}，且仅适用于 Nginx 站点。
 */
public final class WebsiteVhostPaths {

  /** 宝塔默认安装下的 vhost 目录。 */
  public static final String VHOST_DIRECTORY = "/www/server/panel/vhost";

  private WebsiteVhostPaths() {}

  /**
   * 返回站点 Nginx 配置文件的路径。
   *
   * @param siteName 站点名（主域名），例如 {@code example.com}
   * @return {@code /www/server/panel/vhost/nginx/<siteName>.conf}
   * @throws IllegalArgumentException 站点名为空或包含路径分隔符、{@code ..}、控制字符
   */
  public static String nginxConfig(String siteName) {
    return RemotePaths.requireSafeAbsolutePath(
        VHOST_DIRECTORY + "/nginx/" + requireSiteName(siteName) + ".conf");
  }

  /**
   * 返回站点 Nginx 伪静态规则文件的路径。
   *
   * @param siteName 站点名（主域名），例如 {@code example.com}
   * @return {@code /www/server/panel/vhost/rewrite/<siteName>.conf}
   * @throws IllegalArgumentException 站点名为空或包含路径分隔符、{@code ..}、控制字符
   */
  public static String rewriteConfig(String siteName) {
    return RemotePaths.requireSafeAbsolutePath(
        VHOST_DIRECTORY + "/rewrite/" + requireSiteName(siteName) + ".conf");
  }

  private static String requireSiteName(String siteName) {
    if (siteName == null || siteName.isBlank()) {
      throw new IllegalArgumentException("siteName cannot be blank");
    }
    String trimmed = siteName.trim();
    if (trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains("..")) {
      throw new IllegalArgumentException("siteName must not contain path segments: " + siteName);
    }
    for (int i = 0; i < trimmed.length(); i++) {
      if (Character.isISOControl(trimmed.charAt(i))) {
        throw new IllegalArgumentException("siteName must not contain control characters");
      }
    }
    return trimmed;
  }
}
