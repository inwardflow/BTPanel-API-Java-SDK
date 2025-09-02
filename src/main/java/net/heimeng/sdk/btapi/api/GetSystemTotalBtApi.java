package net.heimeng.sdk.btapi.api;

import java.util.Map;

/**
 * 获取系统总览信息的 API
 *
 * @author InwardFlow
 */
public class GetSystemTotalBtApi extends BtApi {

    public GetSystemTotalBtApi() {
        this(null, null);
    }

    public GetSystemTotalBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.BT_GET_SYSTEM_TOTAL, apiKey);
    }

    @Override
    public void beforeExecute() {
        // 可以在这里添加执行后的逻辑
    }

    @Override
    public String execute() {
        // 执行 HTTP 请求
        return defaultExecute();
    }

    @Override
    public String execute(Map<String, Object> params) {
        return execute();
    }
}
