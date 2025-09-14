package net.heimeng.sdk.btapi.api.ssl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 安装SSL证书API实现
 * <p>
 * 用于在宝塔面板中为指定网站安装SSL证书。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class InstallSslCertificateApi extends BaseBtApi<BtResult<Boolean>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "ssl?action=SaveSSL";
    
    /**
     * 构造函数，创建一个新的InstallSslCertificateApi实例
     * 
     * @param domain 要安装证书的网站域名
     * @param key 私钥内容
     * @param cert 证书内容
     */
    public InstallSslCertificateApi(String domain, String key, String cert) {
        super(ENDPOINT, HttpMethod.POST);
        
        // 设置必需参数
        setDomain(domain);
        setKey(key);
        setCert(cert);
        
        // 设置默认值
        setForceHttps(false);
        setAutoRenew(false);
    }
    
    /**
     * 设置要安装证书的网站域名
     * 
     * @param domain 网站域名
     * @return 当前API实例，支持链式调用
     */
    public InstallSslCertificateApi setDomain(String domain) {
        if (StrUtil.isEmpty(domain)) {
            throw new IllegalArgumentException("Domain cannot be empty");
        }
        addParam("domain", domain);
        return this;
    }
    
    /**
     * 设置私钥内容
     * 
     * @param key 私钥内容
     * @return 当前API实例，支持链式调用
     */
    public InstallSslCertificateApi setKey(String key) {
        if (StrUtil.isEmpty(key)) {
            throw new IllegalArgumentException("Private key cannot be empty");
        }
        addParam("key", key);
        return this;
    }
    
    /**
     * 设置证书内容
     * 
     * @param cert 证书内容
     * @return 当前API实例，支持链式调用
     */
    public InstallSslCertificateApi setCert(String cert) {
        if (StrUtil.isEmpty(cert)) {
            throw new IllegalArgumentException("Certificate cannot be empty");
        }
        addParam("cert", cert);
        return this;
    }
    
    /**
     * 设置是否强制使用HTTPS
     * 
     * @param forceHttps 是否强制使用HTTPS
     * @return 当前API实例，支持链式调用
     */
    public InstallSslCertificateApi setForceHttps(boolean forceHttps) {
        addParam("force_https", forceHttps ? 1 : 0);
        return this;
    }
    
    /**
     * 设置是否自动续期证书
     * 
     * @param autoRenew 是否自动续期
     * @return 当前API实例，支持链式调用
     */
    public InstallSslCertificateApi setAutoRenew(boolean autoRenew) {
        addParam("auto_renew", autoRenew ? 1 : 0);
        return this;
    }
    
    /**
     * 设置中间证书（可选）
     * 
     * @param ca 中间证书内容
     * @return 当前API实例，支持链式调用
     */
    public InstallSslCertificateApi setCa(String ca) {
        if (!StrUtil.isEmpty(ca)) {
            addParam("ca", ca);
        }
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<Boolean>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Boolean>对象，data为true表示安装成功
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
            result.setMsg(json.getStr("msg", success ? "SSL certificate installed successfully" : "Failed to install SSL certificate"));
            result.setData(success);
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse install SSL certificate response: " + e.getMessage(), e);
        }
    }
}