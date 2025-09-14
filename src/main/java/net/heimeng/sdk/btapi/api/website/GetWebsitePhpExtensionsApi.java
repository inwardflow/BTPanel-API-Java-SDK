package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 获取网站PHP扩展列表API实现
 * <p>
 * 用于获取宝塔面板中指定网站PHP版本的扩展列表。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsitePhpExtensionsApi extends BaseBtApi<BtResult<List<Map<String, Object>>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=GetPHPModules";
    
    /**
     * 构造函数，创建一个新的GetWebsitePhpExtensionsApi实例
     */
    public GetWebsitePhpExtensionsApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public GetWebsitePhpExtensionsApi setId(Integer id) {
        addParam("id", id);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("id");
    }
    
    /**
     * 解析API响应字符串为BtResult<List<Map<String, Object>>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<List<Map<String, Object>>>对象，其中data为PHP扩展列表
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<List<Map<String, Object>>> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }
        
        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }
            
            JSONObject json = JSONUtil.parseObj(response);
            BtResult<List<Map<String, Object>>> result = new BtResult<>();
            
            // 检查响应状态
            boolean status = json.getBool("status", false);
            if (!status) {
                result.setStatus(false);
                result.setMsg(json.getStr("msg", "获取失败"));
                result.setData(new ArrayList<>());
                return result;
            }
            
            // 获取PHP扩展列表
            JSONArray extensionsArray = json.getJSONArray("data");
            List<Map<String, Object>> extensionsList = new ArrayList<>();
            
            if (extensionsArray != null && !extensionsArray.isEmpty()) {
                for (int i = 0; i < extensionsArray.size(); i++) {
                    extensionsList.add(extensionsArray.getJSONObject(i).toBean(Map.class));
                }
            }
            
            result.setStatus(true);
            result.setMsg("获取成功");
            result.setData(extensionsList);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website PHP extensions response: " + e.getMessage(), e);
        }
    }
}