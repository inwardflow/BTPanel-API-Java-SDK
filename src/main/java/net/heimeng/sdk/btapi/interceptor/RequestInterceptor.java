package net.heimeng.sdk.btapi.interceptor;

import net.heimeng.sdk.btapi.interceptor.RequestContext.Chain;

/**
 * 请求拦截器接口，用于在发送请求前或接收响应后执行自定义逻辑
 * <p>
 * 拦截器可以用于日志记录、请求修改、响应处理、重试逻辑等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@FunctionalInterface
public interface RequestInterceptor {
    
    /**
     * 拦截请求并执行自定义逻辑
     * 
     * @param context 请求上下文，包含请求和响应的相关信息
     * @param chain 拦截器链，用于继续请求流程
     * @throws Exception 当拦截器处理失败时抛出
     */
    void intercept(RequestContext context, Chain chain) throws Exception;
    
    /**
     * 获取拦截器的优先级
     * 
     * @return 优先级值，值越小优先级越高
     */
    default int getPriority() {
        return 0;
    }
}