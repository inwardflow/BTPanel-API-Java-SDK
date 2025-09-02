package net.heimeng.sdk.btapi.api;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.heimeng.sdk.btapi.BtUtils;

import java.util.Map;

/**
 * 宝塔 API 抽象类
 *
 * @author InwardFlow
 */
@Slf4j
public abstract class BtApi {
    private String baseUrl;
    protected String apiPath;
    private String apiKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 请求 URL
     */
    protected String requestUrl;

    /**
     * 提交表单
     */
    protected Map<String, Object> formMap;

    public BtApi(String baseUrl, ApiEnum apiPath, String apiKey) {
        this(baseUrl, apiPath, apiKey, null);
    }

    public BtApi(String baseUrl, ApiEnum apiPath, String apiKey, Map<String, Object> formMap) {
        this.baseUrl = baseUrl;
        this.apiPath = apiPath.getValue();
        this.apiKey = apiKey;
        this.formMap = formMap;
    }

    protected abstract void beforeExecute();

    public abstract String execute();
    public abstract String execute(Map<String, Object> params);

    protected void updateRequestUrlWithSign() {
        requestUrl = baseUrl + apiPath;
        long requestTime = System.currentTimeMillis() / 1000;
        String requestToken = BtUtils.generateRequestToken(apiKey, requestTime);

        // 使用 Hutool 构建 URL 参数
        StringBuilder urlBuilder = new StringBuilder(requestUrl);
        String separator = requestUrl.contains("?") ? "&" : "?";
        urlBuilder.append(separator)
                .append("request_time=").append(requestTime)
                .append("&request_token=").append(URLEncodeUtil.encode(requestToken));

        requestUrl = urlBuilder.toString();
    }

    protected String defaultExecute() {
        updateRequestUrlWithSign();
        beforeExecute();
        try (HttpResponse response = HttpUtil.createPost(requestUrl)
                .form(formMap)
                .execute()) {
            if (response.isOk()) {
                String responseBody = response.body();
                if (JSONUtil.isTypeJSON(responseBody)) {
                    // 检查响应是否包含 status 字段
                    if (JSONUtil.parseObj(responseBody).containsKey("status")) {
                        boolean status = JSONUtil.parseObj(responseBody).getBool("status", true);
                        if (status) {
                            return responseBody;
                        } else {
                            String msg = JSONUtil.parseObj(responseBody).getStr("msg", "Unknown error");
                            log.error("HTTP BT API request failed with response: {}", UnicodeUtil.toString(responseBody));
                            throw new ApiResponseException(status, msg);
                        }
                    } else {
                        // 如果没有 status 字段，认为是成功响应
                        return responseBody;
                    }
                } else {
                    log.error("HTTP BT API request returned non-JSON response: {}", responseBody);
                    throw new ApiResponseException(false, "Invalid JSON response");
                }
            } else {
                log.error("HTTP BT API request failed with status code: {}, response: {}",
                        response.getStatus(), UnicodeUtil.toString(response.body()));
                throw new ApiResponseException(false, "HTTP request failed with status: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("Exception occurred while executing HTTP BT API request", e);
            throw new ApiResponseException(false, "Exception occurred while executing HTTP BT API request: " + e.getMessage());
        }
    }
}