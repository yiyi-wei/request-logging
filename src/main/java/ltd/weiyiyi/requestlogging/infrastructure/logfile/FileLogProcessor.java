package ltd.weiyiyi.requestlogging.infrastructure.logfile;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;
import ltd.weiyiyi.requestlogging.infrastructure.formatter.LogFormatter;
import ltd.weiyiyi.requestlogging.infrastructure.spi.LogFileStrategy;
import ltd.weiyiyi.requestlogging.infrastructure.spi.RequestLogProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件日志处理器
 * 负责将日志写入文件
 *
 * @author weihan
 */
public class FileLogProcessor implements RequestLogProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FileLogProcessor.class);
    private RequestLoggingProperties properties;
    private LogFormatter logFormatter;
    private LogFormatter plainLogFormatter;
    private final ConcurrentHashMap<String, LogFileStrategy> strategyCache = new ConcurrentHashMap<>();

    public FileLogProcessor() {
        // 无参构造函数，用于SPI加载
    }

    @Override
    public void init(RequestLoggingProperties properties) {
        this.properties = properties;
        this.logFormatter = new LogFormatter(properties);
        
        // 创建一个禁用颜色输出的配置副本
        RequestLoggingProperties plainProperties = new RequestLoggingProperties();
        copyProperties(properties, plainProperties);
        plainProperties.setEnableColorOutput(false);
        this.plainLogFormatter = new LogFormatter(plainProperties);
        
        initLogFileStrategy();
    }

    private void copyProperties(RequestLoggingProperties source, RequestLoggingProperties target) {
        target.setEnabled(source.isEnabled());
        target.setLogLevel(source.getLogLevel());
        target.setLogHeaders(source.isLogHeaders());
        target.setLogRequestBody(source.isLogRequestBody());
        target.setLogResponse(source.isLogResponse());
        target.setEnableColorOutput(source.isEnableColorOutput());
        target.setEnableFileLogging(source.isEnableFileLogging());
        target.setEnableConsoleLogging(source.isEnableConsoleLogging());
        target.setLogFileStrategy(source.getLogFileStrategy());
        target.setLogFileBaseDir(source.getLogFileBaseDir());
        target.setRequestStartFlag(source.getRequestStartFlag());
        target.setRequestEndFlag(source.getRequestEndFlag());
        target.setRequestStartForegroundColor(source.getRequestStartForegroundColor());
        target.setRequestStartBackgroundColor(source.getRequestStartBackgroundColor());
        target.setRequestEndForegroundColor(source.getRequestEndForegroundColor());
        target.setRequestEndBackgroundColor(source.getRequestEndBackgroundColor());
        target.setErrorForegroundColor(source.getErrorForegroundColor());
        target.setErrorBackgroundColor(source.getErrorBackgroundColor());
        target.setExcludeHeaders(source.getExcludeHeaders());
        target.setRequestBodyMaxLength(source.getRequestBodyMaxLength());
        target.setResponseMaxLength(source.getResponseMaxLength());
        target.setSeparator(source.getSeparator());
    }

    @Override
    public void processRequestStart(RequestLog log) {
        if (properties == null || !properties.isEnableFileLogging()) {
            return;
        }

        LogFileStrategy strategy = getLogFileStrategy();
        if (strategy == null) {
            logger.error("No log file strategy found for: {}", properties.getLogFileStrategy());
            return;
        }

        try {
            File logFile = strategy.getLogFile(log);
            if (logFile == null) {
                logger.error("Failed to get log file");
                return;
            }

            String logContent = plainLogFormatter.logRequestStart(log);
            writeToFile(logFile, logContent);
        } catch (Exception e) {
            logger.error("Error writing request start log to file", e);
        }
    }

    @Override
    public void processRequestComplete(RequestLog log) {
        if (properties == null || !properties.isEnableFileLogging()) {
            return;
        }

        LogFileStrategy strategy = getLogFileStrategy();
        if (strategy == null) {
            logger.error("No log file strategy found for: {}", properties.getLogFileStrategy());
            return;
        }

        try {
            File logFile = strategy.getLogFile(log);
            if (logFile == null) {
                logger.error("Failed to get log file");
                return;
            }

            String logContent = plainLogFormatter.logRequestComplete(log);
            writeToFile(logFile, logContent);
        } catch (Exception e) {
            logger.error("Error writing request complete log to file", e);
        }
    }

    @Override
    public void processRequestError(RequestLog log) {
        if (properties == null || !properties.isEnableFileLogging()) {
            return;
        }
        
        try {
            File logFile = getLogFileStrategy().getLogFile(log);
            String logMessage = plainLogFormatter.logRequestError(log);
            writeToFile(logFile, logMessage);
        } catch (Exception e) {
            logger.error("Error writing request error log to file", e);
        }
    }

    private void initLogFileStrategy() {
        ServiceLoader<LogFileStrategy> strategies = ServiceLoader.load(LogFileStrategy.class);
        for (LogFileStrategy strategy : strategies) {
            strategyCache.put(strategy.getStrategyName(), strategy);
            strategy.init(getLogFileBaseDir());
        }

        // 如果没有找到策略，使用默认的按日期分割策略
        if (strategyCache.isEmpty()) {
            LogFileStrategy defaultStrategy = new DailyRollingStrategy();
            defaultStrategy.init(getLogFileBaseDir());
            strategyCache.put(defaultStrategy.getStrategyName(), defaultStrategy);
        }
    }

    private LogFileStrategy getLogFileStrategy() {
        if (properties == null) {
            return null;
        }
        String strategyName = properties.getLogFileStrategy();
        return strategyCache.get(strategyName);
    }

    private File getLogFileBaseDir() {
        String baseDir = properties != null ? properties.getLogFileBaseDir() : null;
        if (baseDir == null || baseDir.trim().isEmpty()) {
            baseDir = System.getProperty("user.dir") + File.separator + "logs";
        }
        return new File(baseDir);
    }

    private void writeToFile(File file, String content) {
        if (file == null) {
            logger.error("Log file is null");
            return;
        }

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
             FileChannel channel = randomAccessFile.getChannel()) {
            
            FileLock lock = null;
            try {
                lock = channel.tryLock();
                if (lock != null) {
                    randomAccessFile.seek(randomAccessFile.length());
                    randomAccessFile.write((content + System.lineSeparator()).getBytes());
                }
            } finally {
                if (lock != null) {
                    lock.release();
                }
            }
        } catch (IOException e) {
            logger.error("Error writing to log file: " + file.getAbsolutePath(), e);
        }
    }
} 