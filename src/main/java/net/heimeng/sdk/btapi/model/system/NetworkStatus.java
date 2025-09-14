package net.heimeng.sdk.btapi.model.system;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 网络状态信息模型类，用于表示宝塔面板的实时网络状态信息
 * <p>
 * 包含CPU使用率、内存使用情况、网络流量、负载等实时信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class NetworkStatus {
    
    /**
     * 总接收字节数
     */
    private long downTotal;
    
    /**
     * 总发送字节数
     */
    private long upTotal;
    
    /**
     * 总收包数
     */
    private long downPackets;
    
    /**
     * 总发包数
     */
    private long upPackets;
    
    /**
     * 下行流量(KB)
     */
    private double down;
    
    /**
     * 上行流量(KB)
     */
    private double up;
    
    /**
     * CPU实时信息[使用率,核心数]
     */
    private List<Double> cpu;
    
    /**
     * 内存实时信息
     */
    private Map<String, Object> mem;
    
    /**
     * 负载实时信息
     */
    private Map<String, Object> load;
    
    /**
     * 获取CPU使用率
     * 
     * @return CPU使用率
     */
    public double getCpuUsage() {
        if (cpu != null && cpu.size() > 0) {
            return cpu.get(0);
        }
        return 0.0;
    }
    
    /**
     * 获取CPU核心数
     * 
     * @return CPU核心数
     */
    public int getCpuCores() {
        if (cpu != null && cpu.size() > 1) {
            return cpu.get(1).intValue();
        }
        return 0;
    }
    
    /**
     * 获取内存使用率
     * 
     * @return 内存使用率
     */
    @SuppressWarnings("unchecked")
    public double getMemoryUsage() {
        if (mem != null && mem.containsKey("memRealUsed") && mem.containsKey("memTotal")) {
            Object usedObj = mem.get("memRealUsed");
            Object totalObj = mem.get("memTotal");
            
            if (usedObj instanceof Number && totalObj instanceof Number) {
                long used = ((Number) usedObj).longValue();
                long total = ((Number) totalObj).longValue();
                return total > 0 ? (double) used / total * 100 : 0.0;
            }
        }
        return 0.0;
    }
    
    /**
     * 获取1分钟负载值
     * 
     * @return 1分钟负载值
     */
    @SuppressWarnings("unchecked")
    public double getLoad1Min() {
        if (load != null && load.containsKey("one")) {
            Object oneObj = load.get("one");
            if (oneObj instanceof Number) {
                return ((Number) oneObj).doubleValue();
            }
        }
        return 0.0;
    }
    
    /**
     * 获取5分钟负载值
     * 
     * @return 5分钟负载值
     */
    @SuppressWarnings("unchecked")
    public double getLoad5Min() {
        if (load != null && load.containsKey("five")) {
            Object fiveObj = load.get("five");
            if (fiveObj instanceof Number) {
                return ((Number) fiveObj).doubleValue();
            }
        }
        return 0.0;
    }
    
    /**
     * 获取15分钟负载值
     * 
     * @return 15分钟负载值
     */
    @SuppressWarnings("unchecked")
    public double getLoad15Min() {
        if (load != null && load.containsKey("fifteen")) {
            Object fifteenObj = load.get("fifteen");
            if (fifteenObj instanceof Number) {
                return ((Number) fifteenObj).doubleValue();
            }
        }
        return 0.0;
    }
}