package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取网站备份列表API实现
 * <p>
 * 用于获取宝塔面板中指定网站的备份列表。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteBackupsApi extends BaseBtApi<BtResult<List<Map<String, Object>>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "data?action=getData&table=backup";
    
    /**
     * 构造函数，创建一个新的GetWebsiteBackupsApi实例
     */
    public GetWebsiteBackupsApi() {
        super(ENDPOINT, HttpMethod.POST);
        // 设置默认参数
        addParam("p", 1);
        addParam("limit", 5);
        addParam("type", 0);
    }
    
    /**
     * 设置当前分页
     * 
     * @param page 当前分页
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteBackupsApi setPage(Integer page) {
        addParam("p", page);
        return this;
    }
    
    /**
     * 设置每页取回的数据行数
     * 
     * @param limit 数据行数
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteBackupsApi setLimit(Integer limit) {
        addParam("limit", limit);
        return this;
    }
    
    /**
     * 设置网站ID
     * 
     * @param siteId 网站ID
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteBackupsApi setSiteId(Integer siteId) {
        addParam("search", siteId);
        return this;
    }
    
    /**
     * 设置分页JS回调
     * 
     * @param callback JS回调函数名
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteBackupsApi setCallback(String callback) {
        addParam("tojs", callback);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("limit") && params.containsKey("search");
    }
    
    /**
     * 解析API响应字符串为BtResult<List<Map<String, Object>>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<List<Map<String, Object>>>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<List<Map<String, Object>>> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }
        
        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }
            
            JSONObject json = JSONUtil.parseObj(response);
            BtResult<List<Map<String, Object>>> result = new BtResult<>();
            result.setStatus(true);
            result.setMsg("Success");
            
            // 解析备份列表数据
            JSONArray dataArray = json.getJSONArray("data");
            List<Map<String, Object>> backups = new ArrayList<>();
            
            if (dataArray != null) {
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject backupJson = dataArray.getJSONObject(i);
                    if (backupJson != null) {
                        Map<String, Object> backupMap = new HashMap<>();
                        // 将JSON对象转换为Map
                        for (String key : backupJson.keySet()) {
                            backupMap.put(key, backupJson.get(key));
                        }
                        backups.add(backupMap);
                    }
                }
            }
            
            result.setData(backups);
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website backups response: " + e.getMessage(), e);
        }
    }
}