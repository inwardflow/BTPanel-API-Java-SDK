package net.heimeng.sdk.btapi.api.file;

/**
 * 移动文件或目录的 API。
 *
 * <p>对应面板 9.0 文件管理的“剪切/粘贴”：{@code files?action=MvFile}，参数为源路径 {@code sfile} 和目标路径 {@code dfile}
 * （均为完整路径，目标路径包含文件名）。旧版本 SDK 使用的 {@code files?action=MoveFile} 不在 9.0 UI 中。复制请使用 {@link
 * CopyFileApi}。
 */
public class MoveFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=MvFile";

  private String legacySource;
  private String legacyTargetDirectory;

  public MoveFileApi() {
    super(ENDPOINT, "操作成功", "操作失败");
  }

  /**
   * 设置源路径。
   *
   * @param sourcePath 源文件或目录的完整路径
   * @return 当前 API 实例
   */
  public MoveFileApi setSourcePath(String sourcePath) {
    addParam("sfile", sourcePath);
    return this;
  }

  /**
   * 设置目标路径。
   *
   * @param targetPath 目标完整路径（包含文件名），例如 {@code /www/backup/a.txt}
   * @return 当前 API 实例
   */
  public MoveFileApi setTargetPath(String targetPath) {
    addParam("dfile", targetPath);
    return this;
  }

  /**
   * 设置源路径。
   *
   * @param source 源文件或目录的完整路径
   * @return 当前 API 实例
   * @deprecated 请改用 {@link #setSourcePath(String)}。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public MoveFileApi setSource(String source) {
    this.legacySource = source;
    updateLegacyTarget();
    return setSourcePath(source);
  }

  /**
   * 设置目标目录。目标路径按“目标目录 + 源文件名”拼接。
   *
   * @param target 目标目录
   * @return 当前 API 实例
   * @deprecated 面板的 {@code MvFile} 需要包含文件名的完整目标路径。请改用 {@link #setTargetPath(String)}。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public MoveFileApi setTarget(String target) {
    this.legacyTargetDirectory = target;
    updateLegacyTarget();
    return this;
  }

  /**
   * 设置操作类型。
   *
   * @param type 只接受 {@code move}
   * @return 当前 API 实例
   * @throws IllegalArgumentException 传入 {@code move} 以外的值，例如 {@code copy}
   * @deprecated 移动与复制是面板的两个不同 action。移动直接使用本类，复制请改用 {@link CopyFileApi}。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public MoveFileApi setType(String type) {
    if (!"move".equals(type)) {
      throw new IllegalArgumentException(
          "MoveFileApi only moves files; use CopyFileApi to copy (type=" + type + ")");
    }
    return this;
  }

  /**
   * 该参数不再发送。
   *
   * @param moveType 已忽略
   * @return 当前 API 实例
   * @deprecated 面板 UI 的 {@code MvFile} 请求没有该参数，设置后不会发送。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public MoveFileApi setMoveType(Integer moveType) {
    return this;
  }

  private void updateLegacyTarget() {
    if (legacySource != null
        && !legacySource.isBlank()
        && legacyTargetDirectory != null
        && !legacyTargetDirectory.isBlank()) {
      setTargetPath(
          FilePathSupport.join(legacyTargetDirectory, FilePathSupport.baseName(legacySource)));
    }
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("sfile", "dfile");
  }
}
