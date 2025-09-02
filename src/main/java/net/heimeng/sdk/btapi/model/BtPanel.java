package net.heimeng.sdk.btapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.heimeng.sdk.btapi.api.ApiResponseException;
import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.api.BtApiFactory;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 宝塔面板 API 调用类，负责与宝塔面板进行实际的HTTP通信。
 * 
 * <p>该类封装了对宝塔API的底层调用逻辑，包括身份验证、参数处理和响应解析等功能。</p>
 *
 * @author InwardFlow
 * @since 1.0.0
 */
@Slf4j
@Data
@Builder
@AllArgsConstructor
public class BtPanel {

    /**
     * 面板 URL
     */
    private final String baseUrl;

    /**
     * 面板 API 密钥
     */
    private final String apiKey;
    
    /**
     * 面板 API 令牌
     */
    private final String apiToken;
    
    /**
     * SDK配置对象
     */
    private final BtSdkConfig config;
    
    /**
     * 构造函数，使用默认配置
     * 
     * @param baseUrl 面板URL
     * @param apiKey API密钥
     * @param apiToken API令牌
     */
    public BtPanel(String baseUrl, String apiKey, String apiToken) {
        this(baseUrl, apiKey, apiToken, BtSdkConfig.defaultConfig());
    }

    /**
     * 执行无参数的API调用
     * 
     * @param factory API工厂枚举
     * @return API响应结果
     * @throws BtApiException 当API调用失败时抛出
     */
    public String execute(BtApiFactory factory) {
        return execute(factory.getApi(), null);
    }

    /**
     * 执行带参数的API调用
     * 
     * @param factory API工厂枚举
     * @param params 请求参数
     * @return API响应结果
     * @throws BtApiException 当API调用失败时抛出
     */
    public String execute(BtApiFactory factory, Map<String, Object> params) {
        return execute(factory.getApi(), params);
    }

    /**
     * 执行自定义API调用
     * 
     * @param api API对象
     * @param params 请求参数
     * @return API响应结果
     * @throws BtApiException 当API调用失败时抛出
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    public String execute(BtApi api, Map<String, Object> params) {
        if (Objects.isNull(api)) {
            throw new IllegalArgumentException("API对象不能为空");
        }
        
        // 使用空映射避免空指针异常
        Map<String, Object> safeParams = Objects.requireNonNullElse(params, Collections.emptyMap());

        validateCredentials();

        api.setApiKey(apiKey);
        api.setBaseUrl(baseUrl);

        try {
            log.debug("Executing API call to: {}", apiPathMasked(api));
            String result = api.execute(safeParams);
            
            // 根据配置记录响应日志
            if (config.isEnableResponseLog()) {
                log.debug("API response: {}", result);
            }
            
            return result;
        } catch (ApiResponseException e) {
            // 转换API模块的异常为SDK统一异常
            log.error("API调用失败: {}", e.getMessage());
            throw new BtApiException(e.getMessage(), e, "API_ERROR", e);
        } catch (Exception e) {
            log.error("执行API调用时发生异常", e);
            throw new BtApiException("API调用失败: " + e.getMessage(), e, "SYSTEM_ERROR");
        }
    }

    /**
     * 验证凭证是否有效
     * 
     * @throws IllegalArgumentException 当凭证无效时抛出
     */
    private void validateCredentials() {
        if (Objects.isNull(baseUrl) || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("baseUrl不能为空或无效");
        }
        if (Objects.isNull(apiKey) || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("apiKey不能为空或无效");
        }
    }
    
    /**
     * 对API路径进行掩码处理，避免在日志中泄露敏感信息
     * 
     * @param api API对象
     * @return 掩码后的API路径信息
     */
    private String apiPathMasked(BtApi api) {
        String apiClass = api.getClass().getSimpleName();
        return apiClass.replaceAll("BTApi$", "");
    }
}
