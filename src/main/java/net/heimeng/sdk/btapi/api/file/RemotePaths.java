package net.heimeng.sdk.btapi.api.file;

import java.util.Set;

/**
 * 面板服务器路径的安全校验工具。
 *
 * <p>宝塔接口密钥拥有面板的全部权限，面板本身不会按调用方限制可操作的目录。SDK 因此在客户端做一层纵深防御：
 *
 * <ul>
 *   <li>只接受绝对路径，拒绝 {@code .}、{@code ..} 路径段和控制字符，防止目录穿越；
 *   <li>删除类操作拒绝系统与面板的关键目录本身（如 {@code /}、{@code /etc}、{@code /www/wwwroot}）。
 * </ul>
 *
 * <p>如果路径来自终端用户输入（例如多租户平台让用户删除自己的目录），调用方仍需用 {@link #isWithin(String, String)} 确认路径位于该用户被授权的目录内。SDK
 * 无法替调用方做业务层授权。
 */
public final class RemotePaths {

  /** 删除这些目录本身几乎必然是误操作，会破坏系统或面板。其子路径不受此限制。 */
  private static final Set<String> PROTECTED_DIRECTORIES =
      Set.of(
          "/",
          "/bin",
          "/boot",
          "/dev",
          "/etc",
          "/home",
          "/lib",
          "/lib32",
          "/lib64",
          "/opt",
          "/proc",
          "/root",
          "/run",
          "/sbin",
          "/srv",
          "/sys",
          "/tmp",
          "/usr",
          "/var",
          "/www",
          "/www/backup",
          "/www/server",
          "/www/wwwlogs",
          "/www/wwwroot");

  private RemotePaths() {}

  /**
   * 校验并规范化一个服务器绝对路径。
   *
   * <p>会合并重复的 {@code /} 并去掉末尾的 {@code /}。
   *
   * @param path 服务器上的路径
   * @return 规范化后的绝对路径
   * @throws IllegalArgumentException 路径为空、不是绝对路径、含控制字符或含 {@code .}/{@code ..} 路径段
   */
  public static String requireSafeAbsolutePath(String path) {
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException("Remote path cannot be blank");
    }
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException(
          "Remote path must be an absolute POSIX path starting with '/': " + path);
    }
    for (int i = 0; i < path.length(); i++) {
      if (Character.isISOControl(path.charAt(i))) {
        throw new IllegalArgumentException("Remote path cannot contain control characters");
      }
    }

    StringBuilder normalized = new StringBuilder();
    for (String segment : path.split("/")) {
      if (segment.isEmpty()) {
        continue;
      }
      if (".".equals(segment) || "..".equals(segment)) {
        throw new IllegalArgumentException(
            "Remote path cannot contain '.' or '..' segments: " + path);
      }
      normalized.append('/').append(segment);
    }
    return normalized.length() == 0 ? "/" : normalized.toString();
  }

  /**
   * 校验一个即将被删除的路径：必须是安全的绝对路径，且不是系统或面板的关键目录本身。
   *
   * @param path 待删除的路径
   * @return 规范化后的绝对路径
   * @throws IllegalArgumentException 路径不安全或属于受保护目录
   */
  public static String requireDeletablePath(String path) {
    String normalized = requireSafeAbsolutePath(path);
    if (PROTECTED_DIRECTORIES.contains(normalized)) {
      throw new IllegalArgumentException("Refusing to delete protected directory: " + normalized);
    }
    return normalized;
  }

  /**
   * 判断 {@code path} 是否等于 {@code baseDirectory} 或位于其内部。
   *
   * <p>按路径段比较，因此 {@code /www/wwwroot/site-a2} 不会被视为位于 {@code /www/wwwroot/site-a} 内。
   *
   * @param baseDirectory 被授权的根目录
   * @param path 待检查的路径
   * @return 位于根目录内时返回 {@code true}
   * @throws IllegalArgumentException 任一参数不是安全的绝对路径
   */
  public static boolean isWithin(String baseDirectory, String path) {
    String base = requireSafeAbsolutePath(baseDirectory);
    String candidate = requireSafeAbsolutePath(path);
    if ("/".equals(base)) {
      return true;
    }
    return candidate.equals(base) || candidate.startsWith(base + "/");
  }
}
