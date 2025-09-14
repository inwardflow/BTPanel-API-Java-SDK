package net.heimeng.sdk.btapi.model.website;

import lombok.Data;

/**
 * 创建网站结果模型类，用于表示宝塔面板创建网站的响应结果
 * <p>
 * 包含网站创建状态、FTP创建状态、数据库创建状态等信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class CreateWebsiteResult {
    
    /**
     * 网站是否创建成功
     */
    private boolean siteStatus;
    
    /**
     * FTP是否创建成功
     */
    private boolean ftpStatus;
    
    /**
     * FTP用户名
     */
    private String ftpUser;
    
    /**
     * FTP密码
     */
    private String ftpPass;
    
    /**
     * 数据库是否创建成功
     */
    private boolean databaseStatus;
    
    /**
     * 数据库用户名
     */
    private String databaseUser;
    
    /**
     * 数据库密码
     */
    private String databasePass;
    
    /**
     * 检查网站创建是否完全成功
     * 
     * @return 如果网站、FTP和数据库都创建成功（如果启用了）则返回true，否则返回false
     */
    public boolean isFullySuccess() {
        // 网站必须创建成功
        if (!siteStatus) {
            return false;
        }
        
        // FTP和数据库如果配置了，则必须也成功
        boolean ftpSuccess = !ftpUser.isEmpty() ? ftpStatus : true;
        boolean dbSuccess = !databaseUser.isEmpty() ? databaseStatus : true;
        
        return ftpSuccess && dbSuccess;
    }
}