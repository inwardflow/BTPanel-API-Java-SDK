package net.heimeng.sdk.btapi.core;

/**
 * 拦截器接口，用于拦截和处理API请求和响应
 * <p>
 * 通过实现此接口，可以在发送请求前或接收响应后执行自定义逻辑，如日志记录、重试处理、请求修改等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public interface Interceptor {

    /**
     * 拦截请求并执行自定义逻辑
     * <p>
     * 在实现中，需要调用{@link RequestContext#proceed()}来继续请求处理流程。
     * 可以在调用proceed()前后添加自定义逻辑，实现请求前处理和响应后处理。
     * </p>
     *
     * @param context 请求上下文，包含请求和响应的相关信息
     */
    void intercept(RequestContext context);
}