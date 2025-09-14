package net.heimeng.sdk.btapi.api.file;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 获取文件内容API实现
 * <p>
 * 用于获取宝塔面板中指定文件的内容，可用于获取伪静态规则或网站配置文件等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetFileContentApi extends BaseBtApi<BtResult<String>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "files?action=GetFileBody";
    
    /**
     * 构造函数，创建一个新的GetFileContentApi实例
     */
    public GetFileContentApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置文件路径
     * 
     * @param path 要被获取的文件路径
     * @return 当前API实例，支持链式调用
     */
    public GetFileContentApi setPath(String path) {
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
     * 解析API响应字符串为BtResult<String>对象
     * 
     * @param response API响应字符串
     * @return BtResult<String>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<String> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }
        
        try {
            BtResult<String> result = new BtResult<>();
            
            // 检查响应是否为JSON格式
            if (JSONUtil.isTypeJSON(response)) {
                JSONObject json = JSONUtil.parseObj(response);
                boolean status = json.getBool("status", false);
                
                result.setStatus(status);
                result.setMsg(json.getStr("msg", status ? "获取成功" : "获取失败"));
                result.setData(json.getStr("data", ""));
            } else {
                // 某些情况下，响应可能直接是文件内容
                result.setStatus(true);
                result.setMsg("获取成功");
                result.setData(response);
            }
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse get file content response: " + e.getMessage(), e);
        }
    }
}