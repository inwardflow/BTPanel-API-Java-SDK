package net.heimeng.sdk.btapi.api;

import cn.hutool.json.JSONUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 宝塔设置面板迁移API接口
 *
 * @author InwardFlow
 */
public class SiteMigrateSetPanelApiBtApi extends BtApi {

    public SiteMigrateSetPanelApiBtApi() {
        this(null, null);
    }

    public SiteMigrateSetPanelApiBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.SITE_MIGRATE_SET_PANEL_API, apiKey);
        this.formMap = new HashMap<>();
    }

    @Override
    protected void beforeExecute() {
        // 在执行请求之前可以进行一些准备工作
    }

    @Override
    public String execute() {
        return defaultExecute();
    }

    @Override
    public String execute(Map<String, Object> params) {
        this.formMap.put("api_info", JSONUtil.toJsonStr(params));
        return defaultExecute();
    }
}
