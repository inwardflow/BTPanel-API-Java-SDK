package net.heimeng.sdk.btapi.model.system;

import lombok.Data;
import net.heimeng.sdk.btapi.model.BtResult;

import java.util.List;
import lombok.EqualsAndHashCode;

/**
 * 服务状态列表模型
 * <p>
 * 表示宝塔面板上所有服务的状态信息，包括Web服务器、数据库、FTP服务器等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ServiceStatusList extends BtResult {
    // 服务列表
    private List<ServiceInfo> services;

    /**
     * 无参构造函数，解决Lombok继承问题
     */
    public ServiceStatusList() {
        super();
    }
    
    /**
     * 构造函数，设置状态和消息
     * 
     * @param status 状态
     * @param msg 消息
     */
    public ServiceStatusList(boolean status, String msg) {
        super(status, msg);
    }

    /**
     * 服务信息内部类
     */
    @Data
    public static class ServiceInfo {
        // 服务名称
        private String name;
        // 服务显示名称
        private String displayName;
        // 服务状态（running、stopped、error等）
        private String status;
        // 服务版本
        private String version;
        // 服务类型
        private String type;
        // 服务描述
        private String description;
        // 是否是系统服务
        private boolean isSystemService;
        // 启动命令
        private String startCommand;
        // 停止命令
        private String stopCommand;
        // 重启命令
        private String restartCommand;
        // 状态检查命令
        private String statusCommand;
    }
}