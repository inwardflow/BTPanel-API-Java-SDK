package net.heimeng.sdk.btapi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 宝塔SDK工具类，提供各种辅助方法
 * 
 * <p>该类包含了生成令牌、处理URL、格式化数据等多种工具方法，
 * 为SDK的其他组件提供基础支持。</p>
 *
 * @author InwardFlow
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BtUtils {
    
    // URL验证正则表达式，支持localhost
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}(\\.[a-zA-Z0-9()]{1,6})?\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$"
    );
    
    // 日期格式化器
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    );
    
    /**
     * 生成当前时间的时间戳（毫秒）
     * 
     * @return 当前时间的时间戳
     */
    public static long generateRequestTime() {
        return System.currentTimeMillis();
    }

    /**
     * 根据apiKey和requestTime生成requestToken
     * 
     * @param apiKey API密钥
     * @param requestTime 请求时间戳（毫秒）
     * @return 生成的requestToken
     * @throws IllegalArgumentException 当apiKey为空时抛出
     */
    public static String generateRequestToken(String apiKey, long requestTime) {
        Objects.requireNonNull(apiKey, "API key cannot be null");
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String innerHash = bytesToHex(md.digest(apiKey.getBytes()));
            String combined = requestTime + innerHash;
            return bytesToHex(md.digest(combined.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate request token", e);
            throw new IllegalStateException("MD5 algorithm not found", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        Objects.requireNonNull(bytes, "Byte array cannot be null");
        
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 生成宝塔面板基础URL
     * 
     * @param host 主机地址
     * @param port 端口号
     * @param isHttps 是否使用HTTPS
     * @return 格式化后的URL
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    public static String generateBaseUrl(String host, int port, boolean isHttps) {
        Objects.requireNonNull(host, "Host cannot be null");
        
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port number: " + port);
        }
        
        if (host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be empty");
        }
        
        String protocol = isHttps ? "https://" : "http://";
        return protocol + host + ":" + port;
    }
    
    /**
     * 生成宝塔面板基础URL（兼容字符串参数的isHttps）
     * 
     * @param host 主机地址
     * @param port 端口号
     * @param isHttps 是否使用HTTPS（字符串形式）
     * @return 格式化后的URL
     */
    public static String generateBaseUrl(String host, int port, String isHttps) {
        boolean useHttps = "1".equals(isHttps) || Boolean.parseBoolean(isHttps);
        return generateBaseUrl(host, port, useHttps);
    }
    
    /**
     * 验证URL格式是否正确
     * 
     * @param url 要验证的URL
     * @return 如果URL格式正确则返回true
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }
    
    /**
     * 格式化日期为字符串
     * 
     * @param date 日期对象
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date) {
        Objects.requireNonNull(date, "Date cannot be null");
        return DATE_FORMAT.get().format(date);
    }
    
    /**
     * 对敏感信息进行掩码处理
     * 
     * @param sensitiveInfo 敏感信息
     * @param showLength 前面保留的字符长度
     * @return 掩码后的信息
     */
    public static String maskSensitiveInfo(String sensitiveInfo, int showLength) {
        if (sensitiveInfo == null || sensitiveInfo.isEmpty()) {
            return sensitiveInfo;
        }
        
        if (sensitiveInfo.length() <= showLength) {
            return sensitiveInfo.substring(0, 1) + "****";
        }
        
        StringBuilder masked = new StringBuilder(sensitiveInfo.substring(0, showLength));
        for (int i = 0; i < Math.min(8, sensitiveInfo.length() - showLength); i++) {
            masked.append("*");
        }
        
        return masked.toString();
    }
    
    /**
     * 对URL进行掩码处理，隐藏密码等敏感信息
     * 
     * @param url 原始URL
     * @return 掩码后的URL
     */
    public static String maskUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        // 掩码URL中的凭证信息
        return url.replaceAll("://[^:]+:[^@]+@", "://***:***@")
                 .replaceAll("api[_-]?key=[^&]+", "api_key=***")
                 .replaceAll("token=[^&]+", "token=***");
    }
    
    /**
     * 将字符串转换为Base64编码
     * 
     * @param input 输入字符串
     * @return Base64编码的字符串
     */
    public static String toBase64(String input) {
        Objects.requireNonNull(input, "Input cannot be null");
        return Base64.getEncoder().encodeToString(input.getBytes());
    }
    
    /**
     * 从Base64编码解码字符串
     * 
     * @param base64 Base64编码的字符串
     * @return 解码后的字符串
     */
    public static String fromBase64(String base64) {
        Objects.requireNonNull(base64, "Base64 string cannot be null");
        return new String(Base64.getDecoder().decode(base64));
    }
    
    /**
     * 安全地关闭资源
     * 
     * @param closeable 可关闭的资源
     */
    public static void safeClose(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.debug("Failed to close resource", e);
            }
        }
    }
}
