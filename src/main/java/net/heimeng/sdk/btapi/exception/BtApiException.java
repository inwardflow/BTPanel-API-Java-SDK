package net.heimeng.sdk.btapi.exception;

/**
 * 宝塔API异常类，用于表示调用宝塔API时发生的错误
 * 
 * <p>该异常类继承自RuntimeException，用于封装宝塔API调用过程中可能出现的各种错误情况，
 * 包括网络错误、认证失败、参数错误、API返回错误等。</p>
 * 
 * @author InwardFlow
 * @since 1.0.0
 */
public class BtApiException extends RuntimeException {
    
    /**
     * 错误代码，用于标识具体的错误类型
     */
    private final String errorCode;
    
    /**
     * 原始错误数据，可能包含API返回的错误详情
     */
    private final Object errorData;

    /**
     * 构造函数，创建一个新的BTApiException实例
     * 
     * @param message 错误消息
     */
    public BtApiException(String message) {
        this(message, null, null);
    }
    
    /**
     * 构造函数，创建一个新的BTApiException实例
     * 
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BtApiException(String message, Throwable cause) {
        this(message, cause, null);
    }
    
    /**
     * 构造函数，创建一个新的BTApiException实例
     * 
     * @param message 错误消息
     * @param cause 原始异常
     * @param errorCode 错误代码
     */
    public BtApiException(String message, Throwable cause, String errorCode) {
        this(message, cause, errorCode, null);
    }
    
    /**
     * 完整的构造函数，创建一个新的BTApiException实例
     * 
     * @param message 错误消息
     * @param cause 原始异常
     * @param errorCode 错误代码
     * @param errorData 错误数据
     */
    public BtApiException(String message, Throwable cause, String errorCode, Object errorData) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorData = errorData;
    }
    
    public BtApiException(String string, int statusCode, String responseBody) {
        super(string);
        this.errorCode = String.valueOf(statusCode);
        this.errorData = responseBody;
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BtApiException: ");
        sb.append(getMessage());
        
        if (hasErrorCode()) {
            sb.append(" [Error Code: ").append(errorCode).append("]");
        }
        
        return sb.toString();
    }

    /**
     * 检查是否为服务器错误
     * 
     * @return 如果是服务器错误则返回true，否则返回false
     */
    public boolean isServerError() {
        return hasErrorCode() && errorCode.startsWith("5");
    }

    /**
     * 检查是否为网络错误
     * 
     * @return 如果是网络错误则返回true，否则返回false
     */
    public boolean isNetworkError() {
        return hasErrorCode() && errorCode.startsWith("4");
    }
}