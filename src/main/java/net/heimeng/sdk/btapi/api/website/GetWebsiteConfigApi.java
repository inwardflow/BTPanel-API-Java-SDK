package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取网站配置信息API实现
 * <p>
 * 用于获取宝塔面板中指定网站的防跨站配置、运行目录、日志开关状态、可设置的运行目录列表、密码访问状态等信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteConfigApi extends BaseBtApi<BtResult<Map<String, Object>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=GetDirUserINI";
    
    /**
     * 构造函数，创建一个新的GetWebsiteConfigApi实例
     */
    public GetWebsiteConfigApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteConfigApi setId(Integer id) {
        addParam("id", id);
        return this;
    }
    
    /**
     * 设置网站根目录
     * 
     * @param path 网站根目录
     * @return 当前API实例，支持链式调用
     */
    public GetWebsiteConfigApi setPath(String path) {
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
        return params.containsKey("id") && params.containsKey("path");
    }
    
    /**
     * 解析API响应字符串为BtResult<Map<String, Object>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Map<String, Object>>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<Map<String, Object>> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }
        
        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }
            
            JSONObject json = JSONUtil.parseObj(response);
            BtResult<Map<String, Object>> result = new BtResult<>();
            
            // 创建配置信息Map
            Map<String, Object> configMap = new HashMap<>();
            
            // 解析各项配置
            configMap.put("pass", json.getBool("pass", false));
            configMap.put("logs", json.getBool("logs", true));
            configMap.put("userini", json.getBool("userini", true));
            
            // 解析运行目录信息
            JSONObject runPathJson = json.getJSONObject("runPath");
            if (runPathJson != null) {
                Map<String, Object> runPathMap = new HashMap<>();
                
                // 解析可设置的运行目录列表
                JSONArray dirsArray = runPathJson.getJSONArray("dirs");
                if (dirsArray != null) {
                    List<String> dirsList = new ArrayList<>();
                    for (int i = 0; i < dirsArray.size(); i++) {
                        dirsList.add(dirsArray.getStr(i, ""));
                    }
                    runPathMap.put("dirs", dirsList);
                }
                
                // 解析当前运行目录
                runPathMap.put("runPath", runPathJson.getStr("runPath", "/"));
                
                configMap.put("runPath", runPathMap);
            }
            
            result.setStatus(true);
            result.setMsg("获取成功");
            result.setData(configMap);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website config response: " + e.getMessage(), e);
        }
    }
}