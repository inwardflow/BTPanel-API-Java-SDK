package net.heimeng.sdk.btapi.api;

import java.util.Map;

/**
 * 获取网站列表的 API
 *
 * @author InwardFlow
 */
public class GetSiteListBtApi extends BtApi {

    public GetSiteListBtApi() {
        this(null, null);
    }

    public GetSiteListBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.BT_GET_SITE_LIST, apiKey);
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
