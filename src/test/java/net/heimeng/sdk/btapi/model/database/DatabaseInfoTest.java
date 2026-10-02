package net.heimeng.sdk.btapi.model.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DatabaseInfo 模型测试")
class DatabaseInfoTest {

  @Test
  @DisplayName("应正确判断数据库是否为正常状态")
  void detectsNormalStatus() {
    DatabaseInfo databaseInfo = new DatabaseInfo();
    databaseInfo.setStatus(" Normal ");

    assertTrue(databaseInfo.isNormal());

    databaseInfo.setStatus("locked");
    assertFalse(databaseInfo.isNormal());
  }

  @Test
  @DisplayName("应按容量自动格式化大小")
  void formatsDatabaseSize() {
    DatabaseInfo databaseInfo = new DatabaseInfo();

    databaseInfo.setSize(0);
    assertEquals("0 KB", databaseInfo.getFormattedSize());

    databaseInfo.setSize(512);
    assertEquals("512 KB", databaseInfo.getFormattedSize());

    databaseInfo.setSize(2048);
    assertEquals("2.00 MB", databaseInfo.getFormattedSize());

    databaseInfo.setSize(2 * 1024 * 1024L);
    assertEquals("2.00 GB", databaseInfo.getFormattedSize());
  }
}
