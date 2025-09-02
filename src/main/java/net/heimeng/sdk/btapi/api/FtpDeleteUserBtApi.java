package net.heimeng.sdk.btapi.api;

import java.util.Map;

/**
 * 删除FTP用户的 API
 *
 * @author InwardFlow
 */
public class FtpDeleteUserBtApi extends BtApi {

    public FtpDeleteUserBtApi() {
        this(null, null);
    }

    public FtpDeleteUserBtApi(String baseUrl, String apiKey) {
        super(baseUrl, ApiEnum.BT_FTP_DELETE_USER, apiKey);
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