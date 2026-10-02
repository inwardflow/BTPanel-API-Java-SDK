package net.heimeng.sdk.btapi.api.file;

/** 解压文件的 API。 */
public class UncompressFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=UnCompress";

  public UncompressFileApi() {
    super(ENDPOINT, "解压成功", "解压失败");
  }

  public UncompressFileApi setPath(String path) {
    addParam("path", path);
    return this;
  }

  public UncompressFileApi setTarget(String target) {
    addParam("target", target);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path", "target");
  }
}
