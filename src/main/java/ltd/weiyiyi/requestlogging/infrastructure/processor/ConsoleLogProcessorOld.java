package ltd.weiyiyi.requestlogging.infrastructure.processor;

/**
 * @author Wei Han
 * @description
 * @date 14/01/2025 15:47
 * @domain www.weiyiyi.ltd
 */
public class ConsoleLogProcessorOld {

    //    private static final Logger logger = LoggerFactory.getLogger(ConsoleLogProcessor.class);
    //    private RequestLoggingProperties properties;
    //    private LogFormatter logFormatter;
    //
    //    public ConsoleLogProcessor() {
    //        // 无参构造函数，用于SPI加载
    //    }
    //
    //    public void init(RequestLoggingProperties properties) {
    //        this.properties = properties;
    //        this.logFormatter = new LogFormatter(properties);
    //    }
    //
    //    @Override
    //    public void processRequestStart(RequestLog log) {
    //        if (properties == null || !properties.isEnableConsoleLogging()) {
    //            return;
    //        }
    //
    //        String logContent = logFormatter.logRequestStart(log);
    //        logger.info(logContent);
    //    }
    //
    //    @Override
    //    public void processRequestComplete(RequestLog log) {
    //        if (properties == null || !properties.isEnableConsoleLogging()) {
    //            return;
    //        }
    //
    //        String logContent = logFormatter.logRequestComplete(log);
    //        logger.info(logContent);
    //    }
    //
    //    @Override
    //    public void processRequestError(RequestLog log) {
    //        if (properties == null || !properties.isEnableConsoleLogging()) {
    //            return;
    //        }
    //
    //        String logContent = logFormatter.logRequestError(log);
    //        logger.error(logContent);
    //    }
    //}
}
