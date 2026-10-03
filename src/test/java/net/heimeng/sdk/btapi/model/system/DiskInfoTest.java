package net.heimeng.sdk.btapi.model.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DiskInfo 模型测试")
class DiskInfoTest {

  @Test
  @DisplayName("应按顺序返回容量信息")
  void readsSizeSegments() {
    DiskInfo diskInfo = new DiskInfo();
    diskInfo.setSize(List.of("100G", "30G", "70G", "30%"));

    assertEquals("100G", diskInfo.getTotalSize());
    assertEquals("30G", diskInfo.getUsedSize());
    assertEquals("70G", diskInfo.getFreeSize());
    assertEquals("30%", diskInfo.getUsagePercentage());
  }

  @Test
  @DisplayName("缺失或空白容量信息时应返回 Unknown")
  void returnsUnknownForMissingValues() {
    DiskInfo diskInfo = new DiskInfo();
    diskInfo.setSize(Arrays.asList(" ", null));

    assertEquals("Unknown", diskInfo.getTotalSize());
    assertEquals("Unknown", diskInfo.getUsedSize());
    assertEquals("Unknown", diskInfo.getFreeSize());
    assertEquals("Unknown", diskInfo.getUsagePercentage());
  }
}
