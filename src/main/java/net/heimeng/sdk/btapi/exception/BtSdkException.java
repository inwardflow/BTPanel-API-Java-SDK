package net.heimeng.sdk.btapi.exception;

/**
 * BtSdkException是宝塔SDK的根异常类，表示SDK中发生的所有异常都应该继承自此类
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public abstract class BtSdkException extends RuntimeException {

    /**
     * 错误代码，用于标识具体的错误类型
     */
    private final String errorCode;

    /**
     * 原始错误数据，可能包含详细的错误信息
     */
    private final Object errorData;

    /**
     * 构造函数，创建一个新的BtSdkException实例
     *
     * @param message 错误消息
     * @param errorCode 错误代码
     */
    protected BtSdkException(String message, String errorCode) {
        this(message, errorCode, null, null);
    }

    /**
     * 构造函数，创建一个新的BtSdkException实例
     *
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorData 错误数据
     */
    protected BtSdkException(String message, String errorCode, Object errorData) {
        this(message, errorCode, errorData, null);
    }

    /**
     * 完整的构造函数，创建一个新的BtSdkException实例
     *
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorData 错误数据
     * @param cause 原始异常
     */
    protected BtSdkException(String message, String errorCode, Object errorData, Throwable cause) {
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
}