package net.heimeng.sdk.btapi.api.file;

/**
 * 重命名文件或目录的 API。
 *
 * <p>对应面板 9.0 文件管理右键菜单的“重命名”：{@code files?action=MvFile}，参数为原路径 {@code sfile}、新路径 {@code dfile} 和
 * {@code rename=true}。新路径由原路径所在目录与新名称拼接得到。旧版本 SDK 使用的 {@code files?action=RenameFile} 不在 9.0 UI 中。
 */
public class RenameFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=MvFile";

  private String oldPath;
  private String newName;

  public RenameFileApi() {
    super(ENDPOINT, "重命名成功", "重命名失败");
    addParam("rename", "true");
  }

  /**
   * 设置要重命名的文件或目录的完整路径。
   *
   * @param oldPath 原路径，例如 {@code /www/wwwroot/site/old.txt}
   * @return 当前 API 实例
   */
  public RenameFileApi setOldPath(String oldPath) {
    this.oldPath = oldPath;
    addParam("sfile", oldPath);
    updateTargetPath();
    return this;
  }

  /**
   * 设置新名称（不含目录）。
   *
   * @param newName 新名称，例如 {@code new.txt}
   * @return 当前 API 实例
   * @throws IllegalArgumentException 名称为空或包含路径分隔符
   */
  public RenameFileApi setNewName(String newName) {
    this.newName = FilePathSupport.requireFileName(newName, "newName");
    updateTargetPath();
    return this;
  }

  private void updateTargetPath() {
    if (oldPath != null && !oldPath.isBlank() && newName != null) {
      addParam("dfile", FilePathSupport.join(FilePathSupport.parentOf(oldPath), newName));
    }
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("sfile", "dfile");
  }
}
