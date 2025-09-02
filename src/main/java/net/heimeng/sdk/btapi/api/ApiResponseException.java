package net.heimeng.sdk.btapi.api;

/**
 * API响应异常类，用于表示API调用返回的错误状态
 * 
 * <p>该异常类用于封装宝塔API调用返回的错误状态，包含状态码和错误消息。</p>
 * 
 * @author InwardFlow
 * @since 1.0.0
 */
public class ApiResponseException extends RuntimeException {
    private final boolean status;
    private final String msg;

    /**
     * 构造函数
     * 
     * @param status 响应状态
     * @param msg 错误消息
     */
    public ApiResponseException(boolean status, String msg) {
        super(msg);
        this.status = status;
        this.msg = msg;
    }

    /**
     * 获取响应状态
     * 
     * @return 响应状态
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ApiResponseException{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                '}';
    }
}
