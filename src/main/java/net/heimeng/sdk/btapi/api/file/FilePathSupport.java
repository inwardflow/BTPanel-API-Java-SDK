package net.heimeng.sdk.btapi.api.file;

/** 文件 API 内部使用的服务器路径拼接工具。 */
final class FilePathSupport {

  private FilePathSupport() {}

  /** 返回路径的父目录；根目录下的文件返回 {@code /}。 */
  static String parentOf(String path) {
    String normalized = stripTrailingSlash(path);
    int index = normalized.lastIndexOf('/');
    return index <= 0 ? "/" : normalized.substring(0, index);
  }

  /** 返回路径的最后一段。 */
  static String baseName(String path) {
    String normalized = stripTrailingSlash(path);
    return normalized.substring(normalized.lastIndexOf('/') + 1);
  }

  /** 在目录下拼接子项名称。 */
  static String join(String directory, String name) {
    String normalized = stripTrailingSlash(directory);
    return "/".equals(normalized) ? "/" + name : normalized + "/" + name;
  }

  /** 校验文件名不包含路径分隔符。 */
  static String requireFileName(String name, String paramName) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException(paramName + " cannot be blank");
    }
    if (name.contains("/") || name.contains("\\") || ".".equals(name) || "..".equals(name)) {
      throw new IllegalArgumentException(paramName + " must be a plain file name: " + name);
    }
    return name;
  }

  private static String stripTrailingSlash(String path) {
    String value = path;
    while (value.length() > 1 && value.endsWith("/")) {
      value = value.substring(0, value.length() - 1);
    }
    return value;
  }
}
