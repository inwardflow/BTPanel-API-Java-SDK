package net.heimeng.sdk.btapi.api;

/**
 * BtApi工厂枚举类
 * <p>
 * 用于标识和创建不同的宝塔面板API接口实例
 * </p>
 *
 * @author InwardFlow
 * @since 1.0.0
 */
public enum BtApiFactory {
    // 系统相关API
    GET_SYSTEM_INFO,      // 获取系统信息
    GET_SYSTEM_STATUS,    // 获取系统状态
    GET_SYSTEM_TOTAL,     // 获取系统总览
    
    // 网站相关API
    GET_WEBSITES,         // 获取网站列表
    ADD_WEBSITE,          // 添加网站
    DELETE_WEBSITE,       // 删除网站
    MODIFY_WEBSITE,       // 修改网站
    
    // FTP相关API
    GET_FTP_ACCOUNTS,     // 获取FTP账号列表
    ADD_FTP_ACCOUNT,      // 添加FTP账号
    DELETE_FTP_ACCOUNT,   // 删除FTP账号
    
    // 数据库相关API
    GET_DATABASES,        // 获取数据库列表
    ADD_DATABASE,         // 添加数据库
    DELETE_DATABASE,      // 删除数据库
    
    // 文件相关API
    UPLOAD_FILE,          // 上传文件
    DOWNLOAD_FILE,        // 下载文件
    DELETE_FILE,          // 删除文件
    
    // 其他API
    EXECUTE_SHELL,        // 执行Shell命令
    ;

    /**
     * 获取对应的API实例
     *
     * @return BtApi实例
     * @throws UnsupportedOperationException 当不支持该API时抛出
     */
    public BtApi<?> getApi() {
        throw new UnsupportedOperationException("该方法已废弃，请使用apiManager中的对应方法");
    }
}