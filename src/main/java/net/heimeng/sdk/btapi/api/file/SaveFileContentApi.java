package net.heimeng.sdk.btapi.api.file;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 保存文件内容API实现
 * <p>
 * 用于保存宝塔面板中指定文件的内容，可用于保存伪静态规则或网站配置文件等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class SaveFileContentApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "files?action=SaveFileBody";
    
    /**
     * 构造函数，创建一个新的SaveFileContentApi实例
     */
    public SaveFileContentApi() {
        super(ENDPOINT, HttpMethod.POST);
        // 设置默认编码
        addParam("encoding", "utf-8");
    }
    
    /**
     * 设置保存位置
     * 
     * @param path 文件保存路径
     * @return 当前API实例，支持链式调用
     */
    public SaveFileContentApi setPath(String path) {
        addParam("path", path);
        return this;
    }
    
    /**
     * 设置文件内容
     * 
     * @param data 文件内容
     * @return 当前API实例，支持链式调用
     */
    public SaveFileContentApi setData(String data) {
        addParam("data", data);
        return this;
    }
    
    /**
     * 设置文件编码
     * 
     * @param encoding 文件编码
     * @return 当前API实例，支持链式调用
     */
    public SaveFileContentApi setEncoding(String encoding) {
        addParam("encoding", encoding);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("path") && params.containsKey("data") && params.containsKey("encoding") &&
               params.get("path") != null && !((String) params.get("path")).isEmpty() &&
               params.get("encoding") != null && !((String) params.get("encoding")).isEmpty();
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
            result.setMsg(json.getStr("msg", status ? "保存成功" : "保存失败"));
            result.setData(status);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse save file content response: " + e.getMessage(), e);
        }
    }
}