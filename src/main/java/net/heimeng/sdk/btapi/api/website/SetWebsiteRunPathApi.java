package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 设置网站运行目录API实现
 * <p>
 * 用于设置宝塔面板中网站的运行目录（基于网站根目录的子目录）。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SetWebsiteRunPathApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=SetSiteRunPath";
    
    /**
     * 构造函数，创建一个新的SetWebsiteRunPathApi实例
     */
    public SetWebsiteRunPathApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteRunPathApi setId(Integer id) {
        addParam("id", id);
        return this;
    }
    
    /**
     * 设置运行目录
     * <p>基于网站根目录的运行目录，如：/public</p>
     * 
     * @param runPath 运行目录
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteRunPathApi setRunPath(String runPath) {
        addParam("runPath", runPath);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("id") && params.containsKey("runPath") &&
               params.get("runPath") != null;
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
            throw new BtApiException("Failed to parse set website run path response: " + e.getMessage(), e);
        }
    }
}