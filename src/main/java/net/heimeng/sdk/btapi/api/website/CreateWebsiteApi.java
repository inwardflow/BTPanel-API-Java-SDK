package net.heimeng.sdk.btapi.api.website;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.CreateWebsiteResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建网站API实现
 * <p>
 * 用于在宝塔面板中创建新的网站，支持配置域名、路径、FTP、数据库等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class CreateWebsiteApi extends BaseBtApi<BtResult<CreateWebsiteResult>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "site?action=AddSite";
    
    /**
     * 构造函数，创建一个新的CreateWebsiteApi实例
     * 
     * @param domain 网站主域名
     * @param path 网站根目录
     * @param typeId 分类标识
     * @param phpVersion PHP版本
     * @param port 网站端口
     * @param ps 网站备注
     */
    public CreateWebsiteApi(String domain, String path, int typeId, String phpVersion, int port, String ps) {
        super(ENDPOINT, HttpMethod.POST);
        
        // 设置必需参数
        setDomain(domain);
        setPath(path);
        setTypeId(typeId);
        setPhpVersion(phpVersion);
        setPort(port);
        setPs(ps);
        
        // 设置默认值
        setType("PHP");
        setCreateFtp(false);
        setCreateDatabase(false);
    }
    
    /**
     * 设置网站主域名
     * 
     * @param domain 网站主域名
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setDomain(String domain) {
        if (StrUtil.isEmpty(domain)) {
            throw new IllegalArgumentException("Domain cannot be empty");
        }
        
        // 构建域名JSON对象
        Map<String, Object> webnameMap = new HashMap<>();
        webnameMap.put("domain", domain);
        webnameMap.put("domainlist", new ArrayList<>());
        webnameMap.put("count", 0);
        
        addParam("webname", JSONUtil.toJsonStr(webnameMap));
        return this;
    }
    
    /**
     * 设置网站根目录
     * 
     * @param path 网站根目录
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setPath(String path) {
        if (StrUtil.isEmpty(path)) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        addParam("path", path);
        return this;
    }
    
    /**
     * 设置分类标识
     * 
     * @param typeId 分类标识
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setTypeId(int typeId) {
        addParam("type_id", typeId);
        return this;
    }
    
    /**
     * 设置项目类型
     * 
     * @param type 项目类型
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setType(String type) {
        addParam("type", type);
        return this;
    }
    
    /**
     * 设置PHP版本
     * 
     * @param phpVersion PHP版本
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setPhpVersion(String phpVersion) {
        if (StrUtil.isEmpty(phpVersion)) {
            throw new IllegalArgumentException("PHP version cannot be empty");
        }
        addParam("version", phpVersion);
        return this;
    }
    
    /**
     * 设置网站端口
     * 
     * @param port 网站端口
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setPort(int port) {
        addParam("port", port);
        return this;
    }
    
    /**
     * 设置网站备注
     * 
     * @param ps 网站备注
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setPs(String ps) {
        if (StrUtil.isEmpty(ps)) {
            throw new IllegalArgumentException("PS cannot be empty");
        }
        addParam("ps", ps);
        return this;
    }
    
    /**
     * 设置是否创建FTP
     * 
     * @param createFtp 是否创建FTP
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setCreateFtp(boolean createFtp) {
        addParam("ftp", createFtp);
        return this;
    }
    
    /**
     * 设置FTP用户名和密码（当创建FTP时必需）
     * 
     * @param ftpUsername FTP用户名
     * @param ftpPassword FTP密码
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setFtpCredentials(String ftpUsername, String ftpPassword) {
        if (StrUtil.isEmpty(ftpUsername) || StrUtil.isEmpty(ftpPassword)) {
            throw new IllegalArgumentException("FTP username and password cannot be empty");
        }
        addParam("ftp_username", ftpUsername);
        addParam("ftp_password", ftpPassword);
        setCreateFtp(true);
        return this;
    }
    
    /**
     * 设置是否创建数据库
     * 
     * @param createDatabase 是否创建数据库
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setCreateDatabase(boolean createDatabase) {
        addParam("sql", createDatabase);
        return this;
    }
    
    /**
     * 设置数据库信息（当创建数据库时必需）
     * 
     * @param databaseUser 数据库用户名
     * @param databasePassword 数据库密码
     * @param charset 数据库字符集
     * @return 当前API实例，支持链式调用
     */
    public CreateWebsiteApi setDatabaseCredentials(String databaseUser, String databasePassword, String charset) {
        if (StrUtil.isEmpty(databaseUser) || StrUtil.isEmpty(databasePassword) || StrUtil.isEmpty(charset)) {
            throw new IllegalArgumentException("Database user, password and charset cannot be empty");
        }
        addParam("codeing", charset);
        addParam("datauser", databaseUser);
        addParam("datapassword", databasePassword);
        setCreateDatabase(true);
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<CreateWebsiteResult>对象
     * 
     * @param response API响应字符串
     * @return BtResult<CreateWebsiteResult>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<CreateWebsiteResult> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            JSONObject json = JSONUtil.parseObj(response);
            BtResult<CreateWebsiteResult> result = new BtResult<>();
            
            // 检查响应状态
            boolean siteStatus = json.getBool("siteStatus", false);
            result.setStatus(siteStatus);
            
            // 解析创建网站结果
            if (siteStatus) {
                CreateWebsiteResult createResult = new CreateWebsiteResult();
                createResult.setSiteStatus(siteStatus);
                createResult.setFtpStatus(json.getBool("ftpStatus", false));
                createResult.setFtpUser(json.getStr("ftpUser", ""));
                createResult.setFtpPass(json.getStr("ftpPass", ""));
                createResult.setDatabaseStatus(json.getBool("databaseStatus", false));
                createResult.setDatabaseUser(json.getStr("databaseUser", ""));
                createResult.setDatabasePass(json.getStr("databasePass", ""));
                
                result.setData(createResult);
                result.setMsg("Website created successfully");
            } else {
                result.setMsg(json.getStr("msg", "Failed to create website"));
            }
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse create website response: " + e.getMessage(), e);
        }
    }
}