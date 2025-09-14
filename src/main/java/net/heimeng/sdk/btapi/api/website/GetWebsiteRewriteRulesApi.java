package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 获取网站伪静态规则API实现
 * <p>
 * 用于获取宝塔面板中网站的伪静态规则配置。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteRewriteRulesApi extends BaseBtApi<BtResult<String>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=getRewrite";
    
    /**
     * 构造函数，创建一个新的GetWebsiteRewriteRulesApi实例
     */
    public GetWebsiteRewriteRulesApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteRewriteRulesApi setId(Integer id) {
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
     * 解析API响应字符串为BtResult<String>对象
     * 
     * @param response API响应字符串
     * @return BtResult<String>对象，其中data为伪静态规则内容
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<String> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }
        
        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }
            
            JSONObject json = JSONUtil.parseObj(response);
            BtResult<String> result = new BtResult<>();
            
            // 检查响应状态
            boolean status = json.getBool("status", false);
            if (!status) {
                result.setStatus(false);
                result.setMsg(json.getStr("msg", "获取失败"));
                result.setData("");
                return result;
            }
            
            // 获取伪静态规则内容
            String rewriteRules = json.getStr("data", "");
            
            result.setStatus(true);
            result.setMsg("获取成功");
            result.setData(rewriteRules);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website rewrite rules response: " + e.getMessage(), e);
        }
    }
}