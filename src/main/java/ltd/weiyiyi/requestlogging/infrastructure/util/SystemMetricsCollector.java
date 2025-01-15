package ltd.weiyiyi.requestlogging.infrastructure.util;

import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 系统指标收集器
 * 用于收集系统运行时的各项指标
 *
 * @author weihan
 */
@Component
public class SystemMetricsCollector {

    private final OperatingSystemMXBean operatingSystemMXBean;
    private final MemoryMXBean memoryMXBean;
    private final ThreadMXBean threadMXBean;
    private final String instanceId;
    private final String hostName;

    public SystemMetricsCollector() {
        this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.instanceId = UUID.randomUUID().toString();
        
        String tempHostName;
        try {
            tempHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            tempHostName = "unknown-host";
        }
        this.hostName = tempHostName;
    }

    /**
     * 获取实例ID
     *
     * @return 实例ID
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * 获取主机名
     *
     * @return 主机名
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * 获取CPU负载
     *
     * @return CPU负载百分比
     */
    public double getCpuLoad() {
        if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) operatingSystemMXBean).getCpuLoad() * 100;
        }
        return operatingSystemMXBean.getSystemLoadAverage() * 100 / operatingSystemMXBean.getAvailableProcessors();
    }

    /**
     * 获取已使用内存（GB）
     *
     * @return 已使用内存大小
     */
    public double getUsedMemory() {
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
        return (heapUsed + nonHeapUsed) / (1024.0 * 1024.0 * 1024.0);
    }

    /**
     * 获取总内存（GB）
     *
     * @return 总内存大小
     */
    public double getTotalMemory() {
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        long nonHeapMax = memoryMXBean.getNonHeapMemoryUsage().getMax();
        return (heapMax + nonHeapMax) / (1024.0 * 1024.0 * 1024.0);
    }

    /**
     * 获取运行环境
     *
     * @return 运行环境名称
     */
    public String getEnvironment() {
        String env = System.getProperty("spring.profiles.active");
        return env != null ? env : "default";
    }

    /**
     * 收集系统指标
     *
     * @return 系统指标Map
     */
    public Map<String, Object> collectMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // 实例信息
        metrics.put("instanceId", getInstanceId());
        metrics.put("hostName", getHostName());
        metrics.put("environment", getEnvironment());

        // CPU相关指标
        metrics.put("cpuLoad", getCpuLoad());
        metrics.put("systemLoadAverage", operatingSystemMXBean.getSystemLoadAverage());
        metrics.put("availableProcessors", operatingSystemMXBean.getAvailableProcessors());

        // 内存相关指标
        metrics.put("heapMemoryUsage", memoryMXBean.getHeapMemoryUsage().getUsed());
        metrics.put("heapMemoryMax", memoryMXBean.getHeapMemoryUsage().getMax());
        metrics.put("nonHeapMemoryUsage", memoryMXBean.getNonHeapMemoryUsage().getUsed());
        metrics.put("usedMemory", getUsedMemory());
        metrics.put("totalMemory", getTotalMemory());

        // 线程相关指标
        metrics.put("threadCount", threadMXBean.getThreadCount());
        metrics.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        metrics.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());

        return metrics;
    }
} 