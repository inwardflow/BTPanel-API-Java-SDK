package net.heimeng.sdk.btapi.api.file;

/**
 * 删除文件的 API（面板 {@code files?action=DeleteFile}）。删除目录请使用 {@link DeleteFileDirectoryApi}。
 *
 * <p>面板开启回收站时会移入回收站。路径会经过 {@link RemotePaths#requireDeletablePath(String)} 校验。
 *
 * @see <a href="https://docs.bt.cn/api/files/actions/">宝塔官方文档：文件管理</a>
 */
public class DeleteFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=DeleteFile";

  public DeleteFileApi() {
    super(ENDPOINT, "删除成功", "删除失败");
  }

  /**
   * 设置要删除的文件。
   *
   * @param path 文件的绝对路径
   * @return 当前 API 实例
   * @throws IllegalArgumentException 路径不安全或属于受保护目录
   */
  public DeleteFileApi setPath(String path) {
    addParam("path", RemotePaths.requireDeletablePath(path));
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path");
  }
}
