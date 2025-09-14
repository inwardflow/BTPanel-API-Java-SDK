package net.heimeng.sdk.btapi.api.ssl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 获取SSL证书列表API实现
 * <p>
 * 用于获取宝塔面板中的所有SSL证书信息列表。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetSslCertificatesApi extends BaseBtApi<BtResult<List<SslCertificate>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "ssl?action=getData";
    
    /**
     * 构造函数，创建一个新的GetSslCertificatesApi实例
     */
    public GetSslCertificatesApi() {
        super(ENDPOINT, HttpMethod.POST);
        
        // 设置默认参数
        addParam("table", "ssl");
    }
    
    /**
     * 解析API响应字符串为BtResult<List<SslCertificate>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<List<SslCertificate>>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<List<SslCertificate>> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            JSONObject json = JSONUtil.parseObj(response);
            BtResult<List<SslCertificate>> result = new BtResult<>();
            List<SslCertificate> certificates = new ArrayList<>();
            
            // 检查响应是否包含data字段
            if (json.containsKey("data")) {
                result.setStatus(true);
                result.setMsg("Success");
                
                // 解析SSL证书列表
                JSONArray certArray = json.getJSONArray("data");
                if (certArray != null && !certArray.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    
                    for (int i = 0; i < certArray.size(); i++) {
                        JSONObject certJson = certArray.getJSONObject(i);
                        if (certJson != null) {
                            SslCertificate certificate = new SslCertificate();
                            
                            // 设置证书基本信息
                            certificate.setId(certJson.getInt("id", 0));
                            certificate.setName(certJson.getStr("name", ""));
                            certificate.setType(certJson.getStr("type", ""));
                            certificate.setIssuer(certJson.getStr("issuer", ""));
                            certificate.setStatus(certJson.getStr("status", "unknown"));
                            certificate.setAutoRenew(certJson.getBool("auto_renew", false));
                            certificate.setFingerprint(certJson.getStr("fingerprint", ""));
                            
                            // 解析域名列表
                            String domainsStr = certJson.getStr("domains", "");
                            if (!domainsStr.isEmpty()) {
                                List<String> domains = new ArrayList<>();
                                for (String domain : domainsStr.split(",")) {
                                    if (!domain.trim().isEmpty()) {
                                        domains.add(domain.trim());
                                    }
                                }
                                certificate.setDomains(domains);
                            }
                            
                            // 解析有效期
                            try {
                                String validFromStr = certJson.getStr("valid_from", "");
                                if (!validFromStr.isEmpty()) {
                                    certificate.setValidFrom(dateFormat.parse(validFromStr));
                                }
                                
                                String validToStr = certJson.getStr("valid_to", "");
                                if (!validToStr.isEmpty()) {
                                    certificate.setValidTo(dateFormat.parse(validToStr));
                                }
                            } catch (ParseException e) {
                                // 忽略时间解析错误
                            }
                            
                            // 根据有效期计算证书状态（如果状态未设置）
                            if ("unknown".equals(certificate.getStatus()) && certificate.getValidTo() != null) {
                                Date now = new Date();
                                long daysDiff = (certificate.getValidTo().getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
                                
                                if (daysDiff < 0) {
                                    certificate.setStatus("expired");
                                } else if (daysDiff <= 30) {
                                    certificate.setStatus("expiring_soon");
                                } else {
                                    certificate.setStatus("valid");
                                }
                            }
                            
                            certificates.add(certificate);
                        }
                    }
                }
            } else if (json.containsKey("status")) {
                // 处理错误响应
                result.setStatus(json.getBool("status", false));
                result.setMsg(json.getStr("msg", ""));
            }
            
            result.setData(certificates);
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse SSL certificates response: " + e.getMessage(), e);
        }
    }
}