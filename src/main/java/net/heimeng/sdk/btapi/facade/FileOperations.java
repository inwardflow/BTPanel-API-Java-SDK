package net.heimeng.sdk.btapi.facade;

import net.heimeng.sdk.btapi.api.file.CompressFileApi;
import net.heimeng.sdk.btapi.api.file.CreateFileApi;
import net.heimeng.sdk.btapi.api.file.CreateFileDirectoryApi;
import net.heimeng.sdk.btapi.api.file.DeleteFileApi;
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

  public BtResult<Boolean> delete(String path) {
    return execute(new DeleteFileApi().setPath(path));
  }

  public BtResult<Boolean> rename(String oldPath, String newName) {
    return execute(new RenameFileApi().setOldPath(oldPath).setNewName(newName));
  }

  public BtResult<Boolean> move(String source, String target, String type, Integer moveType) {
    return execute(
        new MoveFileApi().setSource(source).setTarget(target).setType(type).setMoveType(moveType));
  }

  public BtResult<Boolean> compress(String path, String filename, String format) {
    return execute(new CompressFileApi().setPath(path).setFilename(filename).setFormat(format));
  }

  public BtResult<Boolean> uncompress(String path, String target) {
    return execute(new UncompressFileApi().setPath(path).setTarget(target));
  }
}
