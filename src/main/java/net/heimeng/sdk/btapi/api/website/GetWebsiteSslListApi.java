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
 * 获取SSL证书列表API实现
 * <p>
 * 用于获取宝塔面板中指定网站的SSL证书列表。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteSslListApi extends BaseBtApi<BtResult<List<Map<String, Object>>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=GetSSLCertList";
    
    /**
     * 构造函数，创建一个新的GetWebsiteSslListApi实例
     */
    public GetWebsiteSslListApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteSslListApi setId(Integer id) {
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
     * @return BtResult<List<Map<String, Object>>>对象
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
            
            // 获取证书列表
            JSONArray certsArray = json.getJSONArray("certs");
            List<Map<String, Object>> certsList = new ArrayList<>();
            
            if (certsArray != null && !certsArray.isEmpty()) {
                for (int i = 0; i < certsArray.size(); i++) {
                    certsList.add(certsArray.getJSONObject(i).toBean(Map.class));
                }
            }
            
            result.setStatus(true);
            result.setMsg("获取成功");
            result.setData(certsList);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website SSL list response: " + e.getMessage(), e);
        }
    }
}