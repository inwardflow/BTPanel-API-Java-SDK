package net.heimeng.sdk.btapi.model;

import java.util.Objects;

/**
 * 宝塔返回值格式
 *
 * @author InwardFlow
 */
public class BtResult {
    private final Boolean status;
    private final String msg;

    /**
     * 无参构造函数
     * <p>
     * 用于创建一个状态为false，消息为空的BtResult对象。
     * </p>
     */
    public BtResult() {
        this.status = false;
        this.msg = "";
    }

    /**
     * 构造宝塔返回值对象
     *
     * @param status 状态
     * @param msg    消息
     */
    public BtResult(Boolean status, String msg) {
        this.status = Objects.requireNonNullElse(status, false); // 使用 Objects.requireNonNullElse 处理 null 值
        this.msg = Objects.requireNonNullElse(msg, ""); // 使用 Objects.requireNonNullElse 处理 null 值
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public Boolean isSuccess() {
        return status;
    }

    /**
     * 获取消息
     *
     * @return 消息
     */
    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return String.format("BtResult{status=%s, msg='%s'}", status, msg); // 使用 String.format 提高性能
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BtResult btResult = (BtResult) o;

        if (!Objects.equals(status, btResult.status)) return false; // 使用 Objects.equals 处理 null 值
        return Objects.equals(msg, btResult.msg); // 使用 Objects.equals 处理 null 值
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, msg); // 使用 Objects.hash 简化 hashCode 实现
    }
}
