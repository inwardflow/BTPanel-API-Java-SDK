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
 * 用于在宝塔面板中删除指定的网站，支持选择是否同时删除关联的FTP、数据库和网站根目录。
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
     * @param id 网站ID
     * @param webname 网站名称
     */
    public DeleteWebsiteApi(int id, String webname) {
        super(ENDPOINT, HttpMethod.POST);
        
        if (id <= 0) {
            throw new IllegalArgumentException("Website ID must be positive");
        }
        
        if (StrUtil.isEmpty(webname)) {
            throw new IllegalArgumentException("Website name cannot be empty");
        }
        
        setId(id);
        setWebname(webname);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public DeleteWebsiteApi setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Website ID must be positive");
        }
        addParam("id", id);
        return this;
    }
    
    /**
     * 设置网站名称
     * 
     * @param webname 网站名称
     * @return 当前API实例，支持链式调用
     */
    public DeleteWebsiteApi setWebname(String webname) {
        if (StrUtil.isEmpty(webname)) {
            throw new IllegalArgumentException("Website name cannot be empty");
        }
        addParam("webname", webname);
        return this;
    }
    
    /**
     * 设置是否删除关联的FTP
     * <p>注意：如果不删除请不要调用此方法</p>
     * 
     * @param deleteFtp 是否删除关联FTP
     * @return 当前API实例，支持链式调用
     */
    public DeleteWebsiteApi setDeleteFtp(boolean deleteFtp) {
        if (deleteFtp) {
            addParam("ftp", 1);
        }
        return this;
    }
    
    /**
     * 设置是否删除关联的数据库
     * <p>注意：如果不删除请不要调用此方法</p>
     * 
     * @param deleteDatabase 是否删除关联数据库
     * @return 当前API实例，支持链式调用
     */
    public DeleteWebsiteApi setDeleteDatabase(boolean deleteDatabase) {
        if (deleteDatabase) {
            addParam("database", 1);
        }
        return this;
    }
    
    /**
     * 设置是否删除网站根目录
     * <p>注意：如果不删除请不要调用此方法</p>
     * 
     * @param deletePath 是否删除网站根目录
     * @return 当前API实例，支持链式调用
     */
    public DeleteWebsiteApi setDeletePath(boolean deletePath) {
        if (deletePath) {
            addParam("path", 1);
        }
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("id") && params.containsKey("webname");
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
            result.setMsg(json.getStr("msg", success ? "网站删除成功" : "网站删除失败"));
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse delete website response: " + e.getMessage(), e);
        }
    }
}