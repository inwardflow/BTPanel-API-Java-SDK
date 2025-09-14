package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取流量限制相关配置API实现
 * <p>
 * 用于获取宝塔面板中指定网站的流量限制配置（仅支持nginx）。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteLimitNetApi extends BaseBtApi<BtResult<Map<String, Object>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=GetLimitNet";
    
    /**
     * 构造函数，创建一个新的GetWebsiteLimitNetApi实例
     */
    public GetWebsiteLimitNetApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteLimitNetApi setId(Integer id) {
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
     * @return BtResult<Map<String, Object>>对象
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
            
            // 创建配置信息Map
            Map<String, Object> limitConfigMap = new HashMap<>();
            
            // 解析各项配置
            limitConfigMap.put("perserver", json.getInt("perserver", 0));
            limitConfigMap.put("perip", json.getInt("perip", 0));
            limitConfigMap.put("limit_rate", json.getInt("limit_rate", 0));
            limitConfigMap.put("enabled", json.getBool("enabled", false));
            
            // 添加其他可能的配置项
            for (String key : json.keySet()) {
                if (!key.equals("status") && !key.equals("msg")) {
                    limitConfigMap.put(key, json.get(key));
                }
            }
            
            result.setStatus(true);
            result.setMsg("获取成功");
            result.setData(limitConfigMap);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website limit net response: " + e.getMessage(), e);
        }
    }
}