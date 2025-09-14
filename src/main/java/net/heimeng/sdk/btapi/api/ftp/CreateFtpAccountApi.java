package net.heimeng.sdk.btapi.api.ftp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 创建FTP账户API实现
 * <p>
 * 用于在宝塔面板中创建新的FTP账户。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class CreateFtpAccountApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "ftp?action=AddFtp";
    
    /**
     * 构造函数，创建一个新的CreateFtpAccountApi实例
     * 
     * @param username FTP用户名
     * @param password FTP密码
     * @param path FTP主目录路径
     */
    public CreateFtpAccountApi(String username, String password, String path) {
        super(ENDPOINT, HttpMethod.POST);
        
        // 设置必需参数
        setUsername(username);
        setPassword(password);
        setPath(path);
        
        // 设置默认值
        setSize(0); // 0表示无限制
        setCanViewAll(false);
    }
    
    /**
     * 设置FTP用户名
     * 
     * @param username FTP用户名
     * @return 当前API实例，支持链式调用
     */
    public CreateFtpAccountApi setUsername(String username) {
        if (StrUtil.isEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        addParam("name", username);
        return this;
    }
    
    /**
     * 设置FTP密码
     * 
     * @param password FTP密码
     * @return 当前API实例，支持链式调用
     */
    public CreateFtpAccountApi setPassword(String password) {
        if (StrUtil.isEmpty(password)) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        addParam("password", password);
        return this;
    }
    
    /**
     * 设置FTP主目录路径
     * 
     * @param path FTP主目录路径
     * @return 当前API实例，支持链式调用
     */
    public CreateFtpAccountApi setPath(String path) {
        if (StrUtil.isEmpty(path)) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        addParam("path", path);
        return this;
    }
    
    /**
     * 设置FTP空间大小限制（单位：MB），0表示无限制
     * 
     * @param size 空间大小限制
     * @return 当前API实例，支持链式调用
     */
    public CreateFtpAccountApi setSize(long size) {
        addParam("size", size);
        return this;
    }
    
    /**
     * 设置是否允许查看所有目录
     * 
     * @param canViewAll 是否允许查看所有目录
     * @return 当前API实例，支持链式调用
     */
    public CreateFtpAccountApi setCanViewAll(boolean canViewAll) {
        addParam("ps", canViewAll ? 1 : 0);
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<Boolean>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Boolean>对象，data为true表示创建成功
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
            result.setMsg(json.getStr("msg", success ? "FTP account created successfully" : "Failed to create FTP account"));
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse create FTP account response: " + e.getMessage(), e);
        }
    }
}