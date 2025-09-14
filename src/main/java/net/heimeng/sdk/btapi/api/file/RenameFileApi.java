package net.heimeng.sdk.btapi.api.file;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 重命名文件API实现
 * <p>
 * 用于在宝塔面板中重命名文件或目录。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class RenameFileApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "files?action=RenameFile";
    
    /**
     * 构造函数，创建一个新的RenameFileApi实例
     */
    public RenameFileApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置原始文件或目录路径
     * 
     * @param oldPath 原始文件或目录路径
     * @return 当前API实例，支持链式调用
     */
    public RenameFileApi setOldPath(String oldPath) {
        addParam("oldpath", oldPath);
        return this;
    }
    
    /**
     * 设置新文件名或目录名
     * 
     * @param newName 新文件名或目录名
     * @return 当前API实例，支持链式调用
     */
    public RenameFileApi setNewName(String newName) {
        addParam("newname", newName);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("oldpath") && 
               params.containsKey("newname") &&
               params.get("oldpath") != null && 
               !((String) params.get("oldpath")).isEmpty() &&
               params.get("newname") != null && 
               !((String) params.get("newname")).isEmpty();
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
            result.setMsg(json.getStr("msg", status ? "重命名成功" : "重命名失败"));
            result.setData(status);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse rename file response: " + e.getMessage(), e);
        }
    }
}