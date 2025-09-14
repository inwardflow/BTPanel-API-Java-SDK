package net.heimeng.sdk.btapi.api.ftp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 修改FTP账户密码API实现
 * <p>
 * 用于在宝塔面板中修改指定FTP账户的密码。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class ChangeFtpPasswordApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "ftp?action=ChangeFtpPassword";
    
    /**
     * 构造函数，创建一个新的ChangeFtpPasswordApi实例
     * 
     * @param username FTP用户名
     * @param newPassword 新密码
     */
    public ChangeFtpPasswordApi(String username, String newPassword) {
        super(ENDPOINT, HttpMethod.POST);
        
        // 设置必需参数
        setUsername(username);
        setNewPassword(newPassword);
    }
    
    /**
     * 设置FTP用户名
     * 
     * @param username FTP用户名
     * @return 当前API实例，支持链式调用
     */
    public ChangeFtpPasswordApi setUsername(String username) {
        if (StrUtil.isEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        addParam("name", username);
        return this;
    }
    
    /**
     * 设置FTP新密码
     * 
     * @param newPassword 新密码
     * @return 当前API实例，支持链式调用
     */
    public ChangeFtpPasswordApi setNewPassword(String newPassword) {
        if (StrUtil.isEmpty(newPassword)) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        addParam("password", newPassword);
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<Boolean>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Boolean>对象，data为true表示修改密码成功
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
            result.setMsg(json.getStr("msg", success ? "FTP password changed successfully" : "Failed to change FTP password"));
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse change FTP password response: " + e.getMessage(), e);
        }
    }
}