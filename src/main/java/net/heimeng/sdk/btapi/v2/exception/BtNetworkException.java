package net.heimeng.sdk.btapi.v2.exception;

/**
 * 网络异常类，用于表示网络连接相关的错误
 * <p>
 * 该异常类继承自BtApiException，专门用于处理网络连接失败、超时等网络相关的错误情况。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class BtNetworkException extends BtApiException {
    
    /**
     * 目标主机
     */
    private final String host;
    
    /**
     * 目标端口
     */
    private final Integer port;
    
    /**
     * 构造函数，创建一个新的BtNetworkException实例
     * 
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BtNetworkException(String message, Throwable cause) {
        super(message, cause);
        this.host = null;
        this.port = null;
    }
    
    /**
     * 构造函数，创建一个新的BtNetworkException实例
     * 
     * @param message 错误消息
     * @param host 目标主机
     * @param cause 原始异常
     */
    public BtNetworkException(String message, String host, Throwable cause) {
        super(message, cause);
        this.host = host;
        this.port = null;
    }
    
    /**
     * 构造函数，创建一个新的BtNetworkException实例
     * 
     * @param message 错误消息
     * @param host 目标主机
     * @param port 目标端口
     * @param cause 原始异常
     */
    public BtNetworkException(String message, String host, int port, Throwable cause) {
        super(message, cause);
        this.host = host;
        this.port = port;
    }
    
    /**
     * 获取目标主机
     * 
     * @return 目标主机，如果没有设置则返回null
     */
    public String getHost() {
        return host;
    }
    
    /**
     * 获取目标端口
     * 
     * @return 目标端口，如果没有设置则返回null
     */
    public Integer getPort() {
        return port;
    }
}