package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.WebsiteType;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取网站分类API实现
 * <p>
 * 用于获取宝塔面板中的网站分类列表。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsiteTypesApi extends BaseBtApi<BtResult<List<WebsiteType>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=get_site_types";
    
    /**
     * 构造函数，创建一个新的GetWebsiteTypesApi实例
     */
    public GetWebsiteTypesApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 解析API响应字符串为BtResult<List<WebsiteType>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<List<WebsiteType>>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<List<WebsiteType>> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            Object jsonObj = JSONUtil.parse(response);
            BtResult<List<WebsiteType>> result = new BtResult<>();
            List<WebsiteType> websiteTypes = new ArrayList<>();
            
            // 响应可能是直接的数组格式
            if (jsonObj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) jsonObj;
                result.setStatus(true);
                result.setMsg("Success");
                
                // 解析每个网站分类信息
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject typeJson = jsonArray.getJSONObject(i);
                    if (typeJson != null) {
                        WebsiteType websiteType = new WebsiteType();
                        websiteType.setId(typeJson.getInt("id", 0));
                        websiteType.setName(typeJson.getStr("name", ""));
                        
                        websiteTypes.add(websiteType);
                    }
                }
            } else if (jsonObj instanceof JSONObject) {
                // 响应可能是带有status字段的错误格式
                JSONObject json = (JSONObject) jsonObj;
                result.setStatus(json.getBool("status", false));
                result.setMsg(json.getStr("msg", ""));
            }
            
            result.setData(websiteTypes);
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse website types response: " + e.getMessage(), e);
        }
    }
}