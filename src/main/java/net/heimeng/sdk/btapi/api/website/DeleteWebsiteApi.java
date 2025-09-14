package net.heimeng.sdk.btapi.api.website;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 删除网站API实现
 * <p>
 * 用于在宝塔面板中删除指定的网站，支持选择是否同时删除网站文件和数据库。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class DeleteWebsiteApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=DeleteSite";
    
    /**
     * 构造函数，创建一个新的DeleteWebsiteApi实例
     * 
     * @param domain 要删除的网站域名
     */
    public DeleteWebsiteApi(String domain) {
        super(ENDPOINT, HttpMethod.POST);
        
        if (StrUtil.isEmpty(domain)) {
            throw new IllegalArgumentException("Domain cannot be empty");
        }
        
        setDomain(domain);
        setDelFiles(false);
        setDelDatabase(false);
    }
    
    /**
     * 设置要删除的网站域名
     * 
     * @param domain 网站域名
     * @return 当前API实例，支持链式调用
     */
    public DeleteWebsiteApi setDomain(String domain) {
        addParam("domain", domain);
        return this;
    }
    
    /**
     * 设置是否同时删除网站文件
     * 
     * @param delFiles 是否删除网站文件
     * @return 当前API实例，支持链式调用
     */
    public DeleteWebsiteApi setDelFiles(boolean delFiles) {
        addParam("delFiles", delFiles);
        return this;
    }
    
    /**
     * 设置是否同时删除网站数据库
     * 
     * @param delDatabase 是否删除网站数据库
     * @return 当前API实例，支持链式调用
     */
    public DeleteWebsiteApi setDelDatabase(boolean delDatabase) {
        addParam("delDatabase", delDatabase);
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<Boolean>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Boolean>对象，data为true表示删除成功
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
            
            // 检查响应状态
            boolean success = json.getBool("status", false);
            result.setStatus(success);
            result.setMsg(json.getStr("msg", success ? "Website deleted successfully" : "Failed to delete website"));
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse delete website response: " + e.getMessage(), e);
        }
    }
}