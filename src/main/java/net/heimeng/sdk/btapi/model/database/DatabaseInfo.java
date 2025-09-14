package net.heimeng.sdk.btapi.model.database;

import lombok.Data;

import java.util.Date;

/**
 * 数据库信息模型类
 * <p>
 * 用于存储宝塔面板中数据库的基本信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class DatabaseInfo {
    
    /**
     * 数据库ID
     */
    private int id;
    
    /**
     * 数据库名称
     */
    private String name;
    
    /**
     * 数据库用户名
     */
    private String username;
    
    /**
     * 数据库类型（如MySQL）
     */
    private String type;
    
    /**
     * 数据库大小（单位：KB）
     */
    private long size;
    
    /**
     * 数据库字符集
     */
    private String charset;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 备注信息
     */
    private String description;
    
    /**
     * 数据库状态（如正常、锁定等）
     */
    private String status;
    
    /**
     * 判断数据库是否为正常状态
     * 
     * @return 数据库是否正常
     */
    public boolean isNormal() {
        return "normal".equalsIgnoreCase(status);
    }
    
    /**
     * 获取数据库大小的可读格式（自动转换单位）
     * 
     * @return 格式化后的数据库大小
     */
    public String getFormattedSize() {
        if (size < 1024) {
            return size + " KB";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f MB", size / 1024.0);
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0));
        }
    }
}