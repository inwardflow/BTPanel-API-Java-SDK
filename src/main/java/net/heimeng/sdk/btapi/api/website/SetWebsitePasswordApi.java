package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 设置密码访问API实现
 * <p>
 * 用于为宝塔面板中的网站设置访问密码保护。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SetWebsitePasswordApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=SetHasPwd";
    
    /**
     * 构造函数，创建一个新的SetWebsitePasswordApi实例
     */
    public SetWebsitePasswordApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public SetWebsitePasswordApi setId(Integer id) {
        addParam("id", id);
        return this;
    }
    
    /**
     * 设置用户名
     * 
     * @param username 用户名
     * @return 当前API实例，支持链式调用
     */
    public SetWebsitePasswordApi setUsername(String username) {
        addParam("username", username);
        return this;
    }
    
    /**
     * 设置密码
     * 
     * @param password 密码
     * @return 当前API实例，支持链式调用
     */
    public SetWebsitePasswordApi setPassword(String password) {
        addParam("password", password);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("id") && params.containsKey("username") && params.containsKey("password") &&
               params.get("username") != null && !((String) params.get("username")).isEmpty() &&
               params.get("password") != null && !((String) params.get("password")).isEmpty();
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
            throw new BtApiException("Failed to parse set website password response: " + e.getMessage(), e);
        }
    }
}