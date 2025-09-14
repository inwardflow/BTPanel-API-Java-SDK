package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 设置网站PHP版本API实现
 * <p>
 * 用于为宝塔面板中的网站设置PHP版本。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SetWebsitePhpVersionApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=SetPhpVersion";
    
    /**
     * 构造函数，创建一个新的SetWebsitePhpVersionApi实例
     */
    public SetWebsitePhpVersionApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public SetWebsitePhpVersionApi setId(Integer id) {
        addParam("id", id);
        return this;
    }
    
    /**
     * 设置PHP版本号
     * 
     * @param phpVersion PHP版本号，如 "56", "70", "71", "72", "73", "74", "80", "81", "82"
     * @return 当前API实例，支持链式调用
     */
    public SetWebsitePhpVersionApi setPhpVersion(String phpVersion) {
        addParam("php_version", phpVersion);
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
               params.containsKey("php_version") &&
               params.get("php_version") != null && 
               !((String) params.get("php_version")).isEmpty();
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
            throw new BtApiException("Failed to parse set website PHP version response: " + e.getMessage(), e);
        }
    }
}