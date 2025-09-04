package net.heimeng.sdk.btapi;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Getter;
import lombok.NonNull;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.heimeng.sdk.btapi.api.BtApiFactory;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.model.BtPanel;
import net.heimeng.sdk.btapi.model.BtSite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 宝塔SDK，封装了与宝塔面板API交互的方法，提供简单易用的接口来管理宝塔Linux面板。
 * <p>
 * 该类采用单例模式设计，确保在应用程序中只有一个BtSdk实例。
 * 使用前必须通过{@link #initSdk(String, String)}方法进行初始化。
 * </p>
 *
 * @author InwardFlow
 * @since 1.0.0
 */
public class BtSdk {
    private static final Logger log = LoggerFactory.getLogger(BtSdk.class);

    // 使用volatile确保多线程环境下的可见性
    private static volatile BtSdk instance;
    private final BtPanel btPanel;
    @Getter
    private final BtSdkConfig config;

    // 配置信息
    @Getter
    private final String baseUrl;

    @Getter
    private final String apiKey;

    /**
     * 构造函数，初始化BTPanel实例
     * 
     * @param baseUrl 宝塔面板地址
     * @param apiKey API密钥
     * @param config SDK配置
     * @throws IllegalArgumentException 当参数为空或格式不正确时抛出
     */
    private BtSdk(@NonNull String baseUrl, @NonNull String apiKey, @NonNull BtSdkConfig config) {
        // 参数验证
        validateBaseUrl(baseUrl);
        validateApiKey(apiKey);
        
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.config = config;
        this.btPanel = new BtPanel(baseUrl, apiKey, config);
        
        log.info("BtSdk initialized with base URL: {}", BtUtils.maskUrl(baseUrl));
    }
    
    /**
     * 构造函数，初始化BTPanel实例（兼容旧版API）
     * 
     * @param baseUrl 宝塔面板URL
     * @param apiKey 宝塔面板API密钥
     * @deprecated 使用{@link #initSDK(String, String, String)}方法替代
     */
    @Deprecated
    public BtSdk(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, BtSdkConfig.defaultConfig());
    }

    /**
     * 验证基础URL格式是否正确
     * 
     * @param baseUrl 要验证的URL
     * @throws IllegalArgumentException 当URL格式不正确时抛出
     */
    private void validateBaseUrl(String baseUrl) {
        if (!BtUtils.isValidUrl(baseUrl)) {
            throw new IllegalArgumentException("Base URL must be a valid URL starting with http:// or https://");
        }
        
        // 检查是否包含端口
        String urlWithoutProtocol = baseUrl.replaceFirst("^https?://", "");
        if (!urlWithoutProtocol.contains(":")) {
            log.warn("Base URL does not contain port, defaulting to 8888");
        }
    }
    
    /**
     * 验证API密钥是否有效
     * 
     * @param apiKey 要验证的API密钥
     * @throws IllegalArgumentException 当API密钥无效时抛出
     */
    private void validateApiKey(String apiKey) {
        if (apiKey.length() < 16) {
            log.warn("API Key seems unusually short. Please verify if it's correct.");
        }
    }

    /**
     * 初始化SDK
     * 
     * @param baseUrl 宝塔面板地址
     * @param apiKey API密钥
     * @return BtSdk实例
     * @throws IllegalArgumentException 当参数为空或无效时抛出
     */
    public static BtSdk initSdk(String baseUrl, String apiKey) {
        return initSdk(baseUrl, apiKey, BtSdkConfig.defaultConfig());
    }
    
    /**
     * 使用自定义配置初始化SDK
     * 
     * @param baseUrl 宝塔面板地址
     * @param apiKey API密钥
     * @param config 自定义SDK配置
     * @return BtSdk实例
     * @throws IllegalArgumentException 当参数为空或无效时抛出
     */
    public static BtSdk initSdk(String baseUrl, String apiKey, BtSdkConfig config) {
        Objects.requireNonNull(config, "Configuration cannot be null");
        
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key cannot be null or empty");
        }
        
        // 双重检查锁定模式
        if (instance == null) {
            synchronized (BtSdk.class) {
                if (instance == null) {
                    instance = new BtSdk(baseUrl, apiKey, config);
                }
            }
        } else {
            log.warn("BtSdk is already initialized. Returning existing instance.");
        }
        
        return instance;
    }

    /**
     * 获取SDK单例实例
     * 
     * @return BtSdk实例
     * @throws IllegalStateException 当SDK未初始化时抛出
     */
    public static BtSdk getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SDK not initialized. Call initSDK() first");
        }
        return instance;
    }

    /**
     * 获取系统总览信息
     *
     * @return 系统总览信息JSON字符串
     * @throws BtApiException 当API调用失败时抛出
     */
    public String getSystemTotal() {
        try {
            log.debug("Calling getSystemTotal API");
            return btPanel.execute(BtApiFactory.GET_SYSTEM_TOTAL);
        } catch (Exception e) {
            log.error("Failed to get system total information", e);
            throw new BtApiException("获取系统总览信息失败", e, "SYSTEM_INFO_ERROR");
        }
    }

    /**
     * 获取网站列表
     *
     * @return 网站列表（不可修改）
     * @throws BtApiException 当API调用失败时抛出
     */
    public List<BtSite> getSiteList() {
        try {
            log.debug("Calling getSiteList API");
            String response = btPanel.execute(BtApiFactory.GET_SITE_LIST);
            validateResponse(response, "getSiteList");
            
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray dataArray = jsonObject.getJSONArray("data");

            List<BtSite> siteList = new ArrayList<>();
            for (Object obj : dataArray) {
                BtSite site = JSONUtil.toBean((JSONObject) obj, BtSite.class);
                siteList.add(site);
            }
            return Collections.unmodifiableList(siteList);
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get site list", e);
            throw new BtApiException("获取网站列表失败", e, "SITE_LIST_ERROR");
        }
    }

    /**
     * 添加FTP用户
     *
     * @param params FTP用户参数，必须包含name、password和path字段
     * @return 操作结果
     * @throws BtApiException 当API调用失败时抛出
     * @throws IllegalArgumentException 当参数为空或缺少必要字段时抛出
     */
    public BtResult addFtpUser(Map<String, Object> params) {
        try {
            if (params == null || params.isEmpty()) {
                throw new IllegalArgumentException("FTP user parameters cannot be null or empty");
            }
            
            validateRequiredFtpParams(params);
            log.debug("Calling addFtpUser API");
            
            String response = btPanel.execute(BtApiFactory.ADD_FTP_USER, params);
            return parseBTResult(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameter: {}", e.getMessage());
            throw e;
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to add FTP user", e);
            throw new BtApiException("添加FTP用户失败", e, "FTP_ADD_ERROR");
        }
    }
    
    /**
     * 验证FTP用户参数是否包含必要字段
     * 
     * @param params FTP用户参数
     * @throws IllegalArgumentException 当必要参数缺失时抛出
     */
    private void validateRequiredFtpParams(Map<String, Object> params) {
        String[] requiredFields = {"name", "password", "path"};
        for (String field : requiredFields) {
            if (!params.containsKey(field) || params.get(field) == null || params.get(field).toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Required FTP parameter missing: " + field);
            }
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 操作结果
     * @throws BtApiException 当API调用失败时抛出
     * @throws IllegalArgumentException 当文件路径为空时抛出
     */
    public BtResult deleteFile(@NonNull String filePath) {
        try {
            if (filePath.trim().isEmpty()) {
                throw new IllegalArgumentException("File path cannot be empty");
            }
            
            log.debug("Calling deleteFile API with path: {}", filePath);
            
            Map<String, Object> params = Collections.singletonMap("path", filePath);
            String response = btPanel.execute(BtApiFactory.DELETE_FILE, params);
            return parseBTResult(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameter: {}", e.getMessage());
            throw e;
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete file", e);
            throw new BtApiException("删除文件失败", e, "FILE_DELETE_ERROR");
        }
    }

    /**
     * 创建目录
     *
     * @param dirPath 目录路径
     * @return 操作结果
     * @throws BtApiException 当API调用失败时抛出
     * @throws IllegalArgumentException 当目录路径为空时抛出
     */
    public BtResult createDir(String dirPath) {
        try {
            if (dirPath == null || dirPath.trim().isEmpty()) {
                throw new IllegalArgumentException("Directory path cannot be null or empty");
            }
            
            log.debug("Calling createDir API with path: {}", dirPath);
            
            Map<String, Object> params = Collections.singletonMap("path", dirPath);
            String response = btPanel.execute(BtApiFactory.FILE_CREATE_DIR, params);
            return parseBTResult(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameter: {}", e.getMessage());
            throw e;
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create directory", e);
            throw new BtApiException("创建目录失败", e, "DIR_CREATE_ERROR");
        }
    }

    /**
     * 释放内存
     *
     * @return 操作结果
     * @throws BtApiException 当API调用失败时抛出
     */
    public BtResult reMemory() {
        try {
            log.debug("Calling reMemory API");
            String response = btPanel.execute(BtApiFactory.RE_MEMORY);
            return parseBTResult(response);
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to release memory", e);
            throw new BtApiException("释放内存失败", e, "MEMORY_RELEASE_ERROR");
        }
    }

    /**
     * 执行自定义API
     *
     * @param factory API工厂枚举
     * @param params  请求参数
     * @return 响应结果
     * @throws BtApiException 当API调用失败时抛出
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    public String executeApi(BtApiFactory factory, Map<String, Object> params) {
        try {
            if (factory == null) {
                throw new IllegalArgumentException("API factory cannot be null");
            }
            
            log.debug("Calling custom API: {}", factory.name());
            
            Map<String, Object> safeParams = Optional.ofNullable(params)
                    .orElseGet(Collections::emptyMap);
            
            return btPanel.execute(factory, safeParams);
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameter: {}", e.getMessage());
            throw e;
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to execute API: {}", factory, e);
            throw new BtApiException("执行API失败", e, "API_EXECUTION_ERROR");
        }
    }

    /**
     * 解析宝塔返回结果
     *
     * @param response 响应字符串
     * @return BTResult对象
     * @throws BtApiException 当解析失败时抛出
     */
    private BtResult parseBTResult(String response) {
        validateResponse(response, "parseBTResult");

        try {
            JSONObject jsonObject = JSONUtil.parseObj(response);
            boolean status = jsonObject.getBool("status", false);
            String msg = jsonObject.getStr("msg", "");
            return new BtResult(status, msg);
        } catch (Exception e) {
            log.error("Failed to parse response: {}", response, e);
            throw new BtApiException("解析响应失败", e, "RESPONSE_PARSE_ERROR");
        }
    }
    
    /**
     * 验证响应是否有效
     * 
     * @param response 响应字符串
     * @param apiName API名称
     * @throws BtApiException 当响应无效时抛出
     */
    private void validateResponse(String response, String apiName) {
        if (Objects.isNull(response) || response.isEmpty()) {
            throw new BtApiException("API返回空响应: " + apiName, null, "EMPTY_RESPONSE");
        }
    }
    
    /**
     * 重置SDK实例（主要用于测试）
     */
    public static void reset() {
        instance = null;
    }
}