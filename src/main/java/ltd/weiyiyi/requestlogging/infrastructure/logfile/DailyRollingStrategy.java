package ltd.weiyiyi.requestlogging.infrastructure.logfile;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.spi.LogFileStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 按天滚动的日志文件策略
 *
 * @author weihan
 */
public class DailyRollingStrategy implements LogFileStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DailyRollingStrategy.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String FILE_PREFIX = "request-";
    private static final String FILE_SUFFIX = ".log";

    private final int maxHistory;
    private String baseDir;
    private LocalDate currentDate;
    private File currentFile;

    public DailyRollingStrategy(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    @Override
    public void init(String baseDir) {
        this.baseDir = baseDir;
        try {
            Files.createDirectories(Paths.get(baseDir));
        } catch (IOException e) {
            logger.error("Failed to create log directory: {}", baseDir, e);
        }
    }

    @Override
    public File getLogFile(RequestLog log) {
        LocalDate today = LocalDate.now();
        if (currentFile == null || !today.equals(currentDate)) {
            currentDate = today;
            currentFile = new File(baseDir, FILE_PREFIX + today.format(DATE_FORMATTER) + FILE_SUFFIX);
            cleanup();
        }
        return currentFile;
    }

    @Override
    public String getStrategyName() {
        return "daily";
    }

    @Override
    public void cleanup() {
        try {
            File dir = new File(baseDir);
            File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_SUFFIX));
            if (files != null && files.length > maxHistory) {
                Arrays.sort(files, Comparator.comparing(File::lastModified));
                for (int i = 0; i < files.length - maxHistory; i++) {
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