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
 * 按日期滚动的日志文件策略
 *
 * @author weihan
 */
public class DateRollingFileStrategy implements LogFileStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DateRollingFileStrategy.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String STRATEGY_NAME = "date";
    
    private final int maxHistory;
    private String baseDir;
    private Path basePath;

    public DateRollingFileStrategy(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public void init(String baseDir) {
        this.baseDir = baseDir;
        this.basePath = Paths.get(baseDir);
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            logger.error("Failed to create log directory: {}", baseDir, e);
            throw new RuntimeException("Failed to create log directory", e);
        }
    }

    @Override
    public File getLogFile(RequestLog log) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        String fileName = String.format("request-%s.log", date);
        Path logFile = basePath.resolve(fileName);
        
        try {
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
                cleanup();
            }
            return logFile.toFile();
        } catch (IOException e) {
            logger.error("Failed to create or access log file: {}", logFile, e);
            throw new RuntimeException("Failed to create or access log file", e);
        }
    }

    @Override
    public void cleanup() {
        try {
            File[] files = basePath.toFile().listFiles((dir, name) -> name.startsWith("request-") && name.endsWith(".log"));
            if (files == null || files.length <= maxHistory) {
                return;
            }

            Arrays.sort(files, Comparator.comparing(File::getName).reversed());
            for (int i = maxHistory; i < files.length; i++) {
                try {
                    Files.delete(files[i].toPath());
                } catch (IOException e) {
                    logger.error("Failed to delete old log file: {}", files[i], e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup old log files", e);
        }
    }
} 