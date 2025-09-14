package net.heimeng.sdk.btapi.api.file;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 解压文件API实现
 * <p>
 * 用于在宝塔面板中解压文件。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class UncompressFileApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "files?action=UnCompress";
    
    /**
     * 构造函数，创建一个新的UncompressFileApi实例
     */
    public UncompressFileApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置压缩文件路径
     * 
     * @param path 压缩文件路径
     * @return 当前API实例，支持链式调用
     */
    public UncompressFileApi setPath(String path) {
        addParam("path", path);
        return this;
    }
    
    /**
     * 设置解压目标路径
     * 
     * @param target 解压目标路径
     * @return 当前API实例，支持链式调用
     */
    public UncompressFileApi setTarget(String target) {
        addParam("target", target);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("path") && 
               params.containsKey("target") &&
               params.get("path") != null && 
               !((String) params.get("path")).isEmpty() &&
               params.get("target") != null && 
               !((String) params.get("target")).isEmpty();
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
            result.setMsg(json.getStr("msg", status ? "解压成功" : "解压失败"));
            result.setData(status);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse uncompress file response: " + e.getMessage(), e);
        }
    }
}