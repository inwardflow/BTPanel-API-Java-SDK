package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.WebsiteInfo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 获取网站列表API实现
 * <p>
 * 用于获取宝塔面板中所有网站的列表信息，支持分页查询。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetWebsitesApi extends BaseBtApi<BtResult<List<WebsiteInfo>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "data?action=getData&table=sites";
    
    /**
     * 日期格式解析器
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    
    /**
     * 构造函数，创建一个新的GetWebsitesApi实例
     * 
     * @param page 页码，从1开始
     * @param limit 每页记录数
     */
    public GetWebsitesApi(int page, int limit) {
        super(ENDPOINT, HttpMethod.POST);
        addParam("p", page);
        addParam("limit", limit);
    }
    
    /**
     * 构造函数，创建一个新的GetWebsitesApi实例，使用默认分页参数
     */
    public GetWebsitesApi() {
        this(1, 10);
    }
    
    /**
     * 设置页码
     * 
     * @param page 页码，从1开始
     * @return 当前API实例，支持链式调用
     */
    public GetWebsitesApi setPage(int page) {
        addParam("p", page);
        return this;
    }
    
    /**
     * 设置每页记录数
     * 
     * @param limit 每页记录数
     * @return 当前API实例，支持链式调用
     */
    public GetWebsitesApi setLimit(int limit) {
        addParam("limit", limit);
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<List<WebsiteInfo>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<List<WebsiteInfo>>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<List<WebsiteInfo>> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }
        
        try {
            // 检查响应是否为有效的JSON
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }
            
            // 解析JSON对象
            JSONObject json = JSONUtil.parseObj(response);
            
            // 创建结果对象
            BtResult<List<WebsiteInfo>> result = new BtResult<>();
            // 宝塔面板API可能没有status字段，根据是否有data字段判断成功
            result.setStatus(json.containsKey("data") && json.getJSONArray("data") != null);
            result.setMsg(json.getStr("msg", "Success"));
            
            // 解析网站列表
            JSONArray dataArray = json.getJSONArray("data");
            if (dataArray != null) {
                List<WebsiteInfo> websites = new ArrayList<>(dataArray.size());
                
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject websiteJson = dataArray.getJSONObject(i);
                    if (websiteJson != null) {
                        WebsiteInfo website = new WebsiteInfo();
                        website.setId(websiteJson.getLong("id", 0L));
                        website.setName(websiteJson.getStr("name", ""));
                        
                        // 处理域名字段 - 从name字段获取，因为domain字段是数量
                        website.setDomain(websiteJson.getStr("name", ""));
                        
                        website.setPath(websiteJson.getStr("path", ""));
                        
                        // 处理网站类型 - 从project_type字段获取
                        website.setType(websiteJson.getStr("project_type", ""));
                        
                        // 处理状态字段 - 字符串转整数
                        String statusStr = websiteJson.getStr("status", "0");
                        website.setStatus(Integer.parseInt(statusStr));
                        
                        // 处理SSL状态 - -1表示未开启
                        int sslValue = websiteJson.getInt("ssl", -1);
                        website.setSsl(sslValue == 1 ? 1 : 0);
                        
                        // 处理创建时间 - 日期字符串转时间戳
                        String addtimeStr = websiteJson.getStr("addtime", "");
                        if (!addtimeStr.isEmpty()) {
                            try {
                                Date date = DATE_FORMAT.parse(addtimeStr);
                                website.setCreateTime(date.getTime() / 1000);
                            } catch (ParseException e) {
                                // 如果解析失败，设置为0
                                website.setCreateTime(0L);
                            }
                        } else {
                            website.setCreateTime(0L);
                        }
                        
                        websites.add(website);
                    }
                }
                
                result.setData(websites);
            }
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse websites response: " + e.getMessage(), e);
        }
    }
}