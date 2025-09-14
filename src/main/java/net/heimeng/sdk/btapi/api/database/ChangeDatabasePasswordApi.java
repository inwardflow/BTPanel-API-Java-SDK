package net.heimeng.sdk.btapi.api.database;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 修改数据库密码API实现
 * <p>
 * 用于在宝塔面板中修改指定数据库的密码。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class ChangeDatabasePasswordApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "database?action=ChangeDBPassword";
    
    /**
     * 构造函数，创建一个新的ChangeDatabasePasswordApi实例
     * 
     * @param databaseName 数据库名称
     * @param username 数据库用户名
     * @param newPassword 新密码
     */
    public ChangeDatabasePasswordApi(String databaseName, String username, String newPassword) {
        super(ENDPOINT, HttpMethod.POST);
        
        // 设置必需参数
        setDatabaseName(databaseName);
        setUsername(username);
        setNewPassword(newPassword);
    }
    
    /**
     * 设置数据库名称
     * 
     * @param databaseName 数据库名称
     * @return 当前API实例，支持链式调用
     */
    public ChangeDatabasePasswordApi setDatabaseName(String databaseName) {
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
    public ChangeDatabasePasswordApi setUsername(String username) {
        if (StrUtil.isEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        addParam("username", username);
        return this;
    }
    
    /**
     * 设置数据库新密码
     * 
     * @param newPassword 新密码
     * @return 当前API实例，支持链式调用
     */
    public ChangeDatabasePasswordApi setNewPassword(String newPassword) {
        if (StrUtil.isEmpty(newPassword)) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        addParam("password", newPassword);
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<Boolean>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Boolean>对象，data为true表示修改密码成功
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
            result.setMsg(json.getStr("msg", success ? "Database password changed successfully" : "Failed to change database password"));
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse change database password response: " + e.getMessage(), e);
        }
    }
}