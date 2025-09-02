package net.heimeng.sdk.btapi.api;

import java.util.HashMap;
import java.util.Map;

/**
 * 宝塔获取迁移速度API接口
 *
 * @author InwardFlow
 */
public class SiteMigrateGetSpeedBtApi extends BtApi {

    public SiteMigrateGetSpeedBtApi() {
        this(null, null);
    }

    public SiteMigrateGetSpeedBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.SITE_MIGRATE_GET_SPEED, apiKey);
        this.formMap = new HashMap<>();
    }

    @Override
    protected void beforeExecute() {
        // 在执行请求之前可以进行一些准备工作
    }

    @Override
    public String execute() {
        return validate(defaultExecute());
    }

    private String validate(String result) {
        // TODO: 增加参数校验，例如响应数据格式校验
        return result;
    }

    @Override
    public String execute(Map<String, Object> params) {
        this.formMap.putAll(params);
        return defaultExecute();
    }
}
