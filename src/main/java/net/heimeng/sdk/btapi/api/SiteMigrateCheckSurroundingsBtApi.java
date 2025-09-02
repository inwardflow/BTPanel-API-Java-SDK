package net.heimeng.sdk.btapi.api;

import java.util.HashMap;
import java.util.Map;

/**
 * 宝塔检查周围环境API接口
 *
 * @author InwardFlow
 */
public class SiteMigrateCheckSurroundingsBtApi extends BtApi {

    public SiteMigrateCheckSurroundingsBtApi() {
        this(null, null);
    }

    public SiteMigrateCheckSurroundingsBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.SITE_MIGRATE_CHECK_SURROUNDINGS, apiKey);
        // TODO: 这里可能有问题
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
        // TODO: 增加参数校验，例如接收方安装的 PHP 版本必须包含发送方
        return result;
    }

    @Override
    public String execute(Map<String, Object> params) {
        this.formMap.putAll(params);
        return defaultExecute();
    }
}
