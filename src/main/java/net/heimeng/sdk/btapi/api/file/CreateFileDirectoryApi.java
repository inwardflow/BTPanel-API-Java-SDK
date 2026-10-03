package net.heimeng.sdk.btapi.api.file;

/** 创建目录的 API。 */
public class CreateFileDirectoryApi extends AbstractFileBooleanApi {

  /**
   * 当前宝塔 9.x 页面真实写目录入口。
   *
   * <p>历史开发者接口文档中常见的是 {@code AddFolder}，但实际面板新版本页面已迁移到 {@code
   * CreateDir}。这里优先对齐当前面板行为，确保集成测试与真实环境一致。
   */
  private static final String ENDPOINT = "files?action=CreateDir";

  public CreateFileDirectoryApi() {
    super(ENDPOINT, "创建成功", "创建失败");
  }

  public CreateFileDirectoryApi setPath(String path) {
    addParam("path", path);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path");
  }
}
