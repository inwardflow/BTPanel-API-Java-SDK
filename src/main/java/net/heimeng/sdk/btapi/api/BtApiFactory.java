package net.heimeng.sdk.btapi.api;

/**
 * 宝塔面板 API 工厂类
 *
 * @author InwardFlow
 */
public enum BtApiFactory {
    /**
     * 获取系统总览信息的 API 实例
     */
    GET_SYSTEM_TOTAL(new GetSystemTotalBtApi()),
    GET_SITE_LIST(new GetSiteListBtApi()),
    // 文件操作相关API
    DELETE_FILE(new DeleteFileBtApi()),
    FILE_CREATE_DIR(new FileCreateDirBtApi()),
    FILE_CREATE_FILE(new FileCreateFileBtApi()),
    FILE_MOVE_FILE(new FileMoveFileBtApi()),
    FILE_COPY_FILE(new FileCopyFileBtApi()),
    FILE_ZIP(new FileZipBtApi()),
    FILE_UNZIP(new FileUnzipBtApi()),
    // FTP相关API
    ADD_FTP_USER(new AddFtpUserBtApi()),
    FTP_STATUS(new FtpStatusBtApi()),
    FTP_SET_USER_PASSWORD(new FtpSetUserPasswordBtApi()),
    FTP_DELETE_USER(new FtpDeleteUserBtApi()),
    // 系统相关API
    GET_CONFIG(new GetConfigBtApi()),
    SET_PANEL(new SetPanelBtApi()),
    RE_MEMORY(new ReMemoryBtApi()),
    // 安全相关API
    GET_PORT_RULES_LIST(new GetPortRulesListBtApi()),
    GET_PORT_FORWARDS_LIST(new GetPortForwardsListBtApi()),
    GET_IP_RULES_LIST(new GetIpRulesListBtApi()),
    // 迁移相关API
    SITE_MIGRATE_CHECK_SURROUNDINGS(new SiteMigrateCheckSurroundingsBtApi()),
    SITE_MIGRATE_SET_PANEL_API(new SiteMigrateSetPanelApiBtApi()),
    SITE_MIGRATE_GET_SITE_INFO(new SiteMigrateGetSiteInfoBtApi()),
    SITE_MIGRATE_SET_SYNC_INFO(new SiteMigrateSetSyncInfoBtApi()),
    SITE_MIGRATE_GET_SPEED(new SiteMigrateGetSpeedBtApi());
    private final BtApi api;

    /**
     * 构造函数，用于初始化枚举成员
     *
     * @param api 具体的 API 实现对象
     */
    BtApiFactory(BtApi api) {
        try {
            if (api == null) {
                throw new IllegalArgumentException("API 实现对象不能为空");
            }
            this.api = api;
        } catch (Exception e) {
            // 记录日志并抛出自定义异常
            System.err.println("初始化 API 实例失败: " + e.getMessage());
            throw new RuntimeException("初始化 API 实例失败", e);
        }
    }

    /**
     * 获取对应的 API 实例
     *
     * @return 具体的 API 实现对象
     */
    public BtApi getApi() {
        return api;
    }
}
