package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 设置SSL证书API实现
 * <p>
 * 用于为宝塔面板中的网站设置SSL证书。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SetWebsiteSslApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=SetSSL";
    
    /**
     * 构造函数，创建一个新的SetWebsiteSslApi实例
     */
    public SetWebsiteSslApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteSslApi setId(Integer id) {
        addParam("id", id);
        return this;
    }
    
    /**
     * 设置证书域名
     * 
     * @param domain 证书域名
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteSslApi setDomain(String domain) {
        addParam("domain", domain);
        return this;
    }
    
    /**
     * 设置证书内容
     * 
     * @param cert 证书内容
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteSslApi setCert(String cert) {
        addParam("cert", cert);
        return this;
    }
    
    /**
     * 设置私钥内容
     * 
     * @param key 私钥内容
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteSslApi setKey(String key) {
        addParam("key", key);
        return this;
    }
    
    /**
     * 设置是否强制HTTPS
     * 
     * @param forceHttps 是否强制HTTPS
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteSslApi setForceHttps(Boolean forceHttps) {
        addParam("force_https", forceHttps);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("id") && params.containsKey("domain") && 
               params.containsKey("cert") && params.containsKey("key") &&
               params.get("domain") != null && !((String) params.get("domain")).isEmpty() &&
               params.get("cert") != null && !((String) params.get("cert")).isEmpty() &&
               params.get("key") != null && !((String) params.get("key")).isEmpty();
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
            throw new BtApiException("Failed to parse set website SSL response: " + e.getMessage(), e);
        }
    }
}