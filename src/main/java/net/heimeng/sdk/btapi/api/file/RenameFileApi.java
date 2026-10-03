package net.heimeng.sdk.btapi.api.file;

/** 重命名文件或目录的 API。 */
public class RenameFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=RenameFile";

  public RenameFileApi() {
    super(ENDPOINT, "重命名成功", "重命名失败");
  }

  public RenameFileApi setOldPath(String oldPath) {
    addParam("oldpath", oldPath);
    return this;
  }

  public RenameFileApi setNewName(String newName) {
    addParam("newname", newName);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("oldpath", "newname");
  }
}
