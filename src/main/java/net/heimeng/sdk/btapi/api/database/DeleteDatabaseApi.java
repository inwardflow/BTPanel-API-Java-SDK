package net.heimeng.sdk.btapi.api.database;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 删除数据库API实现
 * <p>
 * 用于在宝塔面板中删除指定的数据库。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class DeleteDatabaseApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "database?action=DeleteDatabase";
    
    /**
     * 构造函数，创建一个新的DeleteDatabaseApi实例
     * 
     * @param databaseName 要删除的数据库名称
     */
    public DeleteDatabaseApi(String databaseName) {
        super(ENDPOINT, HttpMethod.POST);
        
        if (StrUtil.isEmpty(databaseName)) {
            throw new IllegalArgumentException("Database name cannot be empty");
        }
        
        setDatabaseName(databaseName);
    }
    
    /**
     * 设置要删除的数据库名称
     * 
     * @param databaseName 数据库名称
     * @return 当前API实例，支持链式调用
     */
    public DeleteDatabaseApi setDatabaseName(String databaseName) {
        addParam("name", databaseName);
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<Boolean>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Boolean>对象，data为true表示删除成功
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
            result.setMsg(json.getStr("msg", success ? "Database deleted successfully" : "Failed to delete database"));
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse delete database response: " + e.getMessage(), e);
        }
    }
}