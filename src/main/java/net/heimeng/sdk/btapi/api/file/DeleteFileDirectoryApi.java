package net.heimeng.sdk.btapi.api.file;

/**
 * 删除目录的 API（面板 {@code files?action=DeleteDir}）。
 *
 * <p>该操作会连同目录内容一起删除。面板开启回收站时会移入回收站，而不是永久删除。面板的 {@code DeleteFile} 只能删除文件，对目录调用会返回“删除文件失败”。
 *
 * <p>路径会经过 {@link RemotePaths#requireDeletablePath(String)} 校验：必须是绝对路径，不能含 {@code ..}， 也不能是 {@code
 * /}、{@code /etc}、{@code /www/wwwroot} 等关键目录本身。
 *
 * @see <a href="https://docs.bt.cn/api/files/actions/">宝塔官方文档：文件管理</a>
 */
public class DeleteFileDirectoryApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=DeleteDir";

  public DeleteFileDirectoryApi() {
    super(ENDPOINT, "删除成功", "删除失败");
  }

  /**
   * 设置要删除的目录。
   *
   * @param path 目录的绝对路径
   * @return 当前 API 实例
   * @throws IllegalArgumentException 路径不安全或属于受保护目录
   */
  public DeleteFileDirectoryApi setPath(String path) {
    addParam("path", RemotePaths.requireDeletablePath(path));
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("path");
  }
}
