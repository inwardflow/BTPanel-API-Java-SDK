package net.heimeng.sdk.btapi.v2.api;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 宝塔API的基础抽象类，提供通用的API实现功能
 * <p>
 * 该类实现了BtApi接口，并提供了一些通用的方法，用于简化具体API实现类的开发。
 * </p>
 *
 * @param <T> API返回数据类型
 * @author InwardFlow
 * @since 2.0.0
 */
public abstract class BaseBtApi<T> implements BtApi<T> {
    
    /**
     * API端点路径
     */
    @Getter
    protected final String endpoint;
    
    /**
     * HTTP请求方法
     */
    @Getter
    protected final HttpMethod method;
    
    /**
     * 请求参数
     */
    @Getter
    protected final Map<String, Object> params;
    
    /**
     * 构造函数，创建一个新的BaseBtApi实例
     * 
     * @param endpoint API端点路径
     * @param method HTTP请求方法
     */
    protected BaseBtApi(String endpoint, HttpMethod method) {
        this.endpoint = endpoint;
        this.method = method;
        this.params = new HashMap<>();
    }
    
    /**
     * 添加请求参数
     * 
     * @param key 参数名
     * @param value 参数值
     * @return 当前API实例，支持链式调用
     */
    public BaseBtApi<T> addParam(String key, Object value) {
        if (key != null && !key.isEmpty() && value != null) {
            this.params.put(key, value);
        }
        return this;
    }
    
    /**
     * 批量添加请求参数
     * 
     * @param params 参数映射
     * @return 当前API实例，支持链式调用
     */
    public BaseBtApi<T> addParams(Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            this.params.putAll(params);
        }
        return this;
    }
    
    /**
     * 移除请求参数
     * 
     * @param key 要移除的参数名
     * @return 当前API实例，支持链式调用
     */
    public BaseBtApi<T> removeParam(String key) {
        if (key != null) {
            this.params.remove(key);
        }
        return this;
    }
    
    /**
     * 清空所有请求参数
     * 
     * @return 当前API实例，支持链式调用
     */
    public BaseBtApi<T> clearParams() {
        this.params.clear();
        return this;
    }
    
    /**
     * 获取请求参数
     * 
     * @return 请求参数的不可变映射
     */
    @Override
    public Map<String, Object> getParams() {
        return new HashMap<>(params);
    }
    
    /**
     * 验证请求参数是否有效
     * 
     * @return 如果请求参数有效则返回true，否则返回false
     */
    protected boolean validateParams() {
        return true;
    }
    
    /**
     * 检查响应是否成功
     * 
     * @param response 响应字符串
     * @return 如果响应成功则返回true，否则返回false
     */
    protected boolean isSuccessResponse(String response) {
        try {
            if (response == null || response.isEmpty()) {
                return false;
            }
            
            JSONObject json = JSONUtil.parseObj(response);
            return json.getBool("status", false);
        } catch (Exception e) {
            return false;
        }
    }
}