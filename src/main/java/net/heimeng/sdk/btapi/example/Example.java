package net.heimeng.sdk.btapi.example;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.heimeng.sdk.btapi.BtSdk;
import net.heimeng.sdk.btapi.api.BtApiFactory;
import net.heimeng.sdk.btapi.config.BtSdkConfig;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.BtSite;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 宝塔面板API SDK使用示例类
 * 本类展示了SDK的最佳使用实践，包括初始化、API调用和异常处理
 *
 * @author InwardFlow
 */
@Slf4j
public class Example {

    /**
     * 主方法，包含多个功能示例
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 创建示例实例并运行所有示例
        Example example = new Example();
        
        try {
            // 示例1: 使用默认配置初始化SDK并获取系统总览信息
            example.getSystemTotalExample();
            
            // 示例2: 获取网站列表
            example.getSiteListExample();
            
            // 示例3: 文件操作 - 创建目录
            example.createDirectoryExample();
            
            // 示例4: 系统操作 - 释放内存
            example.releaseMemoryExample();
            
            // 示例5: FTP管理 - 添加FTP用户
            example.addFtpUserExample();
            
            // 示例6: 自定义API调用
            example.customApiCallExample();
            
            // 示例7: 使用自定义配置初始化SDK
            example.customConfigExample();
            
        } catch (Exception e) {
            log.error("示例执行过程中发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 初始化BtSdk实例
     * 建议在实际应用中使用配置文件或环境变量来存储这些敏感信息
     *
     * @return 初始化好的BTSDK实例
     */
    private BtSdk initSDK() {
        // 这里使用示例值，实际使用时请替换为您的宝塔面板地址和API密钥
        String baseUrl = "http://your-bt-panel-url:8888";
        String apiKey = "your-actual-api-key";
        
        // 验证必要参数
        if (Objects.isNull(baseUrl) || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("宝塔面板地址不能为空");
        }
        if (Objects.isNull(apiKey) || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API密钥不能为空");
        }
        
        // 使用工厂方法获取SDK实例，而不是直接new
        BtSdk.initSdk(baseUrl, apiKey);
        return BtSdk.getInstance();
    }

    /**
     * 示例1: 获取系统总览信息
     */
    public void getSystemTotalExample() {
        System.out.println("\n===== 示例1: 获取系统总览信息 =====");
        
        try {
            BtSdk btSDK = initSDK();
            
            // 调用API获取系统总览信息
            String systemTotal = btSDK.getSystemTotal();
            
            // 打印结果
            if (Objects.nonNull(systemTotal) && !systemTotal.isEmpty()) {
                System.out.println("系统总览信息获取成功:");
                System.out.println(JSONUtil.toJsonPrettyStr(JSONUtil.parseObj(systemTotal)));
            } else {
                System.out.println("系统总览信息获取失败");
            }
        } catch (BtApiException e) {
            // 捕获并处理API特定异常
            log.error("获取系统总览信息时发生API错误 [错误码: {}]: {}", e.getErrorCode(), e.getMessage());
            System.err.println("获取系统总览信息时发生错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("获取系统总览信息时发生未知错误: {}", e.getMessage(), e);
            System.err.println("获取系统总览信息时发生未知错误: " + e.getMessage());
        }
    }

    /**
     * 示例2: 获取网站列表
     */
    public void getSiteListExample() {
        System.out.println("\n===== 示例2: 获取网站列表 =====");
        
        try {
            BtSdk btSDK = initSDK();
            
            // 调用API获取网站列表
            List<BtSite> siteList = btSDK.getSiteList();
            
            // 打印结果
            if (siteList != null && !siteList.isEmpty()) {
                System.out.println("网站列表获取成功，共 " + siteList.size() + " 个网站:");
                for (BtSite site : siteList) {
                    System.out.println("- " + site.getName() + " (ID: " + site.getId() + ")");
                }
            } else {
                System.out.println("未获取到网站列表或网站列表为空");
            }
        } catch (BtApiException e) {
            // 捕获并处理API特定异常
            log.error("获取网站列表时发生API错误 [错误码: {}]: {}", e.getErrorCode(), e.getMessage());
            System.err.println("获取网站列表时发生错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("获取网站列表时发生未知错误: {}", e.getMessage(), e);
            System.err.println("获取网站列表时发生未知错误: " + e.getMessage());
        }
    }

    /**
     * 示例3: 创建目录
     */
    public void createDirectoryExample() {
        System.out.println("\n===== 示例3: 创建目录 =====");
        
        try {
            BtSdk btSDK = initSDK();
            
            // 设置要创建的目录路径
            String dirPath = "/www/wwwroot/new_directory";
            
            // 调用API创建目录
            BtResult result = btSDK.createDir(dirPath);
            
            // 打印结果
            if (result != null && result.isSuccess()) {
                System.out.println("目录创建成功: " + dirPath);
                System.out.println("消息: " + result.getMsg());
            } else {
                System.out.println("目录创建失败: " + dirPath);
                if (result != null) {
                    System.out.println("失败原因: " + result.getMsg());
                }
            }
        } catch (BtApiException e) {
            // 捕获并处理API特定异常
            log.error("创建目录时发生API错误 [错误码: {}]: {}", e.getErrorCode(), e.getMessage());
            System.err.println("创建目录时发生错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("创建目录时发生未知错误: {}", e.getMessage(), e);
            System.err.println("创建目录时发生未知错误: " + e.getMessage());
        }
    }

    /**
     * 示例4: 释放内存
     */
    public void releaseMemoryExample() {
        System.out.println("\n===== 示例4: 释放内存 =====");
        
        try {
            BtSdk btSDK = initSDK();
            
            // 调用API释放内存
            BtResult result = btSDK.reMemory();
            
            // 打印结果
            if (result != null && result.isSuccess()) {
                System.out.println("内存释放成功");
                System.out.println("消息: " + result.getMsg());
            } else {
                System.out.println("内存释放失败");
                if (result != null) {
                    System.out.println("失败原因: " + result.getMsg());
                }
            }
        } catch (BtApiException e) {
            // 捕获并处理API特定异常
            log.error("释放内存时发生API错误 [错误码: {}]: {}", e.getErrorCode(), e.getMessage());
            System.err.println("释放内存时发生错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("释放内存时发生未知错误: {}", e.getMessage(), e);
            System.err.println("释放内存时发生未知错误: " + e.getMessage());
        }
    }

    /**
     * 示例5: 添加FTP用户
     */
    public void addFtpUserExample() {
        System.out.println("\n===== 示例5: 添加FTP用户 =====");
        
        try {
            BtSdk btSDK = initSDK();
            
            // 准备FTP用户参数
            Map<String, Object> ftpParams = new HashMap<>();
            ftpParams.put("name", "test_ftp_user");
            ftpParams.put("password", "StrongPass123!");
            ftpParams.put("path", "/www/wwwroot/ftp_home");
            ftpParams.put("ps", "测试FTP用户");
            ftpParams.put("readonly", 0); // 0表示可读写，1表示只读
            
            // 调用API添加FTP用户
            BtResult result = btSDK.addFtpUser(ftpParams);
            
            // 打印结果
            if (result != null && result.isSuccess()) {
                System.out.println("FTP用户添加成功: " + ftpParams.get("name"));
                System.out.println("消息: " + result.getMsg());
            } else {
                System.out.println("FTP用户添加失败: " + ftpParams.get("name"));
                if (result != null) {
                    System.out.println("失败原因: " + result.getMsg());
                }
            }
        } catch (BtApiException e) {
            // 捕获并处理API特定异常
            log.error("添加FTP用户时发生API错误 [错误码: {}]: {}", e.getErrorCode(), e.getMessage());
            System.err.println("添加FTP用户时发生错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("添加FTP用户时发生未知错误: {}", e.getMessage(), e);
            System.err.println("添加FTP用户时发生未知错误: " + e.getMessage());
        }
    }

    /**
     * 示例6: 自定义API调用
     */
    public void customApiCallExample() {
        System.out.println("\n===== 示例6: 自定义API调用 =====");
        
        try {
            BtSdk btSDK = initSDK();
            
            // 构建自定义参数
            Map<String, Object> customParams = new HashMap<>();
            customParams.put("id", 1); // 示例参数，根据实际API需要调整
            
            // 使用executeApi方法进行自定义API调用
            String result = btSDK.executeApi(BtApiFactory.GET_SITE_LIST, customParams);
            
            // 打印结果
            if (Objects.nonNull(result) && !result.isEmpty()) {
                System.out.println("自定义API调用成功:");
                System.out.println(JSONUtil.toJsonPrettyStr(JSONUtil.parseObj(result)));
            } else {
                System.out.println("自定义API调用失败");
            }
        } catch (BtApiException e) {
            // 捕获并处理API特定异常
            log.error("自定义API调用时发生API错误 [错误码: {}]: {}", e.getErrorCode(), e.getMessage());
            System.err.println("自定义API调用时发生错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("自定义API调用时发生未知错误: {}", e.getMessage(), e);
            System.err.println("自定义API调用时发生未知错误: " + e.getMessage());
        }
    }
    
    /**
     * 示例7: 使用自定义配置初始化SDK
     * 展示如何使用BTSDKConfig来自定义SDK的配置参数
     */
    public void customConfigExample() {
        System.out.println("\n===== 示例7: 使用自定义配置初始化SDK =====");
        
        try {
            // 这里使用示例值，实际使用时请替换为您的宝塔面板地址和API密钥
            String baseUrl = "http://your-bt-panel.com:8888";
            String apiKey = "your-api-key";
            
            // 创建自定义配置
            BtSdkConfig config = BtSdkConfig.builder()
                .connectTimeout(5) // 连接超时时间5秒
                .readTimeout(10)   // 读取超时时间10秒
                .retryCount(3)            // 失败重试3次
                .retryInterval(Duration.ofSeconds(1))    // 重试间隔1秒
                .enableSslVerify(false)   // 开发环境可关闭SSL验证
                .enableRequestLog(true)   // 开启请求日志
                .enableResponseLog(true)  // 开启响应日志
                .build();
            
            // 使用自定义配置初始化SDK
            BtSdk.initSdk(baseUrl, apiKey, config);
            BtSdk btSDK = BtSdk.getInstance();
            
            System.out.println("使用自定义配置初始化SDK成功");
            System.out.println("配置详情: ");
            System.out.println("- 连接超时: " + config.getConnectTimeout() + "秒");
            System.out.println("- 读取超时: " + config.getReadTimeout() + "秒");
            System.out.println("- 重试次数: " + config.getRetryCount());
            System.out.println("- SSL验证: " + config.isEnableSslVerify());
            System.out.println("- 请求日志: " + config.isEnableRequestLog());
            System.out.println("- 响应日志: " + config.isEnableResponseLog());
            
            // 使用自定义配置的SDK调用API示例
            String systemTotal = btSDK.getSystemTotal();
            if (Objects.nonNull(systemTotal) && !systemTotal.isEmpty()) {
                System.out.println("使用自定义配置成功获取系统总览信息");
            }
            
        } catch (BtApiException e) {
            // 捕获并处理API特定异常
            log.error("使用自定义配置初始化SDK时发生API错误 [错误码: {}]: {}", e.getErrorCode(), e.getMessage());
            System.err.println("使用自定义配置初始化SDK时发生错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("使用自定义配置初始化SDK时发生未知错误: {}", e.getMessage(), e);
            System.err.println("使用自定义配置初始化SDK时发生未知错误: " + e.getMessage());
        }
    }
}