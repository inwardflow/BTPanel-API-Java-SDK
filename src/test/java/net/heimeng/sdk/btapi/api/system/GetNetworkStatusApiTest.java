package net.heimeng.sdk.btapi.api.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.NetworkStatus;

@DisplayName("GetNetworkStatusApi 单元测试")
class GetNetworkStatusApiTest {

  private final GetNetworkStatusApi api = new GetNetworkStatusApi();

  @Test
  @DisplayName("应正确暴露接口元数据")
  void exposesEndpointMetadata() {
    assertEquals("system?action=GetNetWork", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
  }

  @Test
  @DisplayName("应正确解析直接成功响应")
  void parsesDirectSuccessResponse() {
    String response =
        """
        {
          "downTotal": 446326699,
          "upTotal": 77630707,
          "downPackets": 1519428,
          "upPackets": 175326,
          "down": 36.22,
          "up": 72.81,
          "cpu": [12.5, 8],
          "mem": {"memTotal": 8000, "memRealUsed": 4000},
          "load": {"one": 0.8, "five": 0.5, "fifteen": 0.3}
        }
        """;

    BtResult<NetworkStatus> result = api.parseResponse(response);

    assertTrue(result.isSuccess());
    assertEquals("Success", result.getMsg());
    assertEquals(446326699L, result.getData().getDownTotal());
    assertEquals(12.5, result.getData().getCpuUsage());
    assertEquals(8, result.getData().getCpuCores());
    assertEquals(50.0, result.getData().getMemoryUsage(), 0.001);
    assertEquals(0.8, result.getData().getLoad1Min(), 0.001);
    assertEquals(0.5, result.getData().getLoad5Min(), 0.001);
    assertEquals(0.3, result.getData().getLoad15Min(), 0.001);
  }

  @Test
  @DisplayName("应支持带 data 包装的成功响应")
  void parsesWrappedSuccessResponse() {
    String response =
        """
        {
          "status": true,
          "msg": "ok",
          "data": {
            "downTotal": 1,
            "upTotal": 2,
            "cpu": [1.5, 4],
            "mem": {"memTotal": 10, "memRealUsed": 5},
            "load": {"one": 1.0}
          }
        }
        """;

    BtResult<NetworkStatus> result = api.parseResponse(response);

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(1L, result.getData().getDownTotal());
    assertEquals(4, result.getData().getCpuCores());
  }

  @Test
  @DisplayName("应保留失败响应状态")
  void preservesFailureResponse() {
    BtResult<NetworkStatus> result =
        api.parseResponse("{\"status\":false,\"msg\":\"network unavailable\"}");

    assertFalse(result.isSuccess());
    assertEquals("network unavailable", result.getMsg());
  }

  @Test
  @DisplayName("成功响应缺少有效载荷时应抛出异常")
  void rejectsMissingPayload() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("supported payload"));
  }

  @Test
  @DisplayName("数组响应应抛出异常")
  void rejectsArrayResponse() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("[1,2,3]"));

    assertTrue(exception.getMessage().contains("JSON object"));
  }
}
