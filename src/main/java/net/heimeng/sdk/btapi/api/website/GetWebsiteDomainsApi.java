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
 * 获取网站域名列表API实现
 * <p>
 * 用于获取宝塔面板中指定网站的域名列表。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteDomainsApi extends BaseBtApi<BtResult<List<Map<String, Object>>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "data?action=getData&table=domain";
    
    /**
     * 构造函数，创建一个新的GetWebsiteDomainsApi实例
     */
    public GetWebsiteDomainsApi() {
        super(ENDPOINT, HttpMethod.POST);
        // 设置默认参数
        addParam("list", true);
    }
    
    /**
     * 设置网站ID
     * 
     * @param siteId 网站ID
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteDomainsApi setSiteId(Integer siteId) {
        addParam("search", siteId);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("search") && params.containsKey("list");
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
            
            BtResult<List<Map<String, Object>>> result = new BtResult<>();
            result.setStatus(true);
            result.setMsg("Success");
            
            // 解析域名列表数据
            JSONArray dataArray;
            Object jsonObj = JSONUtil.parse(response);
            
            if (jsonObj instanceof JSONArray) {
                dataArray = (JSONArray) jsonObj;
            } else if (jsonObj instanceof JSONObject) {
                dataArray = ((JSONObject) jsonObj).getJSONArray("data");
            } else {
                throw new BtApiException("Invalid response format");
            }
            
            List<Map<String, Object>> domains = new ArrayList<>();
            
            if (dataArray != null) {
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject domainJson = dataArray.getJSONObject(i);
                    if (domainJson != null) {
                        Map<String, Object> domainMap = new HashMap<>();
                        // 将JSON对象转换为Map
                        for (String key : domainJson.keySet()) {
                            domainMap.put(key, domainJson.get(key));
                        }
                        domains.add(domainMap);
                    }
                }
            }
            
            result.setData(domains);
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website domains response: " + e.getMessage(), e);
        }
    }
}