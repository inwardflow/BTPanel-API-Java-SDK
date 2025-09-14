package net.heimeng.sdk.btapi.api.system;

import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 检查安装任务数量API实现
 * <p>
 * 用于检查宝塔面板是否有正在执行的安装任务。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetTaskCountApi extends BaseBtApi<BtResult<Integer>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "ajax?action=GetTaskCount";
    
    /**
     * 构造函数，创建一个新的GetTaskCountApi实例
     */
    public GetTaskCountApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 解析API响应字符串为BtResult<Integer>对象
     * 
     * @param response API响应字符串
     * @return BtResult<Integer>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<Integer> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            BtResult<Integer> result = new BtResult<>();
            result.setStatus(true);
            result.setMsg("Success");
            
            // 响应可能是纯数字字符串（如"0"）
            if (response.trim().matches("\\d+")) {
                result.setData(Integer.parseInt(response.trim()));
            } else if (JSONUtil.isTypeJSON(response)) {
                // 也可能是JSON格式，处理错误情况
                result.setStatus(false);
                result.setMsg("Invalid response format");
            } else {
                // 默认返回0
                result.setData(0);
            }
            
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (NumberFormatException e) {
            throw new BtApiException("Failed to parse task count: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse task count response: " + e.getMessage(), e);
        }
    }
}