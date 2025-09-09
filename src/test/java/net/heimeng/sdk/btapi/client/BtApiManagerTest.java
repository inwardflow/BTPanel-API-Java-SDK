package net.heimeng.sdk.btapi.client;

import net.heimeng.sdk.btapi.api.system.GetSystemInfoApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.SystemInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BtApiManager的单元测试类
 * <p>
 * 测试API管理器的同步和异步API调用功能
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("API管理器单元测试")
public class BtApiManagerTest {

    @Mock
    private BtClient mockClient;

    @Mock
    private GetSystemInfoApi mockSystemInfoApi;

    private BtApiManager apiManager;

    @BeforeEach
    void setUp() {
        // 创建API管理器实例
        apiManager = new BtApiManager(mockClient);
    }

    @AfterEach
    void tearDown() {
        // 清理资源
        apiManager.close();
    }

    @Test
    @DisplayName("测试同步API调用成功场景")
    void testExecute_Success() throws BtApiException {
        // 准备模拟数据
        BtResult<SystemInfo> mockResult = new BtResult<>();
        mockResult.setStatus(true);
        mockResult.setMsg("success");
        
        SystemInfo mockSystemInfo = new SystemInfo();
        mockSystemInfo.setHostname("test-server");
        mockSystemInfo.setOs("Ubuntu 20.04");
        mockResult.setData(mockSystemInfo);
        
        // 模拟客户端请求
        when(mockClient.execute(mockSystemInfoApi)).thenReturn(mockResult);
        
        // 执行API调用
        BtResult<SystemInfo> result = apiManager.execute(mockSystemInfoApi);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("success", result.getMsg());
        assertNotNull(result.getData());
        assertEquals("test-server", result.getData().getHostname());
        
        // 验证调用了客户端的execute方法
        verify(mockClient).execute(mockSystemInfoApi);
    }

    @Test
    @DisplayName("测试同步API调用失败场景")
    void testExecute_Failure() throws BtApiException {
        // 准备模拟数据
        BtResult<SystemInfo> mockResult = new BtResult<>();
        mockResult.setStatus(false);
        mockResult.setMsg("API调用失败");
        
        // 模拟客户端请求
        when(mockClient.execute(mockSystemInfoApi)).thenReturn(mockResult);
        
        // 执行API调用
        BtResult<SystemInfo> result = apiManager.execute(mockSystemInfoApi);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("API调用失败", result.getMsg());
    }

    @Test
    @DisplayName("测试同步API调用抛出异常")
    void testExecute_Exception() throws BtApiException {
        // 模拟客户端请求抛出异常
        doThrow(new BtApiException("服务器内部错误")).when(mockClient).execute(mockSystemInfoApi);
        
        // 验证异常抛出
        BtApiException exception = assertThrows(BtApiException.class, 
                () -> apiManager.execute(mockSystemInfoApi),
                "API调用失败时应抛出BtApiException");
        
        // 验证异常信息
        assertEquals("服务器内部错误", exception.getMessage());
    }

    @Test
    @DisplayName("测试异步API调用成功场景")
    void testExecuteAsync_Future_Success() throws BtApiException, ExecutionException, InterruptedException {
        // 准备模拟数据
        BtResult<SystemInfo> mockResult = new BtResult<>();
        mockResult.setStatus(true);
        mockResult.setMsg("success");
        
        // 模拟客户端请求
        when(mockClient.executeAsync(mockSystemInfoApi)).thenReturn(CompletableFuture.completedFuture(mockResult));
        
        // 执行异步API调用
        CompletableFuture<BtResult<SystemInfo>> future = apiManager.executeAsync(mockSystemInfoApi);
        
        // 等待结果
        BtResult<SystemInfo> result = future.get();
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试异步API调用抛出异常")
    void testExecuteAsync_Future_Exception() throws InterruptedException {
        // 模拟客户端请求抛出异常
        CompletableFuture<BtResult<SystemInfo>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new BtApiException("未授权访问"));
        when(mockClient.executeAsync(mockSystemInfoApi)).thenReturn(failedFuture);
        
        // 执行异步API调用
        CompletableFuture<BtResult<SystemInfo>> future = apiManager.executeAsync(mockSystemInfoApi);
        
        // 验证异常抛出
        ExecutionException exception = assertThrows(ExecutionException.class, 
                () -> future.get(5, TimeUnit.SECONDS),
                "异步API调用失败时应抛出ExecutionException");
        
        // 验证异常原因
        assertInstanceOf(BtApiException.class, exception.getCause());
        assertEquals("未授权访问", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("测试自定义超时的异步API调用")
    void testExecuteAsyncWithTimeout_Future() throws BtApiException {
        // 准备模拟数据
        BtResult<SystemInfo> mockResult = new BtResult<>();
        mockResult.setStatus(true);
        mockResult.setMsg("success");
        
        // 模拟客户端请求
        when(mockClient.executeAsync(mockSystemInfoApi)).thenReturn(CompletableFuture.completedFuture(mockResult));
        
        // 执行自定义超时的异步API调用
        BtResult<SystemInfo> result = apiManager.executeAsyncWithTimeout(mockSystemInfoApi, 5000, TimeUnit.MILLISECONDS);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试关闭API管理器")
    void testClose() {
        // 创建新的API管理器实例
        BtApiManager newApiManager = new BtApiManager(mockClient);
        
        // 关闭API管理器
        newApiManager.close();
        
        // 在实际项目中，您可能需要验证客户端资源是否被正确释放
    }

    @Test
    @DisplayName("测试异步API调用超时场景")
    void testExecuteAsync_Future_Timeout() throws InterruptedException {
        // 模拟客户端请求耗时较长
        CompletableFuture<BtResult<SystemInfo>> slowFuture = new CompletableFuture<>();
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                slowFuture.complete(new BtResult<>());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        when(mockClient.executeAsync(mockSystemInfoApi)).thenReturn(slowFuture);
        
        // 执行异步API调用
        CompletableFuture<BtResult<SystemInfo>> future = apiManager.executeAsync(mockSystemInfoApi);
        
        // 验证超时异常抛出
        TimeoutException exception = assertThrows(TimeoutException.class, 
                () -> future.get(1, TimeUnit.SECONDS),
                "异步API调用超时时应抛出TimeoutException");
    }


}