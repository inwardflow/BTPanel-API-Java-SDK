package net.heimeng.sdk.btapi.api.ssl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 删除SSL证书API实现
 * <p>
 * 用于在宝塔面板中删除指定的SSL证书。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class DeleteSslCertificateApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "ssl?action=DeleteSSL";
    
    /**
     * 构造函数，创建一个新的DeleteSslCertificateApi实例
     * 
     * @param id 要删除的证书ID
     */
    public DeleteSslCertificateApi(int id) {
        super(ENDPOINT, HttpMethod.POST);
        
        setId(id);
    }
    
    /**
     * 设置要删除的证书ID
     * 
     * @param id 证书ID
     * @return 当前API实例，支持链式调用
     */
    public DeleteSslCertificateApi setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Certificate ID must be positive");
        }
        addParam("id", id);
        return this;
    }
    
    /**
     * 设置要删除的证书域名（可选，用于确认）
     * 
     * @param domain 证书域名
     * @return 当前API实例，支持链式调用
     */
    public DeleteSslCertificateApi setDomain(String domain) {
        if (!StrUtil.isEmpty(domain)) {
            addParam("domain", domain);
        }
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<Boolean>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Boolean>对象，data为true表示删除成功
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
            
            // 检查响应状态
            boolean success = json.getBool("status", false);
            result.setStatus(success);
            result.setMsg(json.getStr("msg", success ? "SSL certificate deleted successfully" : "Failed to delete SSL certificate"));
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse delete SSL certificate response: " + e.getMessage(), e);
        }
    }
}