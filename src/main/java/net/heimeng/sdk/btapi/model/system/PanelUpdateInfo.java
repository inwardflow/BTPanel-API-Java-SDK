package net.heimeng.sdk.btapi.model.system;

import lombok.Data;

/**
 * 面板更新信息模型类，用于表示宝塔面板的更新信息
 * <p>
 * 包含面板的最新版本、更新状态、升级说明等信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class PanelUpdateInfo {
    
    /**
     * 获取状态，true表示有新版本，false表示已是最新版本
     */
    private boolean status;
    
    /**
     * 最新版本号
     */
    private String version;
    
    /**
     * 升级说明
     */
    private String updateMsg;
    
    /**
     * 检查面板是否需要更新
     * 
     * @return 如果面板需要更新则返回true，否则返回false
     */
    public boolean isUpdateAvailable() {
        return status;
    }
}