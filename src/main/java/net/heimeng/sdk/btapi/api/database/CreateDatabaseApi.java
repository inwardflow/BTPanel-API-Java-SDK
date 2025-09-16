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
 * <p>数据访问权限支持多种格式：
 * - 本地服务器："127.0.0.1"
 * - 指定IP：使用换行符(%0A)分隔多个IP
 * - 所有人："%"
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 基本用法
 * CreateDatabaseApi api1 = CreateDatabaseApi.builder("testdb", "testuser", "password")
 *     .withCharset("utf8mb4")
 *     .withNote("测试数据库")
 *     .withDataAccess("%")
 *     .build();
 *
 * // 创建MySQL数据库
 * CreateDatabaseApi api2 = CreateDatabaseApi.builder("testdb", "testuser", "password")
 *     .asMySql()
 *     .build();
 *
 * // 创建MongoDB数据库
 * CreateDatabaseApi api3 = CreateDatabaseApi.builder("testdb", "testuser", "password")
 *     .asMongoDb()
 *     .build();
 * </pre>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class CreateDatabaseApi extends BaseBtApi<BtResult<Boolean>> {

    /**
     * API端点路径
     */
    private static final String ENDPOINT = "database";

    /**
     * 数据库类型枚举（确保类型安全）
     */
    public enum DatabaseType {
        MYSQL("MySQL"),
        MONGODB("MongoDb");

        private final String value;

        DatabaseType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 构造函数，创建一个新的CreateDatabaseApi实例
     *
     * @param databaseName 数据库名称
     * @param username 数据库用户名
     * @param password 数据库密码
     */
    private CreateDatabaseApi(String databaseName, String username, String password) {
        super(ENDPOINT, HttpMethod.POST);

        // 设置API动作参数
        addParam("action", "AddDatabase");
        
        // 设置必需参数
        setDatabaseName(databaseName);
        setUsername(username);
        // 备注默认为数据库名
        setNote(databaseName);
        setPassword(password);

        // 设置默认值
        setCharset("utf8mb4");
        setDatabaseType(DatabaseType.MYSQL);
        setDataAccess("%");
        setAddress("%");
        setSid(0);
        setListenIp("0.0.0.0/0");
        setHost("%");
    }

    /**
     * 创建Builder实例（推荐使用Builder模式构建API）
     *
     * @param databaseName 数据库名称
     * @param username 数据库用户名
     * @param password 数据库密码
     * @return Builder实例
     */
    public static Builder builder(String databaseName, String username, String password) {
        return new Builder(databaseName, username, password);
    }

    /**
     * Builder模式实现，提供更灵活的API构建方式
     */
    public static class Builder {
        private final CreateDatabaseApi api;

        private Builder(String databaseName, String username, String password) {
            this.api = new CreateDatabaseApi(databaseName, username, password);
        }

        public Builder withCharset(String charset) {
            api.setCharset(charset);
            return this;
        }

        public Builder withNote(String note) {
            api.setNote(note);
            return this;
        }

        public Builder withDatabaseType(DatabaseType type) {
            api.setDatabaseType(type);
            return this;
        }

        public Builder asMySql() {
            return withDatabaseType(DatabaseType.MYSQL);
        }

        public Builder asMongoDb() {
            return withDatabaseType(DatabaseType.MONGODB);
        }

        public Builder withDataAccess(String dataAccess) {
            api.setDataAccess(dataAccess);
            return this;
        }

        public Builder withAddress(String address) {
            api.setAddress(address);
            return this;
        }

        public Builder withListenIp(String listenIp) {
            api.setListenIp(listenIp);
            return this;
        }

        public Builder withHost(String host) {
            api.setHost(host);
            return this;
        }

        public Builder withSid(int sid) {
            api.setSid(sid);
            return this;
        }

        public CreateDatabaseApi build() {
            return api;
        }
    }

    /**
     * 设置数据库名称
     *
     * @param databaseName 数据库名称（非空）
     * @return 当前API实例
     * @throws IllegalArgumentException 当名称为空时
     */
    public CreateDatabaseApi setDatabaseName(String databaseName) {
        if (StrUtil.isEmpty(databaseName)) {
            throw new IllegalArgumentException("Database name must not be empty");
        }
        addParam("name", databaseName);
        return this;
    }

    /**
     * 设置数据库用户名
     *
     * @param username 数据库用户名（非空）
     * @return 当前API实例
     * @throws IllegalArgumentException 当用户名为空时
     */
    public CreateDatabaseApi setUsername(String username) {
        if (StrUtil.isEmpty(username)) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        addParam("db_user", username);
        return this;
    }

    /**
     * 设置数据库密码
     *
     * @param password 数据库密码（非空）
     * @return 当前API实例
     * @throws IllegalArgumentException 当密码为空时
     */
    public CreateDatabaseApi setPassword(String password) {
        if (StrUtil.isEmpty(password)) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        addParam("password", password);
        return this;
    }

    /**
     * 设置数据库字符集
     *
     * @param charset 字符集（非空）
     * @return 当前API实例
     * @throws IllegalArgumentException 当字符集为空时
     */
    public CreateDatabaseApi setCharset(String charset) {
        if (StrUtil.isEmpty(charset)) {
            throw new IllegalArgumentException("Charset must not be empty");
        }
        addParam("codeing", charset);
        return this;
    }

    /**
     * 设置数据库备注
     *
     * @param note 备注（可为空）
     * @return 当前API实例
     */
    public CreateDatabaseApi setNote(String note) {
        addParam("ps", note);
        return this;
    }

    /**
     * 设置数据库类型
     *
     * @param type 数据库类型（枚举值）
     * @return 当前API实例
     * @throws IllegalArgumentException 当类型为null时
     */
    public CreateDatabaseApi setDatabaseType(DatabaseType type) {
        if (type == null) {
            throw new IllegalArgumentException("Database type must not be null");
        }
        addParam("dtype", type.getValue());
        return this;
    }

    /**
     * 设置数据访问权限
     *
     * @param dataAccess 数据访问权限（非空）
     * @return 当前API实例
     * @throws IllegalArgumentException 当权限为空时
     */
    public CreateDatabaseApi setDataAccess(String dataAccess) {
        if (StrUtil.isEmpty(dataAccess)) {
            throw new IllegalArgumentException("Data access must not be empty");
        }
        addParam("dataAccess", dataAccess);
        return this;
    }

    /**
     * 设置数据库地址
     *
     * @param address 数据库地址（可为空）
     * @return 当前API实例
     */
    public CreateDatabaseApi setAddress(String address) {
        addParam("address", address);
        return this;
    }

    /**
     * 设置监听IP（SDK内部自动处理URL编码）
     *
     * @param listenIp 监听IP地址（如"0.0.0.0/0"）
     * @return 当前API实例
     */
    public CreateDatabaseApi setListenIp(String listenIp) {
        addParam("listen_ip", listenIp);
        return this;
    }

    /**
     * 设置主机地址
     *
     * @param host 主机地址（可为空）
     * @return 当前API实例
     */
    public CreateDatabaseApi setHost(String host) {
        addParam("host", host);
        return this;
    }

    /**
     * 设置SID (默认为 0, 代表本地数据库)
     *
     * @param sid SID值
     * @return 当前API实例
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
        if (StrUtil.isEmpty(response)) {
            throw new BtApiException("Empty response received from server");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            JSONObject json = JSONUtil.parseObj(response);
            BtResult<Boolean> result = new BtResult<>();

            boolean success = json.getBool("status", false);
            result.setStatus(success);

            // 优先使用JSON中的消息，无消息时使用默认值
            String msg = json.getStr("msg", null);
            if (StrUtil.isEmpty(msg)) {
                msg = success ? "Database created successfully" : "Failed to create database";
            }
            result.setMsg(msg);
            result.setData(success);

            return result;
        } catch (JSONException e) {
            throw new BtApiException("JSON parsing failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BtApiException("Unexpected error parsing response: " + e.getMessage(), e);
        }
    }
}