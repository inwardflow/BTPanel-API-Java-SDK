package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 删除网站备份API实现
 * <p>
 * 用于删除宝塔面板中的网站备份。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class DeleteWebsiteBackupApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=DelBackup";
    
    /**
     * 构造函数，创建一个新的DeleteWebsiteBackupApi实例
     */
    public DeleteWebsiteBackupApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置备份列表ID
     * 
     * @param id 备份列表ID
     * @return 当前API实例，支持链式调用
     */
    public DeleteWebsiteBackupApi setId(Integer id) {
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
            result.setMsg(json.getStr("msg", status ? "删除成功" : "删除失败"));
            result.setData(status);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse delete website backup response: " + e.getMessage(), e);
        }
    }
}