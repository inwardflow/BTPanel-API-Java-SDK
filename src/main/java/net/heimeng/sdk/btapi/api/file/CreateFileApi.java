package net.heimeng.sdk.btapi.api.file;

/** 创建空文件的 API。 */
public class CreateFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=CreateFile";

  public CreateFileApi() {
    super(ENDPOINT, "创建成功", "创建失败");
  }

  public CreateFileApi setPath(String path) {
    addParam("path", path);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path");
  }
}
