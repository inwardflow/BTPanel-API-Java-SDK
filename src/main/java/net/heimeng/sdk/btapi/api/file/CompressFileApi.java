package net.heimeng.sdk.btapi.api.file;

/**
 * 压缩文件或目录的 API。
 *
 * <p>对应面板 9.0 文件管理的“创建压缩”：{@code files?action=Zip}，参数为：
 *
 * <ul>
 *   <li>{@code path}：被压缩项所在目录，以 {@code /} 结尾；
 *   <li>{@code sfile}：被压缩项在该目录下的名称；
 *   <li>{@code dfile}：压缩包的完整路径；
 *   <li>{@code z_type}：压缩格式，例如 {@code tar.gz}、{@code zip}。
 * </ul>
 *
 * <p>推荐用 {@link #setSourcePath(String)} 一次设置 {@code path} 和 {@code sfile}。旧版本 SDK 使用的 {@code
 * files?action=Compress} 不在 9.0 UI 中。
 */
public class CompressFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=Zip";

  private String legacyPath;
  private String legacyFilename;

  public CompressFileApi() {
    super(ENDPOINT, "压缩成功", "压缩失败");
  }

  /**
   * 设置要压缩的文件或目录的完整路径，自动拆分为 {@code path}（所在目录）和 {@code sfile}（名称）。
   *
   * @param sourcePath 被压缩项的完整路径，例如 {@code /www/wwwroot/site/logs}
   * @return 当前 API 实例
   */
  public CompressFileApi setSourcePath(String sourcePath) {
    addParam("path", withTrailingSlash(FilePathSupport.parentOf(sourcePath)));
    addParam("sfile", FilePathSupport.baseName(sourcePath));
    return this;
  }

  /**
   * 设置压缩包的完整路径。
   *
   * @param archivePath 压缩包路径，例如 {@code /www/backup/logs.tar.gz}
   * @return 当前 API 实例
   */
  public CompressFileApi setArchivePath(String archivePath) {
    addParam("dfile", archivePath);
    return this;
  }

  /**
   * 设置压缩格式，对应请求参数 {@code z_type}。
   *
   * @param format 压缩格式，例如 {@code tar.gz}（UI 默认）或 {@code zip}
   * @return 当前 API 实例
   */
  public CompressFileApi setFormat(String format) {
    addParam("z_type", format);
    updateLegacyArchivePath();
    return this;
  }

  /**
   * 设置要压缩的文件或目录。
   *
   * @param path 被压缩项的完整路径
   * @return 当前 API 实例
   * @deprecated 面板的 {@code Zip} 需要拆分的 {@code path}/{@code sfile} 参数。请改用 {@link
   *     #setSourcePath(String)}。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public CompressFileApi setPath(String path) {
    this.legacyPath = path;
    setSourcePath(path);
    updateLegacyArchivePath();
    return this;
  }

  /**
   * 设置压缩包文件名，压缩包放在被压缩项所在目录；未带扩展名时按压缩格式补全。
   *
   * @param filename 压缩包文件名
   * @return 当前 API 实例
   * @deprecated 面板的 {@code Zip} 需要压缩包的完整路径 {@code dfile}。请改用 {@link #setArchivePath(String)}。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public CompressFileApi setFilename(String filename) {
    this.legacyFilename = FilePathSupport.requireFileName(filename, "filename");
    updateLegacyArchivePath();
    return this;
  }

  private void updateLegacyArchivePath() {
    if (legacyPath == null || legacyFilename == null) {
      return;
    }
    Object format = params.get("z_type");
    String name = legacyFilename;
    if (format instanceof String type && !type.isBlank() && !name.endsWith("." + type)) {
      name = name + "." + type;
    }
    addParam("dfile", FilePathSupport.join(FilePathSupport.parentOf(legacyPath), name));
  }

  private static String withTrailingSlash(String directory) {
    return directory.endsWith("/") ? directory : directory + "/";
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path", "sfile", "dfile", "z_type");
  }
}
