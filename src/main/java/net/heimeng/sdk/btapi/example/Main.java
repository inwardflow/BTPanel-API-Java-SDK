package net.heimeng.sdk.btapi.example;

import net.heimeng.sdk.btapi.BtSdk;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.BtSite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* 宝塔面板SDK使用示例
*
* @author InwardFlow
*/
public class Main {
   public static void main(String[] args) {
       // 宝塔面板配置
       String baseUrl = "http://your-bt-panel:8888";
       String apiKey = "your-api-key";

       try {
           // 初始化SDK
           BtSdk btSDK = new BtSdk(baseUrl, apiKey);

           // 示例1: 获取系统总览信息
           System.out.println("=== 获取系统总览信息 ===");
           String systemTotal = btSDK.getSystemTotal();
           System.out.println("系统总览信息: " + systemTotal);

           // 示例2: 获取网站列表
           System.out.println("\n=== 获取网站列表 ===");
           List<BtSite> siteList = btSDK.getSiteList();
           System.out.println("网站数量: " + siteList.size());
           for (BtSite site : siteList) {
               System.out.println("网站ID: " + site.getId() + ", 域名: " + site.getName() + ", 状态: " + (site.isStatus() ? "启用" : "禁用"));
           }

           // 示例3: 创建目录
           System.out.println("\n=== 创建目录 ===");
           BtResult createDirResult = btSDK.createDir("/www/wwwroot/test_dir");
           System.out.println("创建目录结果: " + createDirResult.isSuccess() + ", 消息: " + createDirResult.getMsg());

           // 示例4: 释放内存
           System.out.println("\n=== 释放内存 ===");
           BtResult reMemoryResult = btSDK.reMemory();
           System.out.println("释放内存结果: " + reMemoryResult.isSuccess() + ", 消息: " + reMemoryResult.getMsg());

           // 示例5: 添加FTP用户
           System.out.println("\n=== 添加FTP用户 ===");
           Map<String, Object> ftpParams = new HashMap<>();
           ftpParams.put("name", "test_ftp_user");
           ftpParams.put("password", "test_password");
           ftpParams.put("path", "/www/wwwroot/test_dir");
           ftpParams.put("ps", "测试FTP用户");
           BtResult addFtpResult = btSDK.addFtpUser(ftpParams);
           System.out.println("添加FTP用户结果: " + addFtpResult.isSuccess() + ", 消息: " + addFtpResult.getMsg());

       } catch (Exception e) {
           System.err.println("操作失败: " + e.getMessage());
           e.printStackTrace();
       }
   }
}