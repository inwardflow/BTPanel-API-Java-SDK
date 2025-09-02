package net.heimeng.sdk.btapi.api.system;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 重启宝塔面板的API
 * <p>
 * 用于重启宝塔面板服务，使配置更改生效或解决面板运行问题。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class RestartPanelApi extends BaseBtApi<Boolean> {

    /**
     * 获取API端点路径
     *
     * @return API端点路径
     */
    @Override
    public String getEndpoint() {
        return "plugin?action=a&name=restart";
    }

    /**
     * 获取HTTP请求方法
     *
     * @return HTTP请求方法
     */
    @Override
    public HttpMethod getMethod() {
        return HttpMethod.POST;
    }

    /**
     * 解析API响应字符串，判断重启操作是否成功
     *
     * @param response API响应字符串
     * @return 如果重启成功则返回true，否则返回false
     */
    @Override
    public Boolean parseResponse(String response) {
        BtResult result = parseBasicResult(response);
        return result.isSuccess();
    }
}