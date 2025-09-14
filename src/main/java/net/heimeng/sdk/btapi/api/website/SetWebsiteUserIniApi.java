package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 设置防跨站状态API实现
 * <p>
 * 用于设置宝塔面板中网站的防跨站状态（自动取反）。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SetWebsiteUserIniApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=SetDirUserINI";
    
    /**
     * 构造函数，创建一个新的SetWebsiteUserIniApi实例
     */
    public SetWebsiteUserIniApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站根目录
     * 
     * @param path 网站根目录
     * @return 当前API实例，支持链式调用
     */
    public SetWebsiteUserIniApi setPath(String path) {
        addParam("path", path);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("path") && params.get("path") != null && !((String) params.get("path")).isEmpty();
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
            throw new BtApiException("Failed to parse set website user ini response: " + e.getMessage(), e);
        }
    }
}