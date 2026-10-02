package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("FileOperations 门面测试")
class FileOperationsTest {

  @Mock private BtClient client;

  @Test
  @DisplayName("读取文件内容应委托到底层客户端")
  void getContentDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    BtResult<String> response = new BtResult<>();
    response.setStatus(true);
    response.setData("content");
    when(client.execute(any(GetFileContentApi.class))).thenReturn(response);

    BtResult<String> result = operations.getContent("/www/test.conf");

    assertNotNull(result);
    assertTrue(result.isSuccess());
    verify(client).execute(any(GetFileContentApi.class));
  }

  @Test
  @DisplayName("保存文件内容应委托到底层客户端")
  void saveContentDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(SaveFileContentApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.saveContent("/www/test.conf", "data", "utf-8");

    assertTrue(result.isSuccess());
    verify(client).execute(any(SaveFileContentApi.class));
  }

  @Test
  @DisplayName("创建目录应委托到底层客户端")
  void createDirectoryDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(CreateFileDirectoryApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.createDirectory("/www/new-dir");

    assertTrue(result.isSuccess());
    verify(client).execute(any(CreateFileDirectoryApi.class));
  }

  @Test
  @DisplayName("创建文件应委托到底层客户端")
  void createFileDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(CreateFileApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.createFile("/www/new-file.txt");

    assertTrue(result.isSuccess());
    verify(client).execute(any(CreateFileApi.class));
  }

  @Test
  @DisplayName("删除文件应委托到底层客户端")
  void deleteDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(DeleteFileApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.delete("/www/test.conf");

    assertTrue(result.isSuccess());
    verify(client).execute(any(DeleteFileApi.class));
  }

  @Test
  @DisplayName("重命名文件应委托到底层客户端")
  void renameDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(RenameFileApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.rename("/www/test.conf", "prod.conf");

    assertTrue(result.isSuccess());
    verify(client).execute(any(RenameFileApi.class));
  }

  @Test
  @DisplayName("移动文件应委托到底层客户端")
  void moveDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(MoveFileApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.move("/www/a.txt", "/backup", "move", 1);

    assertTrue(result.isSuccess());
    verify(client).execute(any(MoveFileApi.class));
  }

  @Test
  @DisplayName("压缩文件应委托到底层客户端")
  void compressDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(CompressFileApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.compress("/www", "backup", "zip");

    assertTrue(result.isSuccess());
    verify(client).execute(any(CompressFileApi.class));
  }

  @Test
  @DisplayName("解压文件应委托到底层客户端")
  void uncompressDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(UncompressFileApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.uncompress("/www/backup.zip", "/www/output");

    assertTrue(result.isSuccess());
    verify(client).execute(any(UncompressFileApi.class));
  }

  private static BtResult<Boolean> successBoolean() {
    BtResult<Boolean> response = new BtResult<>();
    response.setStatus(true);
    response.setData(true);
    return response;
  }
}
