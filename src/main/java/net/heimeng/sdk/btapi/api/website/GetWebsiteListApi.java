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
    private static final String ENDPOINT = "data?action=getData&table=sites";
    
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
        addParam("p", page);
        return this;
    }
    
    /**
     * 设置每页数量（必传参数）
     * 
     * @param limit 取回的数据行数
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteListApi setLimit(Integer limit) {
        addParam("limit", limit);
        return this;
    }
    
    /**
     * 设置分类标识
     * 
     * @param type 分类标识，-1:分部分类 0:默认分类
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteListApi setType(Integer type) {
        addParam("type", type);
        return this;
    }
    
    /**
     * 设置排序规则
     * 
     * @param order 排序规则，如"iddesc"（id降序）、"namedesc"（名称降序）等
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteListApi setOrder(String order) {
        addParam("order", order);
        return this;
    }
    
    /**
     * 设置分页JS回调
     * 
     * @param tojs 分页JS回调，若不传则构造URI分页连接
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteListApi setTojs(String tojs) {
        addParam("tojs", tojs);
        return this;
    }
    
    /**
     * 设置搜索关键词
     * 
     * @param search 搜索内容
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteListApi setSearch(String search) {
        addParam("search", search);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        // limit是必传参数
        if (!params.containsKey("limit") || !(params.get("limit") instanceof Integer) || (Integer)params.get("limit") < 1) {
            return false;
        }
        
        // 页码参数如果提供了，必须是有效的
        if (params.containsKey("p") && !(params.get("p") instanceof Integer) || (Integer)params.get("p") < 1) {
            return false;
        }
        
        // 分类标识如果提供了，必须是-1或0
        if (params.containsKey("type") && !(params.get("type") instanceof Integer) || 
            ((Integer)params.get("type") != -1 && (Integer)params.get("type") != 0)) {
            return false;
        }
        
        // 其他可选参数的类型验证
        if (params.containsKey("order") && !(params.get("order") instanceof String)) {
            return false;
        }
        if (params.containsKey("tojs") && !(params.get("tojs") instanceof String)) {
            return false;
        }
        if (params.containsKey("search") && !(params.get("search") instanceof String)) {
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
            
            // 直接获取网站列表数据（新响应格式没有status字段）
            JSONArray sitesArray = json.getJSONArray("data");
            List<Map<String, Object>> sitesList = new ArrayList<>();
            
            if (sitesArray != null && !sitesArray.isEmpty()) {
                for (int i = 0; i < sitesArray.size(); i++) {
                    sitesList.add(sitesArray.getJSONObject(i).toBean(Map.class));
                }
            }
            
            // 设置结果信息
            result.setStatus(true);
            result.setMsg("获取成功");
            result.setData(sitesList);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website list response: " + e.getMessage(), e);
        }
    }
}