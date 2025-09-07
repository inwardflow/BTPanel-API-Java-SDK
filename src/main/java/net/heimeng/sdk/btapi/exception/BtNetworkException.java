package net.heimeng.sdk.btapi.exception;

import java.net.URL;

/**
 * BtNetworkException表示网络相关的异常，如连接超时、无法连接等
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class BtNetworkException extends BtSdkException {

    /**
     * 目标URL
     */
    private final URL url;

    /**
     * 构造函数，创建一个新的BtNetworkException实例
     *
     * @param message 错误消息
     * @param url 目标URL
     */
    public BtNetworkException(String message, URL url) {
        super(message, "NETWORK_ERROR");
        this.url = url;
    }

    /**
     * 构造函数，创建一个新的BtNetworkException实例
     *
     * @param message 错误消息
     * @param url 目标URL
     * @param cause 原始异常
     */
    public BtNetworkException(String message, URL url, Throwable cause) {
        super(message, "NETWORK_ERROR", null, cause);
        this.url = url;
    }

    /**
     * 构造函数，创建一个新的BtNetworkException实例
     *
     * @param message 错误消息
     * @param url 目标URL
     * @param errorData 错误数据
     * @param cause 原始异常
     */
    public BtNetworkException(String message, URL url, Object errorData, Throwable cause) {
        super(message, "NETWORK_ERROR", errorData, cause);
        this.url = url;
    }

    /**
     * 获取目标URL
     *
     * @return 目标URL
     */
    public URL getUrl() {
        return url;
    }
}