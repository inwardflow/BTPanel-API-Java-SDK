package net.heimeng.sdk.btapi.api.ftp;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ftp.FtpAccount;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取FTP账户列表API实现
 * <p>
 * 用于获取宝塔面板中的所有FTP账户信息列表。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetFtpAccountsApi extends BaseBtApi<BtResult<List<FtpAccount>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "ftp?action=getData";
    
    /**
     * 构造函数，创建一个新的GetFtpAccountsApi实例
     */
    public GetFtpAccountsApi() {
        super(ENDPOINT, HttpMethod.POST);
        
        // 设置默认参数
        addParam("table", "ftps");
    }
    
    /**
     * 解析API响应字符串为BtResult<List<FtpAccount>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<List<FtpAccount>>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<List<FtpAccount>> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            JSONObject json = JSONUtil.parseObj(response);
            BtResult<List<FtpAccount>> result = new BtResult<>();
            List<FtpAccount> ftpAccounts = new ArrayList<>();
            
            // 检查响应是否包含data字段
            if (json.containsKey("data")) {
                result.setStatus(true);
                result.setMsg("Success");
                
                // 解析FTP账户列表
                JSONArray ftpArray = json.getJSONArray("data");
                if (ftpArray != null && !ftpArray.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    
                    for (int i = 0; i < ftpArray.size(); i++) {
                        JSONObject ftpJson = ftpArray.getJSONObject(i);
                        if (ftpJson != null) {
                            FtpAccount ftpAccount = new FtpAccount();
                            
                            // 设置FTP账户基本信息
                            ftpAccount.setId(ftpJson.getInt("id", 0));
                            ftpAccount.setUsername(ftpJson.getStr("name", ""));
                            ftpAccount.setPath(ftpJson.getStr("path", ""));
                            ftpAccount.setSize(ftpJson.getLong("size", 0L));
                            ftpAccount.setUsedSize(ftpJson.getLong("used", 0L));
                            ftpAccount.setStatus(ftpJson.getStr("status", "normal"));
                            ftpAccount.setCanViewAll(ftpJson.getBool("ps", false));
                            ftpAccount.setWebsiteDomain(ftpJson.getStr("domain", ""));
                            
                            // 解析创建时间
                            try {
                                String createTimeStr = ftpJson.getStr("create_time", "");
                                if (!createTimeStr.isEmpty()) {
                                    ftpAccount.setCreateTime(dateFormat.parse(createTimeStr));
                                }
                            } catch (ParseException e) {
                                // 忽略时间解析错误
                            }
                            
                            ftpAccounts.add(ftpAccount);
                        }
                    }
                }
            } else if (json.containsKey("status")) {
                // 处理错误响应
                result.setStatus(json.getBool("status", false));
                result.setMsg(json.getStr("msg", ""));
            }
            
            result.setData(ftpAccounts);
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse FTP accounts response: " + e.getMessage(), e);
        }
    }
}