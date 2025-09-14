package net.heimeng.sdk.btapi.model.ftp;

import lombok.Data;

import java.util.Date;

/**
 * FTP账户信息模型类
 * <p>
 * 用于存储宝塔面板中FTP账户的基本信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class FtpAccount {
    
    /**
     * FTP账户ID
     */
    private int id;
    
    /**
     * FTP用户名
     */
    private String username;
    
    /**
     * FTP主目录路径
     */
    private String path;
    
    /**
     * FTP空间大小限制（单位：MB），0表示无限制
     */
    private long size;
    
    /**
     * 已使用空间大小（单位：MB）
     */
    private long usedSize;
    
    /**
     * FTP账户状态（如正常、锁定等）
     */
    private String status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 是否可查看所有目录
     */
    private boolean canViewAll;
    
    /**
     * 所属网站（如果有）
     */
    private String websiteDomain;
    
    /**
     * 判断FTP账户是否为正常状态
     * 
     * @return FTP账户是否正常
     */
    public boolean isNormal() {
        return "normal".equalsIgnoreCase(status);
    }
    
    /**
     * 获取FTP空间使用率
     * 
     * @return 使用率百分比，如果size为0（无限制）则返回0
     */
    public double getUsagePercentage() {
        if (size <= 0) {
            return 0.0;
        }
        return Math.min(100.0, (double) usedSize / size * 100);
    }
    
    /**
     * 获取空间限制的可读格式
     * 
     * @return 格式化后的空间限制
     */
    public String getFormattedSizeLimit() {
        if (size == 0) {
            return "无限制";
        } else if (size < 1024) {
            return size + " MB";
        } else {
            return String.format("%.2f GB", size / 1024.0);
        }
    }
}