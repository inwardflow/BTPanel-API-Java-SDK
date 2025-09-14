package net.heimeng.sdk.btapi.api.system;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.DiskInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 获取磁盘分区信息API实现
 * <p>
 * 用于获取宝塔面板的磁盘分区信息，包括挂载点、inode使用情况、容量使用情况等。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class GetDiskInfoApi extends BaseBtApi<BtResult<List<DiskInfo>>> {
    
    /**
     * API端点路径
     */
    private static final String ENDPOINT = "system?action=GetDiskInfo";
    
    /**
     * 构造函数，创建一个新的GetDiskInfoApi实例
     */
    public GetDiskInfoApi() {
        super(ENDPOINT, HttpMethod.POST);
    }
    
    /**
     * 解析API响应字符串为BtResult<List<DiskInfo>>对象
     * 
     * @param response API响应字符串
     * @return BtResult<List<DiskInfo>>对象
     * @throws BtApiException 当解析失败时抛出
     */
    @Override
    public BtResult<List<DiskInfo>> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new BtApiException("Empty response received");
        }

        try {
            if (!JSONUtil.isTypeJSON(response)) {
                throw new BtApiException("Invalid JSON response: " + response);
            }

            // 解析JSON数组
            Object jsonObj = JSONUtil.parse(response);
            BtResult<List<DiskInfo>> result = new BtResult<>();
            List<DiskInfo> diskInfos = new ArrayList<>();
            
            // 响应可能是直接的数组格式
            if (jsonObj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) jsonObj;
                result.setStatus(true);
                result.setMsg("Success");
                
                // 解析每个磁盘分区信息
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject diskJson = jsonArray.getJSONObject(i);
                    if (diskJson != null) {
                        DiskInfo diskInfo = new DiskInfo();
                        diskInfo.setPath(diskJson.getStr("path", ""));
                        
                        // 解析inodes信息
                        JSONArray inodesArray = diskJson.getJSONArray("inodes");
                        if (inodesArray != null) {
                            List<String> inodes = new ArrayList<>();
                            for (int j = 0; j < inodesArray.size(); j++) {
                                inodes.add(inodesArray.getStr(j, ""));
                            }
                            diskInfo.setInodes(inodes);
                        } else {
                            diskInfo.setInodes(Arrays.asList("", "", "", ""));
                        }
                        
                        // 解析size信息
                        JSONArray sizeArray = diskJson.getJSONArray("size");
                        if (sizeArray != null) {
                            List<String> sizes = new ArrayList<>();
                            for (int j = 0; j < sizeArray.size(); j++) {
                                sizes.add(sizeArray.getStr(j, ""));
                            }
                            diskInfo.setSize(sizes);
                        } else {
                            diskInfo.setSize(Arrays.asList("", "", "", ""));
                        }
                        
                        diskInfos.add(diskInfo);
                    }
                }
            } else if (jsonObj instanceof JSONObject) {
                // 响应可能是带有status字段的错误格式
                JSONObject json = (JSONObject) jsonObj;
                result.setStatus(json.getBool("status", false));
                result.setMsg(json.getStr("msg", ""));
            }
            
            result.setData(diskInfos);
            return result;

        } catch (JSONException e) {
            throw new BtApiException("Invalid JSON response: " + response);
        } catch (Exception e) {
            throw new BtApiException("Failed to parse disk info response: " + e.getMessage(), e);
        }
    }
}