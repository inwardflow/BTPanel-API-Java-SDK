package net.heimeng.sdk.btapi.exception;

/**
 * BtAuthenticationException表示认证相关的异常，如API密钥无效、认证失败等
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class BtAuthenticationException extends BtSdkException {

    /**
     * 构造函数，创建一个新的BtAuthenticationException实例
     *
     * @param message 错误消息
     */
    public BtAuthenticationException(String message) {
        super(message, "AUTHENTICATION_ERROR");
    }

    /**
     * 构造函数，创建一个新的BtAuthenticationException实例
     *
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BtAuthenticationException(String message, Throwable cause) {
        super(message, "AUTHENTICATION_ERROR", null, cause);
    }

    /**
     * 构造函数，创建一个新的BtAuthenticationException实例
     *
     * @param message 错误消息
     * @param errorData 错误数据
     */
    public BtAuthenticationException(String message, Object errorData) {
        super(message, "AUTHENTICATION_ERROR", errorData, null);
    }

    /**
     * 构造函数，创建一个新的BtAuthenticationException实例
     *
     * @param message 错误消息
     * @param errorData 错误数据
     * @param cause 原始异常
     */
    public BtAuthenticationException(String message, Object errorData, Throwable cause) {
        super(message, "AUTHENTICATION_ERROR", errorData, cause);
    }
}