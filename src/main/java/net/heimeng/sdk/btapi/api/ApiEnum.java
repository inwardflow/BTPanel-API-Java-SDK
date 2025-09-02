package net.heimeng.sdk.btapi.api;

/**
 * 宝塔 API 接口常量
 *
 * @author InwardFlow
 */
public enum ApiEnum {
    // 获取系统基础统计
    BT_GET_SYSTEM_TOTAL("/system?action=GetSystemTotal"),

    // 修改数据库备注
    BT_SET_DATABASE_PS("/data?action=setPs&table=databases"),

    // 删除文件
    BT_DELETE_FILE("/files?action=DeleteFile"),

    // 恢复数据库
    BT_INPUT_SQL("/database?action=InputSql"),

    // 添加FTP
    BT_ADD_FTP_USER("/ftp?action=AddUser"),

    // 停用启用FTP
    BT_FTP_STATUS("/ftp?action=SetStatus"),

    // 设置FTP密码
    BT_FTP_SET_USER_PASSWORD("/ftp?action=SetUserPassword"),

    // 删除FTP用户
    BT_FTP_DELETE_USER("/ftp?action=DeleteUser"),

    // 新建目录
    BT_FILE_CREATE_DIR("/files?action=CreateDir"),

    // 新建文件夹
    BT_FILE_CREATE_FILE("/files?action=CreateFile"),

    // 移动文件
    BT_FILE_MOVE_FILE("/files?action=MvFile"),

    // 复制文件
    BT_FILE_COPY_FILE("/files?action=CopyFile"),

    // 压缩文件
    BT_FILE_ZIP("/files?action=Zip"),

    // 解压文件
    BT_FILE_UNZIP("/files?action=UnZip"),

    // 面板操作日志
    BT_GET_LOGS("/data?action=getData&table=logs&tojs=getLogs"),

    // 面板运行日志
    BT_GET_RUN_LOGS("/config?action=get_panel_error_logs"),

    // 计划任务日志
    BT_GET_TASK_LOGS("/crontab?action=GetLogs"),

    // 获取计划任务
    BT_GET_CRONTAB("/data?action=getData&table=crontab"),

    // 网站列表
    BT_GET_SITE_LIST("/data?action=getData&table=sites"),

    // 网站操作日志
    BT_GET_SITE_LOGS("/logs/panel/get_logs_bytype"),

    // 网站运行日志
    BT_GET_SITE_RUN_LOGS("/site?action=GetSiteLogs"),

    // 网站错误日志
    BT_GET_SITE_ERR_LOGS("/site?action=get_site_errlog"),

    // 端口规则
    BT_GET_PORT_RULES_LIST("/safe/firewall/get_rules_list"),

    // 端口转发
    BT_GET_PORT_FORWARDS_LIST("/safe/firewall/get_forward_list"),

    // IP规则
    BT_GET_IP_RULES_LIST("/safe/firewall/get_ip_rules_list"),

    // 地区规则
    BT_GET_COUNTRY_RULES_LIST("/safe/firewall/get_country_list"),

    // 创建端口规则
    BT_CREATE_PORT_RULE_LIST("/safe/firewall/create_rules"),

    // 删除端口规则
    BT_DELETE_PORT_RULE_LIST("/safe/firewall/remove_rules"),

    // 修改端口规则
    BT_MODIFY_PORT_RULE("/safe/firewall/modify_rules"),

    // 创建IP规则
    BT_CREATE_IP_RULE("/safe/firewall/create_ip_rules"),

    // 删除IP规则
    BT_DELETE_IP_RULE("/safe/firewall/remove_ip_rules"),

    // 修改IP规则
    BT_MODIFY_IP_RULE("/safe/firewall/modify_ip_rules"),

    // 创建端口转发规则
    BT_CREATE_FORWARD("/safe/firewall/create_forward"),

    // 删除端口转发规则
    BT_DELETE_FORWARD("/safe/firewall/remove_forward"),

    // 修改端口转发规则
    BT_MODIFY_FORWARD("/safe/firewall/modify_forward"),

    // 创建地区规则
    BT_CREATE_COUNTRY("/safe/firewall/create_country"),

    // 删除地区规则
    BT_DELETE_COUNTRY("/safe/firewall/remove_country"),

    // 修改地区规则
    BT_MODIFY_COUNTRY("/safe/firewall/modify_country"),

    // 获取国家列表
    BT_GET_COUNTRIES("/safe/firewall/get_countrys"),

    // 获取防火墙信息
    BT_GET_FIREWALL_INFO("/safe/firewall/get_firewall_info"),

    // 获取SSH信息
    BT_GET_SSH_INFO("/safe/ssh/GetSshInfo"),

    // 禁ping设置
    BT_SET_PING("/firewall?action=SetPing"),

    // 防火墙开关设置
    BT_FIREWALL_ADMIN("/safe/firewall/firewall_admin"),

    // SSH开关设置
    BT_SET_SSH("/firewall?action=SetSshStatus"),

    // 启动计划任务
    BT_START_TASK("/crontab?action=StartTask"),

    // 设置计划任务状态
    BT_SET_TASK_STATUS("/crontab?action=set_cron_status"),

    // 删除计划任务状态
    BT_DEL_CRONTAB("/crontab?action=DelCrontab"),

    // 计划任务
    BT_GET_CRONTAB_LIST("/crontab?action=GetCrontab"),

    // 释放内存
    BT_RE_MEMORY("/system?action=ReMemory"),

    // 获取配置
    BT_GET_CONFIG("/config?action=get_config"),

    // 配置面板
    BT_SET_PANEL("/config?action=setPanel"),

    // 负载
    BT_GET_LOAD_AVERAGE("/ajax?action=get_load_average"),

    // CPU 内存
    BT_GET_CPU_IO("/ajax?action=GetCpuIo"),

    // 磁盘IO
    BT_GET_DISK_IO("/ajax?action=GetDiskIo"),

    // 网络IO
    BT_GET_NETWORK_IO("/ajax?action=GetNetWorkIo"),

    // 获取监控状态
    BT_SET_CONTROL("/config?action=SetControl"),

    // 获取软件列表
    BT_GET_SOFT_LIST("/plugin?action=get_soft_list"),

    // 卸载软件
    BT_UNINSTALL_PLUGIN("/plugin?action=uninstall_plugin"),

    // 服务管理
    BT_SERVICE_ADMIN("/system?action=ServiceAdmin"),

    // 网站数据获取
    BT_GET_DATA_LIST("/crontab?action=GetDataList"),

    // 数据库数据获取
    BT_GET_DATABASE_LIST("/crontab?action=GetDatabases"),

    // 添加计划任务
    BT_ADD_CRONTAB("/crontab?action=AddCrontab"),

    // 查询计划任务
    BT_GET_CROND_FIND("/crontab?action=get_crond_find"),

    // 修改计划任务
    BT_MODIFY_CROND("/crontab?action=modify_crond"),

    // 远程下载
    BT_DOWNLOAD_FILE("/files?action=DownloadFile"),

    // 站点迁移
    SITE_MIGRATE("/plugin?action=a&name=psync_api"),

    // 站点迁移 - 检查
    SITE_MIGRATE_CHECK_SURROUNDINGS("/plugin?action=a&name=psync_api&s=chekc_surroundings"),

    // 站点迁移 - 获取站点信息
    SITE_MIGRATE_GET_SITE_INFO("/plugin?action=a&name=psync_api&s=get_site_info"),

    // 站点迁移 - 设置面板API
    SITE_MIGRATE_SET_PANEL_API("/plugin?action=a&name=psync_api&s=set_panel_api"),
    SITE_MIGRATE_SET_SYNC_INFO("/plugin?action=a&name=psync_api&s=set_sync_info"),
    SITE_MIGRATE_GET_SPEED("/plugin?action=a&name=psync_api&s=get_speed");

    private final String path;

    ApiEnum(String path) {
        this.path = path;
    }

    public String getValue() {
        return path;
    }
}
