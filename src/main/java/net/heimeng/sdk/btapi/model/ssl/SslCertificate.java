package net.heimeng.sdk.btapi.model.ssl;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * SSL证书信息模型类
 * <p>
 * 用于存储宝塔面板中SSL证书的基本信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class SslCertificate {
    
    /**
     * 证书ID
     */
    private int id;
    
    /**
     * 证书名称
     */
    private String name;
    
    /**
     * 证书类型（如免费、付费等）
     */
    private String type;
    
    /**
     * 证书域名列表
     */
    private List<String> domains;
    
    /**
     * 证书颁发者
     */
    private String issuer;
    
    /**
     * 证书生效日期
     */
    private Date validFrom;
    
    /**
     * 证书过期日期
     */
    private Date validTo;
    
    /**
     * 证书状态（如有效、过期、即将过期等）
     */
    private String status;
    
    /**
     * 是否为自动续期证书
     */
    private boolean autoRenew;
    
    /**
     * 证书指纹
     */
    private String fingerprint;
    
    /**
     * 判断证书是否有效
     * 
     * @return 证书是否有效
     */
    public boolean isValid() {
        return "valid".equalsIgnoreCase(status);
    }
    
    /**
     * 判断证书是否已过期
     * 
     * @return 证书是否已过期
     */
    public boolean isExpired() {
        return "expired".equalsIgnoreCase(status);
    }
    
    /**
     * 判断证书是否即将过期（30天内）
     * 
     * @return 证书是否即将过期
     */
    public boolean isExpiringSoon() {
        return "expiring_soon".equalsIgnoreCase(status);
    }
    
    /**
     * 获取证书有效期的可读格式
     * 
     * @return 格式化后的有效期
     */
    public String getFormattedValidityPeriod() {
        if (validFrom == null || validTo == null) {
            return "未知";
        }
        return String.format("%tF 至 %tF", validFrom, validTo);
    }
}