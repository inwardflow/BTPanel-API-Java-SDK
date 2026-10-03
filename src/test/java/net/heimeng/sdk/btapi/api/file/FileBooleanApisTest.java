package net.heimeng.sdk.btapi.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("文件布尔型 API 单元测试")
class FileBooleanApisTest {

  @Test
  @DisplayName("创建目录 API 契约正确")
  void createDirectoryApiContract() {
    CreateFileDirectoryApi api = new CreateFileDirectoryApi().setPath("/www/test");

    assertEquals("files?action=CreateDir", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals("/www/test", api.getParams().get("path"));
    assertTrue(invokeValidate(api));

    BtResult<Boolean> result = api.parseResponse("{\"status\":true,\"msg\":\"ok\"}");
    assertTrue(result.isSuccess());
    assertTrue(result.getData());
  }

  @Test
  @DisplayName("删除目录 API 应使用 DeleteDir 端点")
  void deleteDirectoryApiContract() {
    DeleteFileDirectoryApi api = new DeleteFileDirectoryApi().setPath("/www/test-dir");

    assertEquals("files?action=DeleteDir", api.getEndpoint());
    assertEquals("/www/test-dir", api.getParams().get("path"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new DeleteFileDirectoryApi()));

    BtResult<Boolean> result = api.parseResponse("{\"status\":true,\"msg\":\"删除目录成功!\"}");
    assertTrue(result.isSuccess());
    assertTrue(result.getData());
  }

  @Test
  @DisplayName("删除文件 API 契约正确")
  void deleteFileApiContract() {
    DeleteFileApi api = new DeleteFileApi().setPath("/www/test.txt");

    assertEquals("files?action=DeleteFile", api.getEndpoint());
    assertEquals("/www/test.txt", api.getParams().get("path"));
    assertTrue(invokeValidate(api));

    BtResult<Boolean> result = api.parseResponse("{\"status\":false,\"msg\":\"denied\"}");
    assertFalse(result.isSuccess());
    assertFalse(result.getData());
    assertEquals("denied", result.getMsg());
  }

  @Test
  @DisplayName("重命名文件 API 应使用 MvFile 并拼接新路径")
  void renameFileApiContract() {
    RenameFileApi api =
        new RenameFileApi().setOldPath("/www/wwwroot/site/old.txt").setNewName("new.txt");

    assertEquals("files?action=MvFile", api.getEndpoint());
    assertEquals("/www/wwwroot/site/old.txt", api.getParams().get("sfile"));
    assertEquals("/www/wwwroot/site/new.txt", api.getParams().get("dfile"));
    assertEquals("true", api.getParams().get("rename"));
    assertTrue(invokeValidate(api));

    RenameFileApi reversed = new RenameFileApi().setNewName("b.txt").setOldPath("/a.txt");
    assertEquals("/b.txt", reversed.getParams().get("dfile"));
    assertThrows(IllegalArgumentException.class, () -> new RenameFileApi().setNewName("x/y"));
    assertFalse(invokeValidate(new RenameFileApi().setOldPath("/www/a.txt")));
  }

  @Test
  @DisplayName("移动文件 API 应使用 MvFile 和完整目标路径")
  void moveFileApiContract() {
    MoveFileApi api = new MoveFileApi().setSourcePath("/www/a.txt").setTargetPath("/backup/a.txt");

    assertEquals("files?action=MvFile", api.getEndpoint());
    assertEquals("/www/a.txt", api.getParams().get("sfile"));
    assertEquals("/backup/a.txt", api.getParams().get("dfile"));
    assertFalse(api.getParams().containsKey("rename"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @SuppressWarnings("removal")
  @DisplayName("已弃用的移动参数应拼接目标路径，并拒绝 copy")
  void moveFileApiLegacySetters() {
    MoveFileApi api =
        new MoveFileApi().setTarget("/backup/").setSource("/www/a.txt").setType("move");

    assertEquals("/backup/a.txt", api.getParams().get("dfile"));
    assertFalse(api.getParams().containsKey("type"));
    assertThrows(IllegalArgumentException.class, () -> new MoveFileApi().setType("copy"));
  }

  @Test
  @DisplayName("复制文件 API 应使用 CopyFile")
  void copyFileApiContract() {
    CopyFileApi api = new CopyFileApi().setSourcePath("/www/a.txt").setTargetPath("/backup/a.txt");

    assertEquals("files?action=CopyFile", api.getEndpoint());
    assertEquals("/www/a.txt", api.getParams().get("sfile"));
    assertEquals("/backup/a.txt", api.getParams().get("dfile"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new CopyFileApi().setSourcePath("/www/a.txt")));
  }

  @Test
  @DisplayName("保存文件内容 API 应保留默认编码")
  void saveFileContentApiKeepsDefaultEncoding() {
    SaveFileContentApi api = new SaveFileContentApi().setPath("/www/test.conf").setData("hello");

    assertEquals("files?action=SaveFileBody", api.getEndpoint());
    assertEquals("utf-8", api.getParams().get("encoding"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("压缩文件 API 契约正确")
  void compressFileApiContract() {
    CompressFileApi api =
        new CompressFileApi()
            .setSourcePath("/www/wwwroot/site/logs")
            .setArchivePath("/www/backup/logs.tar.gz")
            .setFormat("tar.gz");

    assertEquals("files?action=Zip", api.getEndpoint());
    assertEquals("/www/wwwroot/site/", api.getParams().get("path"));
    assertEquals("logs", api.getParams().get("sfile"));
    assertEquals("/www/backup/logs.tar.gz", api.getParams().get("dfile"));
    assertEquals("tar.gz", api.getParams().get("z_type"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new CompressFileApi().setSourcePath("/www/a")));
  }

  @Test
  @SuppressWarnings("removal")
  @DisplayName("已弃用的压缩参数应换算为 Zip 参数")
  void compressFileApiLegacySetters() {
    CompressFileApi api =
        new CompressFileApi().setPath("/www/site/logs").setFilename("backup").setFormat("zip");

    assertEquals("/www/site/", api.getParams().get("path"));
    assertEquals("logs", api.getParams().get("sfile"));
    assertEquals("/www/site/backup.zip", api.getParams().get("dfile"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("解压文件 API 契约正确")
  void uncompressFileApiContract() {
    UncompressFileApi api =
        new UncompressFileApi().setPath("/www/backup.tar.gz").setTarget("/www/output");

    assertEquals("files?action=UnZip", api.getEndpoint());
    assertEquals("/www/backup.tar.gz", api.getParams().get("sfile"));
    assertEquals("/www/output", api.getParams().get("dfile"));
    assertEquals("zip", api.getParams().get("type"));
    assertEquals("UTF-8", api.getParams().get("coding"));
    assertEquals("", api.getParams().get("password"));
    assertEquals("755", api.getParams().get("power"));
    assertTrue(invokeValidate(api));
    assertFalse(invokeValidate(new UncompressFileApi().setPath("/www/backup.zip")));
  }

  @Test
  @DisplayName("缺少状态字段的布尔响应应抛出异常")
  void booleanApisRejectMissingStatusField() {
    CreateFileDirectoryApi api = new CreateFileDirectoryApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"msg\":\"ok\"}"));

    assertTrue(exception.getMessage().contains("status field"));
  }

  @Test
  @DisplayName("无效 JSON 响应应抛出异常")
  void booleanApisRejectInvalidJson() {
    DeleteFileApi api = new DeleteFileApi();

    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("plain text"));

    assertTrue(exception.getMessage().contains("Invalid JSON"));
  }

  private boolean invokeValidate(Object api) {
    try {
      Method method = api.getClass().getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }
}
