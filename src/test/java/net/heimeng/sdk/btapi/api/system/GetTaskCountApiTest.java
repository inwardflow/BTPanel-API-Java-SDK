package net.heimeng.sdk.btapi.api.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("GetTaskCountApi 单元测试")
class GetTaskCountApiTest {

  private final GetTaskCountApi api = new GetTaskCountApi();

  @Test
  @DisplayName("应正确暴露接口元数据")
  void exposesEndpointMetadata() {
    assertEquals("ajax?action=GetTaskCount", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
  }

  @Test
  @DisplayName("应支持纯数字响应")
  void parsesPlainIntegerResponse() {
    BtResult<Integer> result = api.parseResponse("0");

    assertTrue(result.isSuccess());
    assertEquals("Success", result.getMsg());
    assertEquals(0, result.getData());
  }

  @Test
  @DisplayName("应支持带空白的数字响应")
  void parsesTrimmedIntegerResponse() {
    BtResult<Integer> result = api.parseResponse("  12 \n");

    assertTrue(result.isSuccess());
    assertEquals(12, result.getData());
  }

  @Test
  @DisplayName("应支持带引号的数字响应")
  void parsesQuotedIntegerResponse() {
    BtResult<Integer> result = api.parseResponse("\"3\"");

    assertTrue(result.isSuccess());
    assertEquals(3, result.getData());
  }

  @Test
  @DisplayName("应支持成功的 JSON 包装响应")
  void parsesSuccessfulJsonPayload() {
    BtResult<Integer> result = api.parseResponse("{\"status\":true,\"msg\":\"ok\",\"data\":\"5\"}");

    assertTrue(result.isSuccess());
    assertEquals("ok", result.getMsg());
    assertEquals(5, result.getData());
  }

  @Test
  @DisplayName("应支持 count 字段响应")
  void parsesCountFieldPayload() {
    BtResult<Integer> result = api.parseResponse("{\"count\":7}");

    assertTrue(result.isSuccess());
    assertEquals("Success", result.getMsg());
    assertEquals(7, result.getData());
  }

  @Test
  @DisplayName("应保留失败 JSON 的状态与消息")
  void preservesFailureStatusResponse() {
    BtResult<Integer> result = api.parseResponse("{\"status\":false,\"msg\":\"panel is busy\"}");

    assertTrue(result.isFailed());
    assertEquals("panel is busy", result.getMsg());
    assertNull(result.getData());
  }

  @Test
  @DisplayName("空响应应抛出异常")
  void rejectsBlankResponse() {
    BtApiException exception = assertThrows(BtApiException.class, () -> api.parseResponse(" "));

    assertTrue(exception.getMessage().contains("Empty response"));
  }

  @Test
  @DisplayName("无效文本响应应抛出异常")
  void rejectsUnsupportedPlainText() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("plain text"));

    assertTrue(exception.getMessage().contains("Invalid task count response"));
  }

  @Test
  @DisplayName("缺少数值载荷的成功 JSON 应抛出异常")
  void rejectsSuccessfulJsonWithoutPayload() {
    BtApiException exception =
        assertThrows(BtApiException.class, () -> api.parseResponse("{\"status\":true}"));

    assertTrue(exception.getMessage().contains("missing required data field"));
  }

  @Test
  @DisplayName("非法数值载荷应抛出异常")
  void rejectsInvalidNumericPayload() {
    BtApiException exception =
        assertThrows(
            BtApiException.class,
            () -> api.parseResponse("{\"status\":true,\"data\":\"not-a-number\"}"));

    assertTrue(exception.getMessage().contains("not a valid integer"));
  }
}
