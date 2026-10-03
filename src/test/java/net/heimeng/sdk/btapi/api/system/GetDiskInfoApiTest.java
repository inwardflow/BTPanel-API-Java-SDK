package net.heimeng.sdk.btapi.api.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.DiskInfo;

@DisplayName("GetDiskInfoApi 单元测试")
class GetDiskInfoApiTest {

  private final GetDiskInfoApi api = new GetDiskInfoApi();

  @Test
  @DisplayName("应正确暴露接口元数据")
  void exposesEndpointMetadata() {
    assertEquals("system?action=GetDiskInfo", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
  }

  @Test
  @DisplayName("应正确解析数组响应")
  void parsesArrayResponse() {
    String response =
        """
        [
          {
            "path": "/",
            "inodes": ["8675328", "148216", "8527112", "2%"],
            "size": ["8.3G", "4.0G", "4.3G", "49%"]
          }
        ]
        """;

    BtResult<List<DiskInfo>> result = api.parseResponse(response);

    assertTrue(result.isSuccess());
    assertEquals("Success", result.getMsg());
    assertEquals(1, result.getData().size());
    DiskInfo diskInfo = result.getData().get(0);
    assertEquals("/", diskInfo.getPath());
    assertEquals("8.3G", diskInfo.getTotalSize());
    assertEquals("4.0G", diskInfo.getUsedSize());
    assertEquals("4.3G", diskInfo.getFreeSize());
    assertEquals("49%", diskInfo.getUsagePercentage());
  }

  @Test
  @DisplayName("应支持带 data 包装的成功响应")
  void parsesWrappedSuccessResponse() {
    String response =
        """
        {
          "status": true,
          "msg": "ok",
          "data": [
            {
              "path": "/data",
              "inodes": ["10", "2", "8", "20%"],
              "size": ["100G", "20G", "80G", "20%"]
            }
          ]
        }
        """;

    BtResult<List<DiskInfo>> result = api.parseResponse(response);

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals("/data", result.getData().get(0).getPath());
  }

  @Test
  @DisplayName("应保留失败响应状态")
  void preservesFailureResponse() {
    BtResult<List<DiskInfo>> result = api.parseResponse("{\"status\":false,\"msg\":\"denied\"}");

    assertFalse(result.isSuccess());
    assertEquals("denied", result.getMsg());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  @DisplayName("成功包装响应缺少 data 时应抛出异常")
  void rejectsMissingWrappedData() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("required data array"));
  }

  @Test
  @DisplayName("无效文本响应应抛出异常")
  void rejectsInvalidTextResponse() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("plain text"));

    assertTrue(exception.getMessage().contains("Invalid JSON"));
  }
}
