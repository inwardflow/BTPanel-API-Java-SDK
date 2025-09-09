package net.heimeng.sdk.btapi.model.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * SystemInfo模型类的单元测试
 * <p>
 * 测试系统信息模型的属性设置和获取方法
 * </p>
 */
@DisplayName("系统信息模型单元测试")
public class SystemInfoTest {

    @Test
    @DisplayName("测试SystemInfo的基本属性设置和获取")
    void testSystemInfoBasicProperties() {
        // 创建SystemInfo实例
        SystemInfo systemInfo = new SystemInfo();
        
        // 设置属性
        systemInfo.setHostname("test-server");
        systemInfo.setOs("Ubuntu 20.04.4 LTS x86_64");
        systemInfo.setKernel("5.4.0-109-generic");
        systemInfo.setCpuUsage(2.5);
        systemInfo.setMemoryTotal(8192L);  // 8GB内存，单位MB
        systemInfo.setMemoryUsed(4096L);   // 4GB已用，单位MB
        systemInfo.setPanelVersion("7.9.6");
        
        // 验证属性获取
        assertEquals("test-server", systemInfo.getHostname());
        assertEquals("Ubuntu 20.04.4 LTS x86_64", systemInfo.getOs());
        assertEquals("5.4.0-109-generic", systemInfo.getKernel());
        assertEquals(2.5, systemInfo.getCpuUsage());
        assertEquals(8192L, systemInfo.getMemoryTotal());
        assertEquals(4096L, systemInfo.getMemoryUsed());
        assertEquals("7.9.6", systemInfo.getPanelVersion());
    }

    @Test
    @DisplayName("测试SystemInfo的getMemoryUsagePercentage方法")
    void testGetMemoryUsagePercentagePercentage() {
        // 创建SystemInfo实例
        SystemInfo systemInfo = new SystemInfo();
        
        // 设置内存相关属性
        systemInfo.setMemoryTotal(8192L);  // 8GB内存，单位MB
        systemInfo.setMemoryUsed(4096L);   // 4GB已用，单位MB
        
        // 验证内存使用率计算
        assertEquals(50.0, systemInfo.getMemoryUsagePercentage(), 0.1);
        
        // 测试内存总量为0的情况
        systemInfo.setMemoryTotal(0L);
        assertEquals(0.0, systemInfo.getMemoryUsagePercentage(), 0.1);
        
        // 测试内存使用量为0的情况
        systemInfo.setMemoryTotal(8192L);
        systemInfo.setMemoryUsed(0L);
        assertEquals(0.0, systemInfo.getMemoryUsagePercentage(), 0.1);
    }

    @Test
    @DisplayName("测试SystemInfo的toString方法")
    void testToString() {
        // 创建SystemInfo实例
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setHostname("test-server");
        systemInfo.setOs("Ubuntu 20.04.4 LTS x86_64");
        
        // 验证toString方法返回非空字符串
        String toStringResult = systemInfo.toString();
        assertNotNull(toStringResult);
        assertEquals(true, toStringResult.contains("test-server"));
        assertEquals(true, toStringResult.contains("Ubuntu"));
    }

    @Test
    @DisplayName("测试SystemInfo处理空值和零值")
    void testSystemInfoWithNullValues() {
        // 创建SystemInfo实例
        SystemInfo systemInfo = new SystemInfo();
        
        // 设置一些属性为空值
        systemInfo.setHostname(null);
        systemInfo.setOs(null);
        systemInfo.setCpuUsage(-1.0);
        systemInfo.setMemoryTotal(-1L);
        
        // 验证空值处理
        assertEquals(null, systemInfo.getHostname());
        assertEquals(null, systemInfo.getOs());
        assertEquals(-1.0, systemInfo.getCpuUsage());
        assertEquals(-1L, systemInfo.getMemoryTotal());
        
        // 测试内存使用率计算在边界情况下的处理
        assertEquals(0.0, systemInfo.getMemoryUsagePercentage(), 0.1);
    }
}