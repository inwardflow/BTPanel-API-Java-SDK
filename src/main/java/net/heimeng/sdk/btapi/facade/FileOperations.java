package net.heimeng.sdk.btapi.facade;

import net.heimeng.sdk.btapi.api.file.CompressFileApi;
import net.heimeng.sdk.btapi.api.file.CopyFileApi;
import net.heimeng.sdk.btapi.api.file.CreateFileApi;
import net.heimeng.sdk.btapi.api.file.CreateFileDirectoryApi;
import net.heimeng.sdk.btapi.api.file.DeleteFileApi;
import net.heimeng.sdk.btapi.api.file.DeleteFileDirectoryApi;
import net.heimeng.sdk.btapi.api.file.GetFileContentApi;
import net.heimeng.sdk.btapi.api.file.MoveFileApi;
import net.heimeng.sdk.btapi.api.file.RenameFileApi;
import net.heimeng.sdk.btapi.api.file.SaveFileContentApi;
import net.heimeng.sdk.btapi.api.file.UncompressFileApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 文件相关能力的门面入口。
 *
 * <p>对常见的文件读写、目录创建、移动重命名以及压缩解压操作提供更直接的调用方式。
 */
public final class FileOperations extends AbstractOperations {

  public FileOperations(BtClient client) {
    super(client);
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

  public BtResult<Boolean> compress(String path, String filename, String format) {
    return execute(new CompressFileApi().setPath(path).setFilename(filename).setFormat(format));
  }

  public BtResult<Boolean> uncompress(String path, String target) {
    return execute(new UncompressFileApi().setPath(path).setTarget(target));
  }
}
