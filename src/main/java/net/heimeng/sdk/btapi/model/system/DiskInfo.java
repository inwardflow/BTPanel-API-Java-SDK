package net.heimeng.sdk.btapi.model.system;

import java.util.List;

import lombok.Data;

/**
 * 磁盘分区信息模型。
 *
 * <p>用于表达挂载点、Inode 使用情况和容量使用情况等信息。
 */
@Data
public class DiskInfo {

  /** 分区挂载点。 */
  private String path;

  /** Inode 使用信息：[总量, 已使用, 可用, 使用率]。 */
  private List<String> inodes;

  /** 容量使用信息：[总量, 已使用, 可用, 使用率]。 */
  private List<String> size;

  /** 获取总容量。 */
  public String getTotalSize() {
    return readSizeValue(0);
  }

  /** 获取已使用容量。 */
  public String getUsedSize() {
    return readSizeValue(1);
  }

  /** 获取可用容量。 */
  public String getFreeSize() {
    return readSizeValue(2);
  }

  /** 获取使用率。 */
  public String getUsagePercentage() {
    return readSizeValue(3);
  }

  private String readSizeValue(int index) {
    if (size == null || size.size() <= index) {
      return "Unknown";
    }
    String value = size.get(index);
    return value == null || value.isBlank() ? "Unknown" : value;
  }
}
