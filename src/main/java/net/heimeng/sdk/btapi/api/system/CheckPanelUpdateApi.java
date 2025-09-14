package net.heimeng.sdk.btapi.api.system;

import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.PanelUpdateInfo;

/**
 * 检查面板更新API实现
 * <p>
 * 用于检查宝塔面板是否有可用更新，获取最新版本信息。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class CheckPanelUpdateApi extends BaseBtApi<BtResult<PanelUpdateInfo>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "ajax?action=UpdatePanel";
    
    /**
     * 构造函数，创建一个新的CheckPanelUpdateApi实例
     * 
     * @param forceCheck 是否强制检查更新
     */
    public CheckPanelUpdateApi(boolean forceCheck) {
        super(ENDPOINT, HttpMethod.POST);
        if (forceCheck) {
            addParam("check", true);
        }
    }
    
    /**
     * 构造函数，创建一个新的CheckPanelUpdateApi实例（默认不强制检查）
     */
    public CheckPanelUpdateApi() {
        this(false);
    }
    
    /**
     * 设置是否强制检查更新
     * 
     * @param forceCheck 是否强制检查更新
     * @return 当前API实例，支持链式调用
     */
    public CheckPanelUpdateApi setForceCheck(boolean forceCheck) {
        if (forceCheck) {
            addParam("check", true);
        } else {
            removeParam("check");
        }
        return this;
    }
    
    /**
     * 解析API响应字符串为BtResult<PanelUpdateInfo>对象
     * 
     * @param response API响应字符串
     * @return BtResult<PanelUpdateInfo>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<PanelUpdateInfo> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            JSONObject json = JSONUtil.parseObj(response);
            BtResult<PanelUpdateInfo> result = new BtResult<>();
            
            // 根据是否包含status字段判断响应类型
            boolean status = json.getBool("status", false);
            result.setStatus(status);
            result.setMsg(json.getStr("msg", ""));
            
            // 解析面板更新信息
            if (status) {
                PanelUpdateInfo updateInfo = new PanelUpdateInfo();
                updateInfo.setStatus(status);
                updateInfo.setVersion(json.getStr("version", ""));
                updateInfo.setUpdateMsg(json.getStr("updateMsg", ""));
                
                result.setData(updateInfo);
            }
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse panel update info response: " + e.getMessage(), e);
        }
    }
}