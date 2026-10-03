package net.heimeng.sdk.btapi.api.file;

/** 删除文件的 API。删除目录请使用 {@link DeleteFileDirectoryApi}。 */
public class DeleteFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=DeleteFile";

  public DeleteFileApi() {
    super(ENDPOINT, "删除成功", "删除失败");
  }

  public DeleteFileApi setPath(String path) {
    addParam("path", path);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path");
  }
}
