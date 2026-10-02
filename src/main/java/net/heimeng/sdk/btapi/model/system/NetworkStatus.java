package net.heimeng.sdk.btapi.model.system;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 网络状态信息模型。
 *
 * <p>用于表达网络流量、CPU、内存和负载等实时指标。
 */
@Data
public class NetworkStatus {

  /** 总接收字节数。 */
  private long downTotal;

  /** 总发送字节数。 */
  private long upTotal;

  /** 总接收包数。 */
  private long downPackets;

  /** 总发送包数。 */
  private long upPackets;

  /** 下行流量，单位为 KB。 */
  private double down;

  /** 上行流量，单位为 KB。 */
  private double up;

  /** CPU 实时信息：[使用率, 核心数]。 */
  private List<Double> cpu;

  /** 内存实时信息。 */
  private Map<String, Object> mem;

  /** 负载实时信息。 */
  private Map<String, Object> load;

  /** 获取 CPU 使用率。 */
  public double getCpuUsage() {
    if (cpu != null && !cpu.isEmpty()) {
      return cpu.get(0);
    }
    return 0.0;
  }

  /** 获取 CPU 核心数。 */
  public int getCpuCores() {
    if (cpu != null && cpu.size() > 1) {
      return cpu.get(1).intValue();
    }
    return 0;
  }

  /** 获取内存使用率。 */
  public double getMemoryUsage() {
    if (mem != null && mem.containsKey("memRealUsed") && mem.containsKey("memTotal")) {
      long used = readLong(mem.get("memRealUsed"));
      long total = readLong(mem.get("memTotal"));
      return total > 0 ? (double) used / total * 100 : 0.0;
    }
    return 0.0;
  }

  /** 获取 1 分钟负载。 */
  public double getLoad1Min() {
    return readLoadValue("one");
  }

  /** 获取 5 分钟负载。 */
  public double getLoad5Min() {
    return readLoadValue("five");
  }

  /** 获取 15 分钟负载。 */
  public double getLoad15Min() {
    return readLoadValue("fifteen");
  }

  private double readLoadValue(String key) {
    if (load == null || !load.containsKey(key)) {
      return 0.0;
    }
    return readDouble(load.get(key));
  }

  private long readLong(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value instanceof String stringValue && !stringValue.isBlank()) {
      try {
        return Long.parseLong(stringValue.trim());
      } catch (NumberFormatException ignored) {
        return 0L;
      }
    }
    return 0L;
  }

  private double readDouble(Object value) {
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    if (value instanceof String stringValue && !stringValue.isBlank()) {
      try {
        return Double.parseDouble(stringValue.trim());
      } catch (NumberFormatException ignored) {
        return 0.0;
      }
    }
    return 0.0;
  }
}
