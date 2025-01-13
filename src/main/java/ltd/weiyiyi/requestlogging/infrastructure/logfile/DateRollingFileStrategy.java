package ltd.weiyiyi.requestlogging.infrastructure.logfile;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.spi.LogFileStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 按日期滚动的日志文件策略
 * 每天创建一个新的日志文件
 *
 * @author weihan
 */
public class DateRollingFileStrategy implements LogFileStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DateRollingFileStrategy.class);
    private static final String STRATEGY_NAME = "date";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String FILE_SUFFIX = ".log";
    
    private File baseDir;
    private final ThreadLocal<SimpleDateFormat> dateFormat = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat(DATE_PATTERN));
    private final int maxDays;

    public DateRollingFileStrategy(int maxDays) {
        this.maxDays = maxDays;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public void init(File baseDir) {
        this.baseDir = baseDir;
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            logger.error("Failed to create log directory: {}", baseDir.getAbsolutePath());
        }
    }

    @Override
    public File getLogFile(RequestLog log) {
        try {
            String fileName = "request." + dateFormat.get().format(new Date()) + FILE_SUFFIX;
            return new File(baseDir, fileName);
        } catch (Exception e) {
            logger.error("Error creating log file", e);
            return null;
        }
    }

    @Override
    public void cleanup() {
        if (baseDir == null || !baseDir.exists()) {
            return;
        }

        File[] files = baseDir.listFiles((dir, name) -> name.endsWith(FILE_SUFFIX));
        if (files == null || files.length <= maxDays) {
            return;
        }

        // 按修改时间排序，保留最新的maxDays个文件
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        for (int i = maxDays; i < files.length; i++) {
            if (!files[i].delete()) {
                logger.warn("Failed to delete old log file: {}", files[i]);
            }
        }
    }
} 