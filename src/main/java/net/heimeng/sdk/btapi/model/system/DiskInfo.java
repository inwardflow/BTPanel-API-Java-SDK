package net.heimeng.sdk.btapi.model.system;

import lombok.Data;

import java.util.List;

/**
 * 磁盘信息模型类，用于表示宝塔面板的磁盘分区信息
 * <p>
 * 包含磁盘分区的挂载点、inode使用情况、容量使用情况等信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class DiskInfo {
    
    /**
     * 分区挂载点
     */
    private String path;
    
    /**
     * 分区Inode使用信息[总量,已使用,可用,使用率]
     */
    private List<String> inodes;
    
    /**
     * 分区容量使用信息[总量,已使用,可用,使用率]
     */
    private List<String> size;
    
    /**
     * 获取总容量
     * 
     * @return 总容量字符串
     */
    public String getTotalSize() {
        if (size != null && size.size() > 0) {
            return size.get(0);
        }
        return "Unknown";
    }
    
    /**
     * 获取已使用容量
     * 
     * @return 已使用容量字符串
     */
    public String getUsedSize() {
        if (size != null && size.size() > 1) {
            return size.get(1);
        }
        return "Unknown";
    }
    
    /**
     * 获取可用容量
     * 
     * @return 可用容量字符串
     */
    public String getFreeSize() {
        if (size != null && size.size() > 2) {
            return size.get(2);
        }
        return "Unknown";
    }
    
    /**
     * 获取使用率
     * 
     * @return 使用率字符串
     */
    public String getUsagePercentage() {
        if (size != null && size.size() > 3) {
            return size.get(3);
        }
        return "Unknown";
    }
}