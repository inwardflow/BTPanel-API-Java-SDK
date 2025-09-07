package net.heimeng.sdk.btapi.v2.exception;

/**
 * 宝塔API异常类，用于表示调用宝塔API时发生的错误
 * <p>
 * 该异常类继承自BtSdkException，用于封装宝塔API调用过程中可能出现的各种错误情况，
 * 包括网络错误、认证失败、参数错误、API返回错误等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class BtApiException extends BtSdkException {
    
    /**
     * HTTP状态码
     */
    private final Integer statusCode;
    
    /**
     * 响应体
     */
    private final String responseBody;
    
    /**
     * 构造函数，创建一个新的BtApiException实例
     * 
     * @param message 错误消息
     */
    public BtApiException(String message) {
        super(message);
        this.statusCode = null;
        this.responseBody = null;
    }
    
    /**
     * 构造函数，创建一个新的BtApiException实例
     * 
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BtApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
        this.responseBody = null;
    }
    
    /**
     * 构造函数，创建一个新的BtApiException实例
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     */
    public BtApiException(String message, String errorCode) {
        super(message, errorCode);
        this.statusCode = null;
        this.responseBody = null;
    }
    
    /**
     * 构造函数，创建一个新的BtApiException实例
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param cause 原始异常
     */
    public BtApiException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
        this.statusCode = null;
        this.responseBody = null;
    }
    
    /**
     * 构造函数，创建一个新的BtApiException实例
     * 
     * @param message 错误消息
     * @param statusCode HTTP状态码
     * @param responseBody 响应体
     */
    public BtApiException(String message, int statusCode, String responseBody) {
        super(message, String.valueOf(statusCode));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
    
    /**
     * 完整的构造函数，创建一个新的BtApiException实例
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorData 错误数据
     * @param cause 原始异常
     */
    public BtApiException(String message, String errorCode, Object errorData, Throwable cause) {
        super(message, errorCode, errorData, cause);
        this.statusCode = null;
        this.responseBody = null;
    }
    
    /**
     * 获取HTTP状态码
     * 
     * @return HTTP状态码，如果没有设置则返回null
     */
    public Integer getStatusCode() {
        return statusCode;
    }
    
    /**
     * 获取响应体
     * 
     * @return 响应体，如果没有设置则返回null
     */
    public String getResponseBody() {
        return responseBody;
    }
}