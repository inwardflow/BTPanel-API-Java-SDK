package net.heimeng.sdk.btapi.facade;

import net.heimeng.sdk.btapi.api.file.CompressFileApi;
import net.heimeng.sdk.btapi.api.file.CopyFileApi;
import net.heimeng.sdk.btapi.api.file.CreateFileApi;
import net.heimeng.sdk.btapi.api.file.CreateFileDirectoryApi;
import net.heimeng.sdk.btapi.api.file.DeleteFileApi;
import net.heimeng.sdk.btapi.api.file.DeleteFileDirectoryApi;
import net.heimeng.sdk.btapi.api.file.GetDirectoryListingApi;
import net.heimeng.sdk.btapi.api.file.GetFileContentApi;
import net.heimeng.sdk.btapi.api.file.MoveFileApi;
import net.heimeng.sdk.btapi.api.file.RenameFileApi;
import net.heimeng.sdk.btapi.api.file.SaveFileContentApi;
import net.heimeng.sdk.btapi.api.file.UncompressFileApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.file.DirectoryListing;

/**
 * 文件相关能力的门面入口。
 *
 * <p>对常见的目录列表、文件读写、目录创建、移动重命名以及压缩解压操作提供更直接的调用方式。
 */
public final class FileOperations extends AbstractOperations {

  public FileOperations(BtClient client) {
    super(client);
  }

  /**
   * 列出目录内容的第一页（{@code files?action=GetDirNew}），每页 {@value GetDirectoryListingApi#DEFAULT_ROWS} 条，
   * 排序与面板 UI 默认一致。
   *
   * <p>面板在路径不存在或不是目录时会返回另一个目录的列表；SDK 检测到这种情况时抛出 {@link
   * net.heimeng.sdk.btapi.exception.BtApiException}，而不是返回错误的目录。
   *
   * @param path 目录的绝对路径
   * @return 目录列表，目录在前、文件在后；条目超过一页时用 {@link DirectoryListing#hasNextPage()} 判断并调用 {@link
   *     #list(String, int, int)} 翻页
   * @throws IllegalArgumentException 路径不是安全的绝对路径
   */
  public BtResult<DirectoryListing> list(String path) {
    return execute(new GetDirectoryListingApi().setPath(path));
  }

  /**
   * 分页列出目录内容（{@code files?action=GetDirNew}）。面板把目录和文件合并分页，目录排在前面。
   *
   * @param path 目录的绝对路径
   * @param page 页码，从 1 开始
   * @param rows 每页条目数（目录和文件合计）
   * @return 目录列表的一页
   * @throws IllegalArgumentException 路径不安全，或页码、条目数小于 1
   * @see #list(String)
   */
  public BtResult<DirectoryListing> list(String path, int page, int rows) {
    return execute(new GetDirectoryListingApi().setPath(path).setPage(page).setRows(rows));
  }

  public BtResult<String> getContent(String path) {
    return execute(new GetFileContentApi().setPath(path));
  }

  public BtResult<Boolean> saveContent(String path, String data, String encoding) {
    return execute(new SaveFileContentApi().setPath(path).setData(data).setEncoding(encoding));
  }

  public BtResult<Boolean> createDirectory(String path) {
    return execute(new CreateFileDirectoryApi().setPath(path));
  }

  public BtResult<Boolean> createFile(String path) {
    return execute(new CreateFileApi().setPath(path));
  }

  /** 删除文件。删除目录请使用 {@link #deleteDirectory(String)}。 */
  public BtResult<Boolean> delete(String path) {
    return execute(new DeleteFileApi().setPath(path));
  }

  public BtResult<Boolean> deleteDirectory(String path) {
    return execute(new DeleteFileDirectoryApi().setPath(path));
  }

  /**
   * 重命名文件或目录（{@code files?action=MvFile}，{@code rename=true}）。
   *
   * @param oldPath 原完整路径
   * @param newName 新名称（不含目录）
   * @return 操作结果
   */
  public BtResult<Boolean> rename(String oldPath, String newName) {
    return execute(new RenameFileApi().setOldPath(oldPath).setNewName(newName));
  }

  /**
   * 移动文件或目录（{@code files?action=MvFile}）。
   *
   * @param sourcePath 源完整路径
   * @param targetPath 目标完整路径（包含文件名），例如把 {@code /www/a.txt} 移到 {@code /backup/a.txt}
   * @return 操作结果
   */
  public BtResult<Boolean> move(String sourcePath, String targetPath) {
    return execute(new MoveFileApi().setSourcePath(sourcePath).setTargetPath(targetPath));
  }

  /**
   * 复制文件或目录（{@code files?action=CopyFile}）。
   *
   * @param sourcePath 源完整路径
   * @param targetPath 目标完整路径（包含文件名）
   * @return 操作结果
   */
  public BtResult<Boolean> copy(String sourcePath, String targetPath) {
    return execute(new CopyFileApi().setSourcePath(sourcePath).setTargetPath(targetPath));
  }

  /**
   * 移动或复制文件到目标目录。
   *
   * @param source 源完整路径
   * @param target 目标目录，目标路径按“目标目录 + 源文件名”拼接
   * @param type {@code move} 或 {@code copy}
   * @param moveType 已忽略
   * @return 操作结果
   * @deprecated 面板 9.0 用 {@code files?action=MvFile} 移动、用 {@code files?action=CopyFile}
   *     复制，二者都需要完整的目标路径。请改用 {@link #move(String, String)} 或 {@link #copy(String, String)}。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public BtResult<Boolean> move(String source, String target, String type, Integer moveType) {
    if ("copy".equals(type)) {
      String name = source.substring(source.lastIndexOf('/') + 1);
      String directory = target.endsWith("/") ? target.substring(0, target.length() - 1) : target;
      return copy(source, directory + "/" + name);
    }
    return execute(new MoveFileApi().setSource(source).setTarget(target).setType(type));
  }

  /**
   * 压缩文件或目录（{@code files?action=Zip}）。
   *
   * @param sourcePath 被压缩项的完整路径
   * @param archivePath 压缩包的完整路径，例如 {@code /www/backup/logs.tar.gz}
   * @param format 压缩格式，例如 {@code tar.gz} 或 {@code zip}
   * @return 操作结果
   */
  public BtResult<Boolean> compressTo(String sourcePath, String archivePath, String format) {
    return execute(
        new CompressFileApi()
            .setSourcePath(sourcePath)
            .setArchivePath(archivePath)
            .setFormat(format));
  }

  /**
   * 压缩文件或目录，压缩包放在被压缩项所在目录。
   *
   * @param path 被压缩项的完整路径
   * @param filename 压缩包文件名，未带扩展名时按格式补全
   * @param format 压缩格式
   * @return 操作结果
   * @deprecated 面板 9.0 用 {@code files?action=Zip} 压缩，需要压缩包的完整路径。请改用 {@link #compressTo(String,
   *     String, String)}。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public BtResult<Boolean> compress(String path, String filename, String format) {
    return execute(new CompressFileApi().setPath(path).setFilename(filename).setFormat(format));
  }

  /**
   * 解压压缩包（{@code files?action=UnZip}），使用 UI 默认的编码 {@code UTF-8} 和目录权限 {@code 755}。
   *
   * @param path 压缩包的完整路径
   * @param target 解压目录
   * @return 操作结果
   */
  public BtResult<Boolean> uncompress(String path, String target) {
    return execute(new UncompressFileApi().setPath(path).setTarget(target));
  }
}
