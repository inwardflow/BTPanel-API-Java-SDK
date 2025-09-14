package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 设置网站流量限制API实现
 * <p>
 * 用于为宝塔面板中的网站设置流量限制（仅支持nginx）。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SetWebsiteLimitNetApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=SetLimitNet";
    
    /**
     * 构造函数，创建一个新的SetWebsiteLimitNetApi实例
     */
    public SetWebsiteLimitNetApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteLimitNetApi setId(Integer id) {
        addParam("id", id);
        return this;
    }
    
    /**
     * 设置是否启用流量限制
     * 
     * @param enabled 是否启用
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteLimitNetApi setEnabled(Boolean enabled) {
        addParam("enabled", enabled);
        return this;
    }
    
    /**
     * 设置服务器每秒最大请求数
     * 
     * @param perserver 服务器每秒最大请求数，0表示不限制
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteLimitNetApi setPerserver(Integer perserver) {
        addParam("perserver", perserver);
        return this;
    }
    
    /**
     * 设置单IP每秒最大请求数
     * 
     * @param perip 单IP每秒最大请求数，0表示不限制
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteLimitNetApi setPerip(Integer perip) {
        addParam("perip", perip);
        return this;
    }
    
    /**
     * 设置带宽限制（KB/s）
     * 
     * @param limitRate 带宽限制（KB/s），0表示不限制
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteLimitNetApi setLimitRate(Integer limitRate) {
        addParam("limit_rate", limitRate);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("id") && params.containsKey("enabled");
    }
    
    /**
     * 解析API响应字符串为BtResult<Boolean>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Boolean>对象
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
            boolean status = json.getBool("status", false);
            
            result.setStatus(status);
            result.setMsg(json.getStr("msg", status ? "设置成功" : "设置失败"));
            result.setData(status);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse set website limit net response: " + e.getMessage(), e);
        }
    }
}