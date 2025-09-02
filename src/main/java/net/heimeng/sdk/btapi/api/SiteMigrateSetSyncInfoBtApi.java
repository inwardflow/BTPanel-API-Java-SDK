package net.heimeng.sdk.btapi.api;

import java.util.HashMap;
import java.util.Map;

/**
 * 宝塔设置同步信息API接口
 *
 * @author InwardFlow
 */
public class SiteMigrateSetSyncInfoBtApi extends BtApi {

    public SiteMigrateSetSyncInfoBtApi() {
        this(null, null);
    }

    public SiteMigrateSetSyncInfoBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.SITE_MIGRATE_SET_SYNC_INFO, apiKey);
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
        this.formMap.putAll(params);
        return defaultExecute();
    }
}
