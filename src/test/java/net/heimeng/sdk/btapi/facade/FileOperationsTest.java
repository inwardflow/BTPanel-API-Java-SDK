package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("FileOperations 门面测试")
class FileOperationsTest {

  @Mock private BtClient client;

  @Test
  @DisplayName("列出目录应以 UI 默认参数委托到 GetDirNew")
  void listDelegatesWithUiDefaults() {
    FileOperations operations = new FileOperations(client);
    BtResult<DirectoryListing> response = new BtResult<>();
    response.setStatus(true);
    response.setData(new DirectoryListing());
    when(client.execute(any(GetDirectoryListingApi.class))).thenReturn(response);

    BtResult<DirectoryListing> result = operations.list("/www/wwwroot/site/");

    assertSame(response, result);
    verify(client)
        .execute(
            argThat(
                (GetDirectoryListingApi api) ->
                    "files?action=GetDirNew".equals(api.getEndpoint())
                        && "/www/wwwroot/site".equals(api.getParams().get("path"))
                        && "1".equals(api.getParams().get("p"))
                        && "500".equals(api.getParams().get("showRow"))));
  }

  @Test
  @DisplayName("分页列出目录应传递页码和每页条目数")
  void listWithPagingDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    BtResult<DirectoryListing> response = new BtResult<>();
    response.setStatus(true);
    when(client.execute(any(GetDirectoryListingApi.class))).thenReturn(response);

    operations.list("/www/wwwroot", 2, 50);

    verify(client)
        .execute(
            argThat(
                (GetDirectoryListingApi api) ->
                    "2".equals(api.getParams().get("p"))
                        && "50".equals(api.getParams().get("showRow"))));
  }

  @Test
  @DisplayName("列出目录时不安全的路径不应发出请求")
  void listRejectsUnsafePathBeforeSending() {
    FileOperations operations = new FileOperations(client);

    assertThrows(IllegalArgumentException.class, () -> operations.list("/www/../etc"));
    verify(client, never()).execute(any(GetDirectoryListingApi.class));
  }

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
  @DisplayName("删除目录应委托到 DeleteDir API")
  void deleteDirectoryDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(DeleteFileDirectoryApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.deleteDirectory("/www/test-dir");

    assertTrue(result.isSuccess());
    verify(client).execute(any(DeleteFileDirectoryApi.class));
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

    BtResult<Boolean> result = operations.move("/www/a.txt", "/backup/a.txt");

    assertTrue(result.isSuccess());
    verify(client)
        .execute(
            argThat(
                (MoveFileApi api) ->
                    "/www/a.txt".equals(api.getParams().get("sfile"))
                        && "/backup/a.txt".equals(api.getParams().get("dfile"))));
  }

  @Test
  @DisplayName("复制文件应委托到 CopyFileApi")
  void copyDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(CopyFileApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.copy("/www/a.txt", "/backup/a.txt");

    assertTrue(result.isSuccess());
    verify(client).execute(any(CopyFileApi.class));
  }

  @Test
  @SuppressWarnings("removal")
  @DisplayName("已弃用的 move(type=copy) 应走 CopyFile 而不是移动")
  void legacyMoveWithCopyTypeCopies() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(CopyFileApi.class))).thenReturn(successBoolean());

    operations.move("/www/a.txt", "/backup", "copy", 1);

    verify(client)
        .execute(
            argThat((CopyFileApi api) -> "/backup/a.txt".equals(api.getParams().get("dfile"))));
    verify(client, never()).execute(any(MoveFileApi.class));
  }

  @Test
  @DisplayName("压缩文件应委托到底层客户端")
  void compressDelegatesToClient() {
    FileOperations operations = new FileOperations(client);
    when(client.execute(any(CompressFileApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.compressTo("/www/site/logs", "/www/backup/logs.tar.gz", "tar.gz");

    assertTrue(result.isSuccess());
    verify(client)
        .execute(
            argThat(
                (CompressFileApi api) ->
                    "files?action=Zip".equals(api.getEndpoint())
                        && "logs".equals(api.getParams().get("sfile"))
                        && "/www/backup/logs.tar.gz".equals(api.getParams().get("dfile"))));
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
