package net.heimeng.sdk.btapi.api.file;

/**
 * 复制文件或目录的 API。
 *
 * <p>对应面板 9.0 文件管理的“复制/粘贴”：{@code files?action=CopyFile}，参数为源路径 {@code sfile} 和目标路径 {@code dfile}
 * （均为完整路径，目标路径包含新文件名）。
 */
public class CopyFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=CopyFile";

  public CopyFileApi() {
    super(ENDPOINT, "复制成功", "复制失败");
  }

  /**
   * 设置源路径。
   *
   * @param sourcePath 源文件或目录的完整路径
   * @return 当前 API 实例
   */
  public CopyFileApi setSourcePath(String sourcePath) {
    addParam("sfile", sourcePath);
    return this;
  }

  /**
   * 设置目标路径。
   *
   * @param targetPath 目标完整路径（包含文件名），例如 {@code /www/backup/a.txt}
   * @return 当前 API 实例
   */
  public CopyFileApi setTargetPath(String targetPath) {
    addParam("dfile", targetPath);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("sfile", "dfile");
  }
}
