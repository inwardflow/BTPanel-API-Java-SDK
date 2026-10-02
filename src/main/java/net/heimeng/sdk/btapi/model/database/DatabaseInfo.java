package net.heimeng.sdk.btapi.model.database;

import java.util.Date;

import lombok.Data;

/**
 * 数据库信息模型。
 *
 * <p>用于承载数据库的基础信息，并提供少量便于展示的辅助方法。
 */
@Data
public class DatabaseInfo {

  /** 数据库 ID。 */
  private int id;

  /** 数据库名称。 */
  private String name;

  /** 数据库用户名。 */
  private String username;

  /** 数据库类型。 */
  private String type;

  /** 数据库大小，单位为 KB。 */
  private long size;

  /** 字符集。 */
  private String charset;

  /** 创建时间。 */
  private Date createTime;

  /** 备注信息。 */
  private String description;

  /** 数据库状态，例如 normal、locked。 */
  private String status;

  /** 判断数据库是否处于正常状态。 */
  public boolean isNormal() {
    return status != null && "normal".equalsIgnoreCase(status.trim());
  }

  /** 获取适合展示的数据库容量字符串。 */
  public String getFormattedSize() {
    if (size <= 0) {
      return "0 KB";
    }
    if (size < 1024) {
      return size + " KB";
    }
    if (size < 1024 * 1024) {
      return String.format("%.2f MB", size / 1024.0);
    }
    return String.format("%.2f GB", size / (1024.0 * 1024.0));
  }
}
