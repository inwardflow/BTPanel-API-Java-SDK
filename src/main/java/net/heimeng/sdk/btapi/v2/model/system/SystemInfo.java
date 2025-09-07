package net.heimeng.sdk.btapi.v2.model.system;

import lombok.Data;

/**
 * 系统信息模型类，用于表示宝塔面板的系统信息
 * <p>
 * 包含服务器的基本信息，如CPU使用率、内存使用情况、磁盘使用情况等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class SystemInfo {
    
    /**
     * 服务器名称
     */
    private String hostname;
    
    /**
     * 操作系统类型
     */
    private String os;
    
    /**
     * 内核版本
     */
    private String kernel;
    
    /**
     * CPU使用率
     */
    private double cpuUsage;
    
    /**
     * 内存总量（MB）
     */
    private long memoryTotal;
    
    /**
     * 内存已用（MB）
     */
    private long memoryUsed;
    
    /**
     * 磁盘总量（GB）
     */
    private double diskTotal;
    
    /**
     * 磁盘已用（GB）
     */
    private double diskUsed;
    
    /**
     * 宝塔面板版本
     */
    private String panelVersion;
    
    /**
     * 获取内存使用率
     * 
     * @return 内存使用率（百分比）
     */
    public double getMemoryUsage() {
        if (memoryTotal <= 0) {
            return 0;
        }
        return (double) memoryUsed / memoryTotal * 100;
    }
    
    /**
     * 获取磁盘使用率
     * 
     * @return 磁盘使用率（百分比）
     */
    public double getDiskUsage() {
        if (diskTotal <= 0) {
            return 0;
        }
        return diskUsed / diskTotal * 100;
    }
}