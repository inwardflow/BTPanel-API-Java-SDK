package net.heimeng.sdk.btapi.api.database;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.database.DatabaseInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取数据库列表API实现
 * <p>
 * 用于获取宝塔面板中的所有数据库信息列表。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetDatabasesApi extends BaseBtApi<BtResult<List<DatabaseInfo>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "/datalist/data/get_data_list";
    
    /**
     * 构造函数，创建一个新的GetDatabasesApi实例
     */
    public GetDatabasesApi() {
        super(ENDPOINT, HttpMethod.POST);
        
        // 设置默认参数
        addParam("table", "databases");
    }
    
    /**
     * 解析API响应字符串为BtResult<List<DatabaseInfo>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<List<DatabaseInfo>>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<List<DatabaseInfo>> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            JSONObject json = JSONUtil.parseObj(response);
            BtResult<List<DatabaseInfo>> result = new BtResult<>();
            List<DatabaseInfo> databases = new ArrayList<>();
            
            // 检查响应是否包含data字段
            if (json.containsKey("data")) {
                result.setStatus(true);
                result.setMsg("Success");
                
                // 解析数据库列表
                JSONArray databaseArray = json.getJSONArray("data");
                if (databaseArray != null && !databaseArray.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    
                    for (int i = 0; i < databaseArray.size(); i++) {
                        JSONObject dbJson = databaseArray.getJSONObject(i);
                        if (dbJson != null) {
                            DatabaseInfo dbInfo = new DatabaseInfo();
                            
                            // 设置数据库基本信息
                            dbInfo.setId(dbJson.getInt("id", 0));
                            dbInfo.setName(dbJson.getStr("name", ""));
                            dbInfo.setUsername(dbJson.getStr("username", ""));
                            dbInfo.setType(dbJson.getStr("type", "MySQL"));
                            dbInfo.setSize(dbJson.getLong("size", 0L));
                            dbInfo.setCharset(dbJson.getStr("charset", "utf8mb4"));
                            dbInfo.setStatus(dbJson.getStr("status", "normal"));
                            dbInfo.setDescription(dbJson.getStr("description", ""));
                            
                            // 解析创建时间
                            try {
                                String createTimeStr = dbJson.getStr("create_time", "");
                                if (!createTimeStr.isEmpty()) {
                                    dbInfo.setCreateTime(dateFormat.parse(createTimeStr));
                                }
                            } catch (ParseException e) {
                                // 忽略时间解析错误
                            }
                            
                            databases.add(dbInfo);
                        }
                    }
                }
            } else if (json.containsKey("status")) {
                // 处理错误响应
                result.setStatus(json.getBool("status", false));
                result.setMsg(json.getStr("msg", ""));
            }
            
            result.setData(databases);
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse databases response: " + e.getMessage(), e);
        }
    }
}