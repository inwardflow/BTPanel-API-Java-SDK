package net.heimeng.sdk.btapi.api.file;

/** 压缩文件或目录的 API。 */
public class CompressFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=Compress";

  public CompressFileApi() {
    super(ENDPOINT, "压缩成功", "压缩失败");
  }

  public CompressFileApi setPath(String path) {
    addParam("path", path);
    return this;
  }

  public CompressFileApi setFilename(String filename) {
    addParam("filename", filename);
    return this;
  }

  public CompressFileApi setFormat(String format) {
    addParam("format", format);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path", "filename", "format");
  }
}
