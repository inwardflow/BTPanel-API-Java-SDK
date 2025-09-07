package net.heimeng.sdk.btapi.v2.exception;

/**
 * 认证异常类，用于表示API认证相关的错误
 * <p>
 * 该异常类继承自BtApiException，专门用于处理API密钥无效、认证失败等认证相关的错误情况。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class BtAuthenticationException extends BtApiException {
    
    /**
     * 认证类型
     */
    private final String authType;
    
    /**
     * 构造函数，创建一个新的BtAuthenticationException实例
     * 
     * @param message 错误消息
     */
    public BtAuthenticationException(String message) {
        this(message, null, null);
    }
    
    /**
     * 构造函数，创建一个新的BtAuthenticationException实例
     * 
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BtAuthenticationException(String message, Throwable cause) {
        this(message, null, cause);
    }
    
    /**
     * 构造函数，创建一个新的BtAuthenticationException实例
     * 
     * @param message 错误消息
     * @param authType 认证类型
     */
    public BtAuthenticationException(String message, String authType) {
        this(message, authType, null);
    }
    
    /**
     * 完整的构造函数，创建一个新的BtAuthenticationException实例
     * 
     * @param message 错误消息
     * @param authType 认证类型
     * @param cause 原始异常
     */
    public BtAuthenticationException(String message, String authType, Throwable cause) {
        super(message, "AUTHENTICATION_FAILED", null, cause);
        this.authType = authType;
    }
    
    /**
     * 获取认证类型
     * 
     * @return 认证类型，如果没有设置则返回null
     */
    public String getAuthType() {
        return authType;
    }
}