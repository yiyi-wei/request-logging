package ltd.weiyiyi.requestlogging.infrastructure.logfile;

import ltd.weiyiyi.requestlogging.infrastructure.spi.LogFileStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 日志文件策略抽象基类
 * 实现通用的文件清理逻辑
 *
 * @author weihan
 */
public abstract class AbstractLogFileStrategy implements LogFileStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLogFileStrategy.class);
    protected static final String FILE_PREFIX = "request-";
    protected static final String FILE_SUFFIX = ".log";
    
    protected String baseDir;
    protected final int maxFiles;

    protected AbstractLogFileStrategy(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    @Override
    public void init(String baseDir) {
        this.baseDir = baseDir;
        initializeStrategy();
    }

    /**
     * 初始化策略的具体实现
     * 子类可以根据需要重写此方法
     */
    protected abstract void initializeStrategy();

    @Override
    public void cleanup() {
        try {
            File dir = new File(baseDir);
            File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_SUFFIX));
            if (files != null && files.length > maxFiles) {
                Arrays.sort(files, Comparator.comparing(File::lastModified));
                for (int i = 0; i < files.length - maxFiles; i++) {
                    if (!files[i].delete()) {
                        logger.warn("Failed to delete old log file: {}", files[i].getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup old log files", e);
        }
    }
} 