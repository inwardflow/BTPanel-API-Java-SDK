package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 设置网站伪静态规则API实现
 * <p>
 * 用于为宝塔面板中的网站设置伪静态规则。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SetWebsiteRewriteRulesApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=setRewrite";
    
    /**
     * 构造函数，创建一个新的SetWebsiteRewriteRulesApi实例
     */
    public SetWebsiteRewriteRulesApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteRewriteRulesApi setId(Integer id) {
        addParam("id", id);
        return this;
    }
    
    /**
     * 设置伪静态规则名称
     * 
     * @param name 规则名称
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteRewriteRulesApi setName(String name) {
        addParam("name", name);
        return this;
    }
    
    /**
     * 设置伪静态规则内容
     * 
     * @param content 规则内容
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteRewriteRulesApi setContent(String content) {
        addParam("content", content);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("id") && 
               params.containsKey("name") &&
               params.containsKey("content") &&
               params.get("name") != null && 
               !((String) params.get("name")).isEmpty() &&
               params.get("content") != null;
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
            throw new BtApiException("Failed to parse set website rewrite rules response: " + e.getMessage(), e);
        }
    }
}