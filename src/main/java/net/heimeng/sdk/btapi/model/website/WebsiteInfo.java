package net.heimeng.sdk.btapi.model.website;

import lombok.Data;
import java.util.Date;

/**
 * 网站信息模型类，用于表示宝塔面板中的网站信息
 * <p>
 * 包含网站的基本信息，如域名、路径、类型、状态等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class WebsiteInfo {
    
    /**
     * 网站ID
     */
    private Long id;
    
    /**
     * 网站名称
     */
    private String name;
    
    /**
     * 网站域名
     */
    private String domain;
    
    /**
     * 网站根目录路径
     */
    private String path;
    
    /**
     * 网站类型（如：php、html等）
     */
    private String type;
    
    /**
     * 网站状态（0表示停止，1表示运行中）
     */
    private Integer status;
    
    /**
     * SSL状态（0表示未开启，1表示已开启）
     */
    private Integer ssl;
    
    /**
     * 创建时间戳（秒）
     */
    private Long createTime;
    
    /**
     * 获取网站的创建时间
     * 
     * @return 创建时间对象
     */
    public Date getCreateDate() {
        if (createTime == null || createTime <= 0) {
            return null;
        }
        return new Date(createTime * 1000);
    }
    
    /**
     * 检查网站是否运行中
     * 
     * @return 如果网站运行中则返回true，否则返回false
     */
    public boolean isRunning() {
        return status != null && status == 1;
    }
    
    /**
     * 检查网站是否开启了SSL
     * 
     * @return 如果网站开启了SSL则返回true，否则返回false
     */
    public boolean isSslEnabled() {
        return ssl != null && ssl == 1;
    }
}