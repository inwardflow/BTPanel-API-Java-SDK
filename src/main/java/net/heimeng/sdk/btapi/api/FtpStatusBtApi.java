package net.heimeng.sdk.btapi.api;

import java.util.Map;

/**
 * 停用启用FTP的 API
 *
 * @author InwardFlow
 */
public class FtpStatusBtApi extends BtApi {

    public FtpStatusBtApi() {
        this(null, null);
    }

    public FtpStatusBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.BT_FTP_STATUS, apiKey);
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