package net.heimeng.sdk.btapi.api.file;

/** 保存文件内容的 API。 */
public class SaveFileContentApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=SaveFileBody";

  public SaveFileContentApi() {
    super(ENDPOINT, "保存成功", "保存失败");
    addParam("encoding", "utf-8");
  }

  public SaveFileContentApi setPath(String path) {
    addParam("path", path);
    return this;
  }

  public SaveFileContentApi setData(String data) {
    addParam("data", data);
    return this;
  }

  public SaveFileContentApi setEncoding(String encoding) {
    addParam("encoding", encoding);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path", "encoding")
        && params.containsKey("data")
        && params.get("data") != null;
  }
}
