package net.heimeng.sdk.btapi.api;

import java.util.Map;

/**
 * 获取端口转发列表的 API
 *
 * @author InwardFlow
 */
public class GetPortForwardsListBtApi extends BtApi {

    public GetPortForwardsListBtApi() {
        this(null, null);
    }

    public GetPortForwardsListBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.BT_GET_PORT_FORWARDS_LIST, apiKey);
    }

    @Override
    public void beforeExecute() {
        // 可以在这里添加执行前的逻辑
    }

    @Override
    public String execute() {
        // 执行 HTTP 请求
        return defaultExecute();
    }

    @Override
    public String execute(Map<String, Object> params) {
        this.formMap = params;
        return execute();
    }
}