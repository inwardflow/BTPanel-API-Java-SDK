package net.heimeng.sdk.btapi.api;

import java.util.HashMap;
import java.util.Map;

/**
 * 宝塔网站迁移API接口
 *
 * @author InwardFlow
 */
public class SiteMigrateBtApi extends BtApi {
    private int sourceSiteId;
    private int targetSiteId;

    public SiteMigrateBtApi(String baseUrl, String apiKey, int sourceSiteId, int targetSiteId) {
        super(baseUrl, ApiEnum.SITE_MIGRATE, apiKey);
        this.sourceSiteId = sourceSiteId;
        this.targetSiteId = targetSiteId;
        this.formMap = new HashMap<>();
        this.formMap.put("source_id", sourceSiteId);
        this.formMap.put("target_id", targetSiteId);
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
