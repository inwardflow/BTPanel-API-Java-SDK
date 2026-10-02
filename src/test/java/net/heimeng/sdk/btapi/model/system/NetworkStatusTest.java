package net.heimeng.sdk.btapi.model.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NetworkStatus 模型测试")
class NetworkStatusTest {

  @Test
  @DisplayName("应正确读取 CPU 信息")
  void readsCpuMetrics() {
    NetworkStatus networkStatus = new NetworkStatus();
    networkStatus.setCpu(List.of(13.5, 8.0));

    assertEquals(13.5, networkStatus.getCpuUsage());
    assertEquals(8, networkStatus.getCpuCores());
  }

  @Test
  @DisplayName("应支持字符串数值形式的内存与负载信息")
  void supportsStringBasedMetrics() {
    NetworkStatus networkStatus = new NetworkStatus();
    networkStatus.setMem(Map.of("memRealUsed", "4096", "memTotal", "8192"));
    networkStatus.setLoad(Map.of("one", "1.5", "five", "0.8", "fifteen", "0.3"));

    assertEquals(50.0, networkStatus.getMemoryUsage(), 0.001);
    assertEquals(1.5, networkStatus.getLoad1Min(), 0.001);
    assertEquals(0.8, networkStatus.getLoad5Min(), 0.001);
    assertEquals(0.3, networkStatus.getLoad15Min(), 0.001);
  }

  @Test
  @DisplayName("缺失指标时应返回默认值")
  void returnsDefaultValuesForMissingMetrics() {
    NetworkStatus networkStatus = new NetworkStatus();

    assertEquals(0.0, networkStatus.getCpuUsage());
    assertEquals(0, networkStatus.getCpuCores());
    assertEquals(0.0, networkStatus.getMemoryUsage(), 0.001);
    assertEquals(0.0, networkStatus.getLoad1Min(), 0.001);
  }
}
