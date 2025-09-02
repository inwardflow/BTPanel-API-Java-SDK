package net.heimeng.sdk.btapi.api;

import java.util.Map;

/**
 * 复制文件的 API
 *
 * @author InwardFlow
 */
public class FileCopyFileBtApi extends BtApi {

    public FileCopyFileBtApi() {
        this(null, null);
    }

    public FileCopyFileBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.BT_FILE_COPY_FILE, apiKey);
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