package net.heimeng.sdk.btapi.v2.exception;

/**
 * 宝塔SDK的基础异常类，所有SDK相关的异常都应继承此类
 * <p>
 * 提供统一的异常处理机制，包含错误代码、错误数据等额外信息
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class BtSdkException extends RuntimeException {
    
    /**
     * 错误代码
     */
    private final String errorCode;
    
    /**
     * 错误数据，可为任意类型
     */
    private final Object errorData;
    
    /**
     * 构造函数，创建一个新的BtSdkException实例
     * 
     * @param message 错误消息
     */
    public BtSdkException(String message) {
        this(message, null, null, null);
    }
    
    /**
     * 构造函数，创建一个新的BtSdkException实例
     * 
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BtSdkException(String message, Throwable cause) {
        this(message, null, null, cause);
    }
    
    /**
     * 构造函数，创建一个新的BtSdkException实例
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     */
    public BtSdkException(String message, String errorCode) {
        this(message, errorCode, null, null);
    }
    
    /**
     * 构造函数，创建一个新的BtSdkException实例
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param cause 原始异常
     */
    public BtSdkException(String message, String errorCode, Throwable cause) {
        this(message, errorCode, null, cause);
    }
    
    /**
     * 完整的构造函数，创建一个新的BtSdkException实例
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorData 错误数据
     * @param cause 原始异常
     */
    public BtSdkException(String message, String errorCode, Object errorData, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorData = errorData;
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码，如果没有设置则返回null
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取错误数据
     * 
     * @return 错误数据，如果没有设置则返回null
     */
    public Object getErrorData() {
        return errorData;
    }
    
    /**
     * 检查是否包含错误代码
     * 
     * @return 如果有错误代码则返回true，否则返回false
     */
    public boolean hasErrorCode() {
        return errorCode != null && !errorCode.isEmpty();
    }
    
    /**
     * 检查是否包含错误数据
     * 
     * @return 如果有错误数据则返回true，否则返回false
     */
    public boolean hasErrorData() {
        return errorData != null;
    }
}