package net.heimeng.sdk.btapi.api;

import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.core.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 宝塔API的基础抽象实现
 * <p>
 * 提供通用的API实现逻辑，作为所有具体宝塔API实现的基类。
 * </p>
 *
 * @param <T> API返回数据类型
 * @author InwardFlow
 * @since 2.0.0
 */
public abstract class BaseBtApi<T> implements BtApi<T> {
    // 请求参数存储
    protected final Map<String, Object> params = new HashMap<>();

    /**
     * 获取请求参数
     *
     * @return 请求参数字典
     */
    @Override
    public Map<String, Object> getParams() {
        return new HashMap<>(params);
    }

    /**
     * 添加请求参数
     *
     * @param key 参数名
     * @param value 参数值
     * @return 当前API实例，支持链式调用
     */
    protected BaseBtApi<T> addParam(String key, Object value) {
        if (key != null && value != null) {
            params.put(key, value);
        }
        return this;
    }

    /**
     * 解析基本的BtResult响应
     *
     * @param response API响应字符串
     * @return 解析后的BtResult对象
     * @throws BtApiException 当解析失败或响应表示错误时抛出
     */
    protected BtResult parseBasicResult(String response) {
        try {
            BtResult result = JSONUtil.toBean(response, BtResult.class);
            if (!result.isSuccess()) {
                throw new BtApiException("API request failed: " + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            if (e instanceof BtApiException) {
                throw e;
            }
            throw new BtApiException("Failed to parse API response", e);
        }
    }

    /**
     * 解析JSON响应为指定类型的对象
     *
     * @param response API响应字符串
     * @param clazz 目标类型的Class对象
     * @param <R> 目标类型
     * @return 解析后的对象
     * @throws BtApiException 当解析失败时抛出
     */
    protected <R> R parseJsonResponse(String response, Class<R> clazz) {
        try {
            return JSONUtil.toBean(response, clazz);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse API response", e);
        }
    }
}