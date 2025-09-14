package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 获取网站列表API实现
 * <p>
 * 用于获取宝塔面板中的网站列表。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteListApi extends BaseBtApi<BtResult<List<Map<String, Object>>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=GetSiteList";
    
    /**
     * 构造函数，创建一个新的GetWebsiteListApi实例
     */
    public GetWebsiteListApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置页码
     * 
     * @param page 页码
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteListApi setPage(Integer page) {
        addParam("page", page);
        return this;
    }
    
    /**
     * 设置每页数量
     * 
     * @param limit 每页数量
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteListApi setLimit(Integer limit) {
        addParam("limit", limit);
        return this;
    }
    
    /**
     * 设置搜索关键词
     * 
     * @param search 搜索关键词
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteListApi setSearch(String search) {
        addParam("search", search);
        return this;
    }
    
    /**
     * 设置排序字段
     * 
     * @param orderBy 排序字段
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteListApi setOrderBy(String orderBy) {
        addParam("orderby", orderBy);
        return this;
    }
    
    /**
     * 设置排序方向
     * 
     * @param orderDirection 排序方向，asc或desc
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteListApi setOrderDirection(String orderDirection) {
        addParam("order", orderDirection);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        // 分页参数不是必需的，但如果提供了，必须是有效的
        if (params.containsKey("page") && !(params.get("page") instanceof Integer) && (Integer)params.get("page") < 1) {
            return false;
        }
        if (params.containsKey("limit") && !(params.get("limit") instanceof Integer) && (Integer)params.get("limit") < 1) {
            return false;
        }
        
        // 排序方向必须是asc或desc
        if (params.containsKey("order") && !("asc".equals(params.get("order")) || "desc".equals(params.get("order")))) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 解析API响应字符串为BtResult<List<Map<String, Object>>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<List<Map<String, Object>>>对象，其中data为网站列表
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
            
            // 检查响应状态
            boolean status = json.getBool("status", false);
            if (!status) {
                result.setStatus(false);
                result.setMsg(json.getStr("msg", "获取失败"));
                result.setData(new ArrayList<>());
                return result;
            }
            
            // 获取网站列表
            JSONArray sitesArray = json.getJSONArray("data");
            List<Map<String, Object>> sitesList = new ArrayList<>();
            
            if (sitesArray != null && !sitesArray.isEmpty()) {
                for (int i = 0; i < sitesArray.size(); i++) {
                    sitesList.add(sitesArray.getJSONObject(i).toBean(Map.class));
                }
            }
            
            result.setStatus(true);
            result.setMsg("获取成功");
            result.setData(sitesList);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website list response: " + e.getMessage(), e);
        }
    }
}