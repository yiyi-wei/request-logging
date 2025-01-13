package ltd.weiyiyi.requestlogging.infrastructure.logfile;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.spi.LogFileStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 按日期滚动的日志文件策略
 * 每天创建一个新的日志文件
 *
 * @author weihan
 */
public class DailyRollingStrategy implements LogFileStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DailyRollingStrategy.class);
    private static final String STRATEGY_NAME = "daily";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String FILE_SUFFIX = ".log";
    
    private File baseDir;
    private final ReentrantLock lock = new ReentrantLock();
    private final ThreadLocal<SimpleDateFormat> dateFormat = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat(DATE_PATTERN));
    private volatile File currentFile;
    private volatile String currentDate;
    private final int maxDays;

    /**
     * 默认构造函数，设置默认保留7天的日志
     */
    public DailyRollingStrategy() {
        this(7);
    }

    public DailyRollingStrategy(int maxDays) {
        this.maxDays = maxDays;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public void init(File baseDir) {
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            logger.error("Failed to create base directory: {}", baseDir);
            throw new RuntimeException("Failed to create base directory: " + baseDir);
        }
        this.baseDir = baseDir;
        initializeCurrentFile();
    }

    @Override
    public File getLogFile(RequestLog log) {
        try {
            lock.lock();
            String date = dateFormat.get().format(new Date());
            if (currentFile == null || !date.equals(currentDate)) {
                rollOver(date);
            }
            return currentFile;
        } catch (Exception e) {
            logger.error("Error getting log file", e);
            return null;
        } finally {
            lock.unlock();
        }
    }

    private void initializeCurrentFile() {
        try {
            lock.lock();
            String date = dateFormat.get().format(new Date());
            rollOver(date);
        } catch (Exception e) {
            logger.error("Error initializing current file", e);
        } finally {
            lock.unlock();
        }
    }

    private void rollOver(String date) {
        try {
            String fileName = String.format("request-%s%s", date, FILE_SUFFIX);
            File newFile = new File(baseDir, fileName);
            if (!newFile.exists()) {
                try {
                    if (!newFile.createNewFile()) {
                        logger.error("Failed to create new log file: {}", newFile);
                        throw new IOException("Failed to create new log file: " + newFile);
                    }
                } catch (IOException e) {
                    logger.error("Error creating log file", e);
                    throw e;
                }
            }
            currentFile = newFile;
            currentDate = date;
        } catch (Exception e) {
            logger.error("Error rolling over log file", e);
            throw new RuntimeException("Error rolling over log file", e);
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