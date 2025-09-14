package net.heimeng.sdk.btapi.api.file;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 移动或复制文件API实现
 * <p>
 * 用于在宝塔面板中移动或复制文件或目录。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class MoveFileApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "files?action=MoveFile";
    
    /**
     * 构造函数，创建一个新的MoveFileApi实例
     */
    public MoveFileApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置源文件或目录路径
     * 
     * @param source 源文件或目录路径
     * @return 当前API实例，支持链式调用
     */
    public MoveFileApi setSource(String source) {
        addParam("source", source);
        return this;
    }
    
    /**
     * 设置目标路径
     * 
     * @param target 目标路径
     * @return 当前API实例，支持链式调用
     */
    public MoveFileApi setTarget(String target) {
        addParam("target", target);
        return this;
    }
    
    /**
     * 设置操作类型（move/copy）
     * 
     * @param type 操作类型，move表示移动，copy表示复制
     * @return 当前API实例，支持链式调用
     */
    public MoveFileApi setType(String type) {
        addParam("type", type);
        return this;
    }
    
    /**
     * 设置是否移动类型（可选）
     * 
     * @param moveType 移动类型，0表示不移动，1表示移动
     * @return 当前API实例，支持链式调用
     */
    public MoveFileApi setMoveType(Integer moveType) {
        addParam("moveType", moveType);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("source") && 
               params.containsKey("target") &&
               params.containsKey("type") &&
               params.get("source") != null && 
               !((String) params.get("source")).isEmpty() &&
               params.get("target") != null && 
               !((String) params.get("target")).isEmpty() &&
               params.get("type") != null && 
               ("move".equals(params.get("type")) || "copy".equals(params.get("type")));
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
            result.setMsg(json.getStr("msg", status ? ("move".equals(params.get("type")) ? "移动成功" : "复制成功") : "操作失败"));
            result.setData(status);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse move file response: " + e.getMessage(), e);
        }
    }
}