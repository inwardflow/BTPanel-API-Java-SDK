package net.heimeng.sdk.btapi.model.system;

import lombok.Data;
import net.heimeng.sdk.btapi.model.BtResult;

import java.util.List;

/**
 * 系统信息模型
 * <p>
 * 表示宝塔面板所在服务器的系统信息，包括操作系统、CPU、内存、磁盘等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class SystemInfo extends BtResult {
    /**
     * 无参构造函数
     * <p>
     * 调用父类的无参构造函数，状态默认为false
     * </p>
     */
    public SystemInfo() {
        super();
    }
    
    /**
     * 构造函数，设置状态和消息
     * 
     * @param status 状态
     * @param msg 消息
     */
    public SystemInfo(Boolean status, String msg) {
        super(status, msg);
    }
    // 操作系统信息
    private String osName;
    private String osVersion;
    private String osArch;

    // CPU信息
    private int cpuCores;
    private double cpuUsage;
    private String cpuModel;

    // 内存信息
    private long memoryTotal;
    private long memoryUsed;
    private double memoryUsage;

    // 磁盘信息
    private List<DiskInfo> disks;

    // 网络信息
    private List<NetworkInfo> networks;

    // 宝塔面板信息
    private String btVersion;
    private String btStatus;

    // 其他信息
    private String serverName;
    private String kernelVersion;
    private String uptime;

    /**
     * 磁盘信息内部类
     */
    @Data
    public static class DiskInfo {
        private String device;
        private String mountPoint;
        private String fileSystem;
        private long total;
        private long used;
        private long free;
        private double usage;
    }

    /**
     * 网络信息内部类
     */
    @Data
    public static class NetworkInfo {
        private String name;
        private String ip;
        private String mac;
        private long inBytes;
        private long outBytes;
    }
}