package ltd.weiyiyi.requestlogging.infrastructure.logfile;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 按天滚动的日志文件策略
 *
 * @author weihan
 */
public class DailyRollingStrategy extends AbstractLogFileStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DailyRollingStrategy.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private LocalDate currentDate;
    private File currentFile;

    public DailyRollingStrategy(int maxHistory) {
        super(maxHistory);
    }

    @Override
    protected void initializeStrategy() {
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
} 