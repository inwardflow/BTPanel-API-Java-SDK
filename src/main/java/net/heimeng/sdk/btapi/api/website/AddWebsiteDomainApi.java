package net.heimeng.sdk.btapi.api.website;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 添加域名API实现
 * <p>
 * 用于为宝塔面板中的指定网站添加域名。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class AddWebsiteDomainApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=AddDomain";
    
    /**
     * 构造函数，创建一个新的AddWebsiteDomainApi实例
     */
    public AddWebsiteDomainApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 设置网站ID
     * 
     * @param id 网站ID
     * @return 当前API实例，支持链式调用
     */
    public AddWebsiteDomainApi setId(Integer id) {
        addParam("id", id);
        return this;
    }
    
    /**
     * 设置网站名称
     * 
     * @param webname 网站名称
     * @return 当前API实例，支持链式调用
     */
    public AddWebsiteDomainApi setWebname(String webname) {
        addParam("webname", webname);
        return this;
    }
    
    /**
     * 设置要添加的域名
     * <p>多个域名用换行符隔开</p>
     * 
     * @param domain 域名（格式：域名:端口）
     * @return 当前API实例，支持链式调用
     */
    public AddWebsiteDomainApi setDomain(String domain) {
        addParam("domain", domain);
        return this;
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    @Override
    protected boolean validateParams() {
        return params.containsKey("id") && params.containsKey("webname") && params.containsKey("domain");
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
            result.setMsg(json.getStr("msg", status ? "域名添加成功" : "域名添加失败"));
            result.setData(status);
            
            return result;
        } catch (Exception e) {
            throw new BtApiException("Failed to parse add website domain response: " + e.getMessage(), e);
        }
    }
}