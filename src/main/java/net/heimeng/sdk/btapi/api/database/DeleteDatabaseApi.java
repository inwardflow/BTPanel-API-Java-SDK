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
 * <p>
 * <strong>注意：删除数据库时，name和id参数必须同时提供。</strong>
 * </p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * // 基本用法
 * DeleteDatabaseApi api1 = new DeleteDatabaseApi("test_db", 15);
 * 
 * // 使用静态工厂方法
 * DeleteDatabaseApi api2 = DeleteDatabaseApi.create("test_db", 15);
 * 
 * // 使用Builder模式
 * DeleteDatabaseApi api3 = DeleteDatabaseApi.Builder.builder("test_db", 15)
 *     .build();
 * </pre>
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
     * @param databaseId 要删除的数据库ID
     * @throws IllegalArgumentException 当数据库名称为空或数据库ID为负数时抛出
     */
    public DeleteDatabaseApi(String databaseName, int databaseId) {
        super(ENDPOINT, HttpMethod.POST);
        
        if (StrUtil.isEmpty(databaseName)) {
            throw new IllegalArgumentException("Database name cannot be empty");
        }
        
        if (databaseId < 0) {
            throw new IllegalArgumentException("Database ID cannot be negative");
        }
        
        setDatabaseName(databaseName);
        setDatabaseId(databaseId);
    }
    
    /**
     * 创建DeleteDatabaseApi实例的静态工厂方法
     * 
     * @param databaseName 要删除的数据库名称
     * @param databaseId 要删除的数据库ID
     * @return DeleteDatabaseApi实例
     */
    public static DeleteDatabaseApi create(String databaseName, int databaseId) {
        return new DeleteDatabaseApi(databaseName, databaseId);
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
     * 设置要删除的数据库ID
     * 
     * @param databaseId 数据库ID
     * @return 当前API实例，支持链式调用
     */
    public DeleteDatabaseApi setDatabaseId(int databaseId) {
        addParam("id", databaseId);
        return this;
    }
    
    /**
     * Builder模式实现，提供更灵活的API构建方式
     */
    public static class Builder {
        private final DeleteDatabaseApi api;
        
        /**
         * 构造函数，创建Builder实例
         * 
         * @param databaseName 数据库名称
         * @param databaseId 数据库ID
         */
        private Builder(String databaseName, int databaseId) {
            this.api = new DeleteDatabaseApi(databaseName, databaseId);
        }
        
        /**
         * 创建Builder实例
         * 
         * @param databaseName 数据库名称
         * @param databaseId 数据库ID
         * @return Builder实例
         */
        public static Builder builder(String databaseName, int databaseId) {
            return new Builder(databaseName, databaseId);
        }
        
        /**
         * 构建DeleteDatabaseApi实例
         * 
         * @return 配置好的DeleteDatabaseApi实例
         */
        public DeleteDatabaseApi build() {
            return api;
        }
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
            String message = json.getStr("msg", "");
            
            // 处理幂等性：如果消息表明数据库不存在或已删除，也视为成功
            if (!success && message != null) {
                String lowerCaseMsg = message.toLowerCase();
                if (lowerCaseMsg.contains("不存在") || 
                    lowerCaseMsg.contains("not exist") ||
                    lowerCaseMsg.contains("already deleted") ||
                    lowerCaseMsg.contains("已删除")) {
                    success = true;
                    message = "Database deletion succeeded (idempotent result)";
                }
            }
            
            result.setStatus(success);
            result.setMsg(message.isEmpty() ? (success ? "Database deleted successfully" : "Failed to delete database") : message);
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse delete database response: " + e.getMessage(), e);
        }
    }
}