package net.heimeng.sdk.btapi.api;

import java.util.Map;

/**
 * 获取配置的 API
 *
 * @author InwardFlow
 */
public class GetConfigBtApi extends BtApi {

    public GetConfigBtApi() {
        this(null, null);
    }

    public GetConfigBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.BT_GET_CONFIG, apiKey);
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