package net.heimeng.sdk.btapi.model;

import lombok.Data;

/**
 * 宝塔API响应的基础结果类
 * <p>
 * 大多数宝塔API的响应都包含status、msg和data字段，此类用于封装这些通用字段。
 * </p>
 *
 * @param <T> data字段的类型
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class BtResult<T> {
    
    /**
     * 响应状态，true表示成功，false表示失败
     */
    private boolean status;
    
    /**
     * 响应消息，通常包含错误描述
     */
    private String msg;
    
    /**
     * 响应数据，具体类型根据API不同而不同
     */
    private T data;
    
    /**
     * 检查响应是否成功
     * 
     * @return 如果响应成功则返回true，否则返回false
     */
    public boolean isSuccess() {
        return status;
    }
    
    /**
     * 检查响应是否失败
     * 
     * @return 如果响应失败则返回true，否则返回false
     */
    public boolean isFailed() {
        return !status;
    }
}