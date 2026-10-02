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
  @DisplayName("重命名文件 API 契约正确")
  void renameFileApiContract() {
    RenameFileApi api = new RenameFileApi().setOldPath("/www/old.txt").setNewName("new.txt");

    assertEquals("files?action=RenameFile", api.getEndpoint());
    assertEquals("/www/old.txt", api.getParams().get("oldpath"));
    assertEquals("new.txt", api.getParams().get("newname"));
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("移动文件 API 应校验操作类型")
  void moveFileApiValidatesType() {
    MoveFileApi api =
        new MoveFileApi().setSource("/www/a.txt").setTarget("/backup").setType("move");

    assertEquals("files?action=MoveFile", api.getEndpoint());
    assertTrue(invokeValidate(api));

    api.setType("invalid");
    assertFalse(invokeValidate(api));
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
        new CompressFileApi().setPath("/www").setFilename("backup").setFormat("zip");

    assertEquals("files?action=Compress", api.getEndpoint());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("解压文件 API 契约正确")
  void uncompressFileApiContract() {
    UncompressFileApi api =
        new UncompressFileApi().setPath("/www/backup.zip").setTarget("/www/output");

    assertEquals("files?action=UnCompress", api.getEndpoint());
    assertTrue(invokeValidate(api));
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
