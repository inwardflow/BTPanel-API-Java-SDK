package net.heimeng.sdk.btapi.api.database;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 创建数据库API实现
 * <p>
 * 用于在宝塔面板中创建新的数据库，支持MySQL和MongoDB类型。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class CreateDatabaseApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "database?action=AddDatabase";
    
    /**
     * MySQL数据库类型
     */
    public static final String TYPE_MYSQL = "MySQL";
    
    /**
     * MongoDB数据库类型
     */
    public static final String TYPE_MONGODB = "MongoDb";
    
    /**
     * 构造函数，创建一个新的CreateDatabaseApi实例
     * 
     * @param databaseName 数据库名称
     * @param username 数据库用户名
     * @param password 数据库密码
     */
    public CreateDatabaseApi(String databaseName, String username, String password) {
        super(ENDPOINT, HttpMethod.POST);
        
        // 设置必需参数
        setDatabaseName(databaseName);
        setUsername(username);
        setPassword(password);
        
        // 设置默认值
        setCharset("utf8mb4");
        setDatabaseType(TYPE_MYSQL);
        setDataAccess("127.0.0.1");
    }
    
    /**
     * 设置数据库名称
     * 
     * @param databaseName 数据库名称
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setDatabaseName(String databaseName) {
        if (StrUtil.isEmpty(databaseName)) {
            throw new IllegalArgumentException("Database name cannot be empty");
        }
        addParam("name", databaseName);
        return this;
    }
    
    /**
     * 设置数据库用户名
     * 
     * @param username 数据库用户名
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setUsername(String username) {
        if (StrUtil.isEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        addParam("db_user", username);
        return this;
    }
    
    /**
     * 设置数据库密码
     * 
     * @param password 数据库密码
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setPassword(String password) {
        if (StrUtil.isEmpty(password)) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        addParam("password", password);
        return this;
    }
    
    /**
     * 设置数据库字符集
     * 
     * @param charset 数据库字符集
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setCharset(String charset) {
        if (StrUtil.isEmpty(charset)) {
            throw new IllegalArgumentException("Charset cannot be empty");
        }
        addParam("charset", charset);
        return this;
    }
    
    /**
     * 设置数据库备注
     * 
     * @param note 数据库备注
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setNote(String note) {
        addParam("ps", note);
        return this;
    }
    
    /**
     * 设置数据库类型
     * 
     * @param databaseType 数据库类型，可使用TYPE_MYSQL或TYPE_MONGODB常量
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setDatabaseType(String databaseType) {
        if (StrUtil.isEmpty(databaseType)) {
            throw new IllegalArgumentException("Database type cannot be empty");
        }
        addParam("dtype", databaseType);
        return this;
    }
    
    /**
     * 设置数据访问权限
     * 
     * @param dataAccess 数据访问权限地址，如"127.0.0.1"表示仅本地访问
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setDataAccess(String dataAccess) {
        if (StrUtil.isEmpty(dataAccess)) {
            throw new IllegalArgumentException("Data access cannot be empty");
        }
        addParam("dataAccess", dataAccess);
        return this;
    }
    
    /**
     * 设置数据库地址
     * 
     * @param address 数据库地址
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setAddress(String address) {
        addParam("address", address);
        return this;
    }
    
    /**
     * 设置监听IP
     * 
     * @param listenIp 监听IP地址，如"0.0.0.0/0"表示允许所有IP访问
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setListenIp(String listenIp) {
        addParam("listen_ip", listenIp);
        return this;
    }
    
    /**
     * 设置主机地址
     * 
     * @param host 主机地址
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setHost(String host) {
        addParam("host", host);
        return this;
    }
    
    /**
     * 设置SID
     * 
     * @param sid SID值
     * @return 当前API实例，支持链式调用
     */
    public CreateDatabaseApi setSid(int sid) {
        addParam("sid", sid);
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<Boolean>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Boolean>对象，data为true表示创建成功
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<Boolean> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            JSONObject json = JSONUtil.parseObj(response);
            BtResult<Boolean> result = new BtResult<>();
            
            // 检查响应状态
            boolean success = json.getBool("status", false);
            result.setStatus(success);
            result.setMsg(json.getStr("msg", success ? "Database created successfully" : "Failed to create database"));
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse create database response: " + e.getMessage(), e);
        }
    }
}