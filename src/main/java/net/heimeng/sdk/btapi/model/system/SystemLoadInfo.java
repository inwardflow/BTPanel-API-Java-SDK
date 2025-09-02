package net.heimeng.sdk.btapi.model.system;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 系统负载信息模型
 * <p>
 * 表示宝塔面板所在服务器的系统负载信息，包括CPU负载、内存使用、磁盘I/O等数据。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SystemLoadInfo extends BtResult {
    // CPU负载信息
    private double load1;
    private double load5;
    private double load15;
    private double cpuUsage;
    
    // 内存使用信息
    private long memoryTotal;
    private long memoryUsed;
    private double memoryUsage;
    
    // 磁盘I/O信息
    private long diskRead;
    private long diskWrite;
    private double diskUsage;
    
    // 网络I/O信息
    private long netIn;
    private long netOut;
    
    // 进程信息
    private int processCount;
    private int threadCount;
    
    // 系统时间信息
    private long timestamp;
    private String datetime;
}