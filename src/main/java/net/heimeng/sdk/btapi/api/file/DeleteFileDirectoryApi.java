package net.heimeng.sdk.btapi.api.file;

/**
 * 删除目录的 API。
 *
 * <p>面板的 {@code DeleteFile} 只能删除文件，对目录调用会返回“删除文件失败”，目录必须使用 {@code DeleteDir}。
 */
public class DeleteFileDirectoryApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=DeleteDir";

  public DeleteFileDirectoryApi() {
    super(ENDPOINT, "删除成功", "删除失败");
  }

  public DeleteFileDirectoryApi setPath(String path) {
    addParam("path", path);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path");
  }
}
