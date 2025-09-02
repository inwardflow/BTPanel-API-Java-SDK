package net.heimeng.sdk.btapi.api;

import java.util.Map;

/**
 * 新建文件的 API
 *
 * @author InwardFlow
 */
public class FileCreateFileBtApi extends BtApi {

    public FileCreateFileBtApi() {
        this(null, null);
    }

    public FileCreateFileBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.BT_FILE_CREATE_FILE, apiKey);
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