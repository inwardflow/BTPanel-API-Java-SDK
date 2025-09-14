package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取网站详细信息API实现
 * <p>
 * 用于获取宝塔面板中指定网站的详细信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteDetailApi extends BaseBtApi<BtResult<Map<String, Object>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=GetSiteStatus";
    
    /**
     * 构造函数，创建一个新的GetWebsiteDetailApi实例
     */
    public GetWebsiteDetailApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteDetailApi setId(Integer id) {
        addParam("id", id);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("id");
    }
    
    /**
     * 解析API响应字符串为BtResult<Map<String, Object>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Map<String, Object>>对象，其中data为网站详细信息
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<Map<String, Object>> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }
        
        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }
            
            JSONObject json = JSONUtil.parseObj(response);
            BtResult<Map<String, Object>> result = new BtResult<>();
            
            // 检查响应状态
            boolean status = json.getBool("status", false);
            if (!status) {
                result.setStatus(false);
                result.setMsg(json.getStr("msg", "获取失败"));
                result.setData(new HashMap<>());
                return result;
            }
            
            // 创建网站信息Map
            Map<String, Object> websiteInfo = new HashMap<>();
            
            // 将响应中的所有信息添加到Map中
            for (String key : json.keySet()) {
                if (!key.equals("status") && !key.equals("msg")) {
                    websiteInfo.put(key, json.get(key));
                }
            }
            
            result.setStatus(true);
            result.setMsg("获取成功");
            result.setData(websiteInfo);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website detail response: " + e.getMessage(), e);
        }
    }
}