package ltd.weiyiyi.requestlogging.infrastructure.logfile;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.spi.LogFileStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 基于大小滚动的日志文件策略
 * 当日志文件达到指定大小时创建新文件
 *
 * @author weihan
 */
public class SizeBasedRollingStrategy implements LogFileStrategy {
    private static final Logger logger = LoggerFactory.getLogger(SizeBasedRollingStrategy.class);
    private static final String STRATEGY_NAME = "size";
    private static final String FILE_SUFFIX = ".log";
    private static final long DEFAULT_MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final int DEFAULT_MAX_FILES = 10;

    private File baseDir;
    private File currentFile;
    private final long maxFileSize;
    private final int maxFiles;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger currentFileIndex = new AtomicInteger(0);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public SizeBasedRollingStrategy() {
        this(DEFAULT_MAX_FILE_SIZE, DEFAULT_MAX_FILES);
    }

    public SizeBasedRollingStrategy(long maxFileSize, int maxFiles) {
        this.maxFileSize = maxFileSize;
        this.maxFiles = maxFiles;
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
            if (currentFile == null || shouldRollOver()) {
                rollOver();
            }
            if (!currentFile.exists()) {
                if (!currentFile.createNewFile()) {
                    logger.error("Failed to create log file: {}", currentFile);
                    throw new RuntimeException("Failed to create log file: " + currentFile);
                }
            }
            return currentFile;
        } catch (Exception e) {
            logger.error("Error getting log file", e);
            throw new RuntimeException("Error getting log file", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cleanup() {
        if (baseDir == null || !baseDir.exists()) {
            return;
        }

        File[] files = baseDir.listFiles((dir, name) -> name.startsWith("request-") && name.endsWith(FILE_SUFFIX));
        if (files == null || files.length <= maxFiles) {
            return;
        }

        // 按修改时间排序
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        // 删除超出最大文件数的文件
        for (int i = 0; i < files.length - maxFiles; i++) {
            if (!files[i].delete()) {
                logger.warn("Failed to delete old log file: {}", files[i].getAbsolutePath());
            }
        }
    }

    private void initializeCurrentFile() {
        File[] files = baseDir.listFiles((dir, name) -> name.startsWith("request-") && name.endsWith(FILE_SUFFIX));
        if (files != null && files.length > 0) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            currentFile = files[0];
            String fileName = currentFile.getName();
            int index = extractIndex(fileName);
            currentFileIndex.set(index);
        } else {
            rollOver();
        }
    }

    private boolean shouldRollOver() {
        return currentFile != null && currentFile.length() >= maxFileSize;
    }

    private void rollOver() {
        int index = currentFileIndex.incrementAndGet();
        String date = dateFormat.format(new Date());
        String fileName = String.format("request-%s-%03d%s", date, index, FILE_SUFFIX);
        currentFile = new File(baseDir, fileName);
    }

    private int extractIndex(String fileName) {
        try {
            int start = fileName.lastIndexOf('-') + 1;
            int end = fileName.lastIndexOf('.');
            return Integer.parseInt(fileName.substring(start, end));
        } catch (Exception e) {
            return 0;
        }
    }
} 