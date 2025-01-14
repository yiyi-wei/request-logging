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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于文件大小的日志文件滚动策略
 *
 * @author weihan
 */
public class SizeBasedRollingStrategy implements LogFileStrategy {
    private static final Logger logger = LoggerFactory.getLogger(SizeBasedRollingStrategy.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Pattern SEQUENCE_PATTERN = Pattern.compile("request-(\\d{4}-\\d{2}-\\d{2})-(\\d+)\\.log");
    private static final String FILE_PREFIX = "request-";
    private static final String FILE_SUFFIX = ".log";

    private final long maxFileSize;
    private final int maxFiles;
    private String baseDir;
    private final AtomicInteger currentSequence = new AtomicInteger(0);

    public SizeBasedRollingStrategy(String maxFileSize, int maxFiles) {
        this.maxFileSize = parseFileSize(maxFileSize);
        this.maxFiles = maxFiles;
    }

    @Override
    public void init(String baseDir) {
        this.baseDir = baseDir;
        try {
            Files.createDirectories(Paths.get(baseDir));
            initializeSequence();
        } catch (IOException e) {
            logger.error("Failed to create log directory: {}", baseDir, e);
        }
    }

    @Override
    public File getLogFile(RequestLog log) {
        LocalDate today = LocalDate.now();
        String date = today.format(DATE_FORMATTER);
        
        File currentFile = getCurrentFile(date);
        if (currentFile.length() >= maxFileSize) {
            currentSequence.incrementAndGet();
            currentFile = getCurrentFile(date);
            cleanup();
        }
        
        return currentFile;
    }

    @Override
    public String getStrategyName() {
        return "size";
    }

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

    private File getCurrentFile(String date) {
        return new File(baseDir, String.format("%s%s-%d%s", FILE_PREFIX, date, currentSequence.get(), FILE_SUFFIX));
    }

    private void initializeSequence() {
        File dir = new File(baseDir);
        File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_SUFFIX));
        if (files != null) {
            int maxSeq = 0;
            String today = LocalDate.now().format(DATE_FORMATTER);
            
            for (File file : files) {
                Matcher matcher = SEQUENCE_PATTERN.matcher(file.getName());
                if (matcher.matches() && today.equals(matcher.group(1))) {
                    int seq = Integer.parseInt(matcher.group(2));
                    maxSeq = Math.max(maxSeq, seq);
                }
            }
            
            currentSequence.set(maxSeq);
        }
    }

    private long parseFileSize(String size) {
        size = size.toUpperCase().trim();
        long multiplier = 1;
        
        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("B")) {
            size = size.substring(0, size.length() - 1);
        }
        
        try {
            return Long.parseLong(size.trim()) * multiplier;
        } catch (NumberFormatException e) {
            logger.warn("Invalid file size format: {}. Using default 100MB", size);
            return 100 * 1024 * 1024;
        }
    }
} 