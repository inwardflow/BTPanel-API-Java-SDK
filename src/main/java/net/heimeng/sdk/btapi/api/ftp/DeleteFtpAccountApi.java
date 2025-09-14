package net.heimeng.sdk.btapi.api.ftp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 删除FTP账户API实现
 * <p>
 * 用于在宝塔面板中删除指定的FTP账户。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class DeleteFtpAccountApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "ftp?action=DeleteFtp";
    
    /**
     * 构造函数，创建一个新的DeleteFtpAccountApi实例
     * 
     * @param username 要删除的FTP用户名
     */
    public DeleteFtpAccountApi(String username) {
        super(ENDPOINT, HttpMethod.POST);
        
        if (StrUtil.isEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        setUsername(username);
    }
    
    /**
     * 设置要删除的FTP用户名
     * 
     * @param username FTP用户名
     * @return 当前API实例，支持链式调用
     */
    public DeleteFtpAccountApi setUsername(String username) {
        addParam("name", username);
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
            result.setMsg(json.getStr("msg", success ? "FTP account deleted successfully" : "Failed to delete FTP account"));
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse delete FTP account response: " + e.getMessage(), e);
        }
    }
}