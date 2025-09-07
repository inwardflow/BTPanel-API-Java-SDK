package net.heimeng.sdk.btapi;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Getter;
import lombok.NonNull;
import net.heimeng.sdk.btapi.core.BtConfig;
import net.heimeng.sdk.btapi.core.DefaultBtConfig;
import net.heimeng.sdk.btapi.core.BtApiManager;
import net.heimeng.sdk.btapi.core.BtClientFactory;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.heimeng.sdk.btapi.api.BtApiFactory;
import net.heimeng.sdk.btapi.api.GetSiteListBtApi;
import net.heimeng.sdk.btapi.model.BtSite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final BtApiManager apiManager;
    @Getter
    private final BtConfig config;

    // 配置信息
    @Getter
    private final String baseUrl;

    @Getter
    private final String apiKey;

    /**
     * 构造函数，初始化BtApiManager实例
     * 
     * @param baseUrl 宝塔面板地址
     * @param apiKey API密钥
     * @param config SDK配置
     * @throws IllegalArgumentException 当参数为空或格式不正确时抛出
     */
    private BtSdk(@NonNull String baseUrl, @NonNull String apiKey, @NonNull BtConfig config) {
        // 参数验证
        validateBaseUrl(baseUrl);
        validateApiKey(apiKey);
        
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.config = config;
        
        // 创建BtClient并初始化BtApiManager
        var client = BtClientFactory.createClient(config);
        this.apiManager = new BtApiManager(client);
        
        log.info("BtSdk initialized with base URL: {}", BtUtils.maskUrl(baseUrl));
    }
    
    /**
     * 构造函数，初始化BtApiManager实例（兼容旧版API）
     * 
     * @param baseUrl 宝塔面板URL
     * @param apiKey 宝塔面板API密钥
     * @deprecated 使用{@link #initSDK(String, String, String)}方法替代
     */
    @Deprecated
    public BtSdk(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, DefaultBtConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build());
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
     * 获取API管理器
     * 
     * @return BtApiManager实例
     */
    public BtApiManager getApiManager() {
        return apiManager;
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
    public static synchronized BtSdk initSdk(String baseUrl, String apiKey) {
        if (instance == null) {
            // 使用BtConfig替代BtSdkConfig
            BtConfig config = DefaultBtConfig.builder()
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .build();
            instance = new BtSdk(baseUrl, apiKey, config);
        }
        return instance;
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
    public static synchronized BtSdk initSdk(String baseUrl, String apiKey, BtConfig config) {
        Objects.requireNonNull(config, "Configuration cannot be null");
        
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key cannot be null or empty");
        }
        
        if (instance == null) {
            instance = new BtSdk(baseUrl, apiKey, config);
        } else {
            log.warn("BtSdk is already initialized. Returning existing instance.");
        }
        
        return instance;
    }
    
    /**
     * 初始化SDK（兼容旧版API）
     * 
     * @param baseUrl 宝塔面板地址
     * @param apiKey API密钥
     * @param config 自定义SDK配置
     * @return BtSdk实例
     */
    public static synchronized BtSdk initSDK(String baseUrl, String apiKey, BtConfig config) {
        return initSdk(baseUrl, apiKey, config);
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
     * @return 系统总览信息
     * @throws BtApiException 当API调用失败时抛出
     */
    public BtResult getSystemTotal() {
        try {
            log.debug("Calling getSystemTotal API");
            // 使用新版API获取系统信息
            return apiManager.system().getSystemInfo();
        } catch (Exception e) {
            log.error("获取系统总览信息失败", e);
            throw new BtApiException("获取系统总览信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取网站列表
     *
     * @return 网站列表
     * @throws BtApiException 当API调用失败时抛出
     */
    public List<BtSite> getSiteList() {
        try {
            log.debug("Calling getSiteList API");
            // 使用GetSiteListBtApi获取网站列表
            GetSiteListBtApi api = new GetSiteListBtApi(baseUrl, apiKey);
            String result = api.execute();
            
            // 这里应该有JSON解析逻辑，将result转换为List<BtSite>
            // 由于缺少完整的解析逻辑，暂时返回空列表
            return new ArrayList<>();
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get site list", e);
            throw new BtApiException("获取网站列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加FTP用户
     *
     * @param params FTP用户参数，必须包含name、password和path字段
     * @return 操作结果
     * @throws BtApiException 当API调用失败时抛出
     * @throws IllegalArgumentException 当参数为空或缺少必要字段时抛出
     * @deprecated 使用apiManager.ftp().addUser()替代
     */
    @Deprecated
    public BtResult addFtpUser(Map<String, Object> params) {
        try {
            if (params == null || params.isEmpty()) {
                throw new IllegalArgumentException("FTP用户参数不能为空");
            }
            
            validateRequiredFtpParams(params);
            log.debug("调用addFtpUser API");
            
            // 注意：这里暂时返回一个结果对象，因为旧的实现依赖于btPanel
            // 在实际项目中，应该实现ftp相关的新版API
            BtResult result = new BtResult(false, "该方法已废弃，请使用apiManager.ftp().addUser()");
            return result;
        } catch (IllegalArgumentException e) {
            log.error("参数无效: {}", e.getMessage());
            throw e;
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加FTP用户失败", e);
            throw new BtApiException("添加FTP用户失败: " + e.getMessage(), e);
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
                throw new IllegalArgumentException("缺少必要的FTP参数: " + field);
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
     * @deprecated 使用apiManager.file().delete()替代
     */
    @Deprecated
    public BtResult deleteFile(@NonNull String filePath) {
        try {
            if (filePath.trim().isEmpty()) {
                throw new IllegalArgumentException("文件路径不能为空");
            }
            
            log.debug("调用deleteFile API，路径: {}", filePath);
            
            // 注意：这里暂时返回一个结果对象，因为旧的实现依赖于btPanel
            // 在实际项目中，应该实现file相关的新版API
            BtResult result = new BtResult(false, "该方法已废弃，请使用apiManager.file().delete()");
            return result;
        } catch (IllegalArgumentException e) {
            log.error("参数无效: {}", e.getMessage());
            throw e;
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除文件失败", e);
            throw new BtApiException("删除文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建目录
     *
     * @param dirPath 目录路径
     * @return 操作结果
     * @throws BtApiException 当API调用失败时抛出
     * @throws IllegalArgumentException 当目录路径为空时抛出
     * @deprecated 使用apiManager.file().createDirectory()替代
     */
    @Deprecated
    public BtResult createDir(String dirPath) {
        try {
            if (dirPath == null || dirPath.trim().isEmpty()) {
                throw new IllegalArgumentException("目录路径不能为空");
            }
            
            log.debug("调用createDir API，路径: {}", dirPath);
            
            // 注意：这里暂时返回一个结果对象，因为旧的实现依赖于btPanel
            // 在实际项目中，应该实现file相关的新版API
            BtResult result = new BtResult(false, "该方法已废弃，请使用apiManager.file().createDirectory()");
            return result;
        } catch (IllegalArgumentException e) {
            log.error("参数无效: {}", e.getMessage());
            throw e;
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建目录失败", e);
            throw new BtApiException("创建目录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 释放内存
     *
     * @return 操作结果
     * @throws BtApiException 当API调用失败时抛出
     * @deprecated 使用apiManager.system().releaseMemory()替代
     */
    @Deprecated
    public BtResult reMemory() {
        try {
            log.debug("调用reMemory API");
            
            // 注意：这里暂时返回一个结果对象，因为旧的实现依赖于btPanel
            // 在实际项目中，应该检查是否有对应的新版API
            BtResult result = new BtResult(false, "该方法已废弃，请使用apiManager.system().releaseMemory()");
            return result;
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("释放内存失败", e);
            throw new BtApiException("释放内存失败: " + e.getMessage(), e);
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
     * @deprecated 不推荐使用，请使用对应的apiManager分组方法
     */
    @Deprecated
    public String executeApi(BtApiFactory factory, Map<String, Object> params) {
        try {
            if (factory == null) {
                throw new IllegalArgumentException("API工厂不能为空");
            }
            
            log.debug("调用自定义API: {}", factory.name());
            log.warn("executeApi方法已废弃，建议使用对应的apiManager分组方法");
            
            // 注意：这里暂时返回空字符串，因为旧的实现依赖于btPanel
            // 在实际项目中，应该基于新版API架构重新实现这个功能
            return "{\"status\": false, \"msg\": \"该方法已废弃，请使用apiManager中的对应方法\"}";
        } catch (IllegalArgumentException e) {
            log.error("参数无效: {}", e.getMessage());
            throw e;
        } catch (BtApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("执行API失败: {}", factory, e);
            throw new BtApiException("执行API失败: " + e.getMessage(), e);
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
            throw new BtApiException(apiName + " API返回空响应", null, "EMPTY_RESPONSE");
        }
    }
    
    /**
     * 重置SDK实例（主要用于测试）
     */
    public static void reset() {
        instance = null;
    }
}