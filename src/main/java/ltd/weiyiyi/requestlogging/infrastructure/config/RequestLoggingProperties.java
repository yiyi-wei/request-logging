package ltd.weiyiyi.requestlogging.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * 请求日志配置属性
 * 
 * @author weihan
 */
@ConfigurationProperties(prefix = "request-logging")
public class RequestLoggingProperties {

    /**
     * 是否启用请求日志
     */
    private boolean enabled = true;

    /**
     * 日志级别
     */
    private String logLevel = "INFO";

    /**
     * 排除的参数名称列表
     */
    private String[] excludeParameters = {};

    /**
     * 是否记录响应内容
     */
    private boolean logResponse = true;

    /**
     * 响应内容最大长度
     */
    private int responseMaxLength = 1000;

    /**
     * 是否启用异步日志
     */
    private boolean asyncLogging = false;

    /**
     * 采样率 (0.0-1.0)
     */
    private double samplingRate = 1.0;

    /**
     * 是否记录请求头
     */
    private boolean logHeaders = true;

    /**
     * 排除的请求头名称列表
     */
    private String[] excludeHeaders = {"Authorization", "Cookie"};

    /**
     * 是否记录请求体
     */
    private boolean logRequestBody = true;

    /**
     * 请求体最大长度
     */
    private int requestBodyMaxLength = 1000;

    /**
     * 是否启用追踪ID
     */
    private boolean enableTraceId = true;

    /**
     * 追踪ID请求头名称
     */
    private String traceIdHeader = "X-Trace-Id";

    /**
     * 是否启用颜色输出
     */
    private boolean enableColorOutput = true;

    /**
     * 是否启用美化打印
     */
    private boolean prettyPrint = true;

    /**
     * 时间戳格式
     */
    private String timestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 是否显示时间戳
     */
    private boolean showTimestamp = true;

    /**
     * 是否显示分隔符
     */
    private boolean showSeparator = true;

    /**
     * 分隔符
     */
    private String separator = "===================";

    /**
     * 请求开始标记
     */
    private String requestStartFlag = ">>> Request Start >>>";

    /**
     * 请求结束标记
     */
    private String requestEndFlag = "<<< Request End <<<";

    /**
     * INFO级别前景色 - 浅绿色
     */
    private String infoForegroundColor = "(144,238,144)";

    /**
     * INFO级别背景色 - 无背景
     */
    private String infoBackgroundColor = null;

    /**
     * WARN级别前景色 - 橙色
     */
    private String warnForegroundColor = "(255,165,0)";

    /**
     * WARN级别背景色 - 深灰色
     */
    private String warnBackgroundColor = "(40,40,40)";

    /**
     * ERROR级别前景色 - 亮红色
     */
    private String errorForegroundColor = "(255,69,0)";

    /**
     * ERROR级别背景色 - 深红色
     */
    private String errorBackgroundColor = "(139,0,0)";

    /**
     * DEBUG级别前景色 - 天蓝色
     */
    private String debugForegroundColor = "(135,206,235)";

    /**
     * DEBUG级别背景色 - 无背景
     */
    private String debugBackgroundColor = null;

    /**
     * TRACE级别前景色 - 灰色
     */
    private String traceForegroundColor = "(169,169,169)";

    /**
     * TRACE级别背景色 - 无背景
     */
    private String traceBackgroundColor = null;

    /**
     * 请求开始标记前景色 - 亮绿色
     */
    private String requestStartForegroundColor = "(50,205,50)";

    /**
     * 请求开始标记背景色 - 深绿色
     */
    private String requestStartBackgroundColor = "(0,100,0)";

    /**
     * 请求结束标记前景色 - 亮蓝色
     */
    private String requestEndForegroundColor = "(30,144,255)";

    /**
     * 请求结束标记背景色 - 深蓝色
     */
    private String requestEndBackgroundColor = "(0,0,139)";

    /**
     * 重置颜色
     */
    private String resetColor = "\u001B[0m";

    /**
     * 是否启用文件日志
     */
    private boolean enableFileLogging = true;

    /**
     * 日志文件基础目录
     */
    private String logFileBaseDir = "data";

    /**
     * 日志文件策略：daily/size
     */
    private String logFileStrategy = "daily";

    /**
     * 单个文件大小限制（size策略）
     */
    private String maxFileSize = "100MB";

    /**
     * 日志保留天数（daily策略）
     */
    private int maxHistory = 30;

    /**
     * 是否启用控制台日志
     */
    private boolean enableConsoleLogging = true;

    /**
     * 需要添加 TraceID 的包路径列表
     */
    private List<String> tracePackages = Collections.emptyList();

    /**
     * 是否启用 MDC 日志追踪
     */
    private boolean enableMdcTrace = true;

    /**
     * MDC 中 TraceID 的 key
     */
    private String traceIdKey = "traceId";

    /**
     * 日志文件路径
     */
    private String logFilePath = "data";

    /**
     * 日志文件滚动策略
     */
    private String rollingStrategy = "daily";

    /**
     * 请求开始颜色
     */
    private String requestStartColor = "green";

    /**
     * 请求结束颜色
     */
    private String requestEndColor = "blue";

    /**
     * 错误颜色
     */
    private String errorColor = "red";

    /**
     * JSON缩进空格数
     */
    private int jsonIndent = 2;

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String[] getExcludeParameters() {
        return excludeParameters;
    }

    public void setExcludeParameters(String[] excludeParameters) {
        this.excludeParameters = excludeParameters;
    }

    public boolean isLogResponse() {
        return logResponse;
    }

    public void setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
    }

    public int getResponseMaxLength() {
        return responseMaxLength;
    }

    public void setResponseMaxLength(int responseMaxLength) {
        this.responseMaxLength = responseMaxLength;
    }

    public boolean isAsyncLogging() {
        return asyncLogging;
    }

    public void setAsyncLogging(boolean asyncLogging) {
        this.asyncLogging = asyncLogging;
    }

    public double getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(double samplingRate) {
        this.samplingRate = samplingRate;
    }

    public boolean isLogHeaders() {
        return logHeaders;
    }

    public void setLogHeaders(boolean logHeaders) {
        this.logHeaders = logHeaders;
    }

    public String[] getExcludeHeaders() {
        return excludeHeaders;
    }

    public void setExcludeHeaders(String[] excludeHeaders) {
        this.excludeHeaders = excludeHeaders;
    }

    public boolean isLogRequestBody() {
        return logRequestBody;
    }

    public void setLogRequestBody(boolean logRequestBody) {
        this.logRequestBody = logRequestBody;
    }

    public int getRequestBodyMaxLength() {
        return requestBodyMaxLength;
    }

    public void setRequestBodyMaxLength(int requestBodyMaxLength) {
        this.requestBodyMaxLength = requestBodyMaxLength;
    }

    public boolean isEnableTraceId() {
        return enableTraceId;
    }

    public void setEnableTraceId(boolean enableTraceId) {
        this.enableTraceId = enableTraceId;
    }

    public String getTraceIdHeader() {
        return traceIdHeader;
    }

    public void setTraceIdHeader(String traceIdHeader) {
        this.traceIdHeader = traceIdHeader;
    }

    public boolean isEnableColorOutput() {
        return enableColorOutput;
    }

    public void setEnableColorOutput(boolean enableColorOutput) {
        this.enableColorOutput = enableColorOutput;
    }

    public String getInfoForegroundColor() {
        return infoForegroundColor;
    }

    public void setInfoForegroundColor(String infoForegroundColor) {
        this.infoForegroundColor = infoForegroundColor;
    }

    public String getInfoBackgroundColor() {
        return infoBackgroundColor;
    }

    public void setInfoBackgroundColor(String infoBackgroundColor) {
        this.infoBackgroundColor = infoBackgroundColor;
    }

    public String getWarnForegroundColor() {
        return warnForegroundColor;
    }

    public void setWarnForegroundColor(String warnForegroundColor) {
        this.warnForegroundColor = warnForegroundColor;
    }

    public String getWarnBackgroundColor() {
        return warnBackgroundColor;
    }

    public void setWarnBackgroundColor(String warnBackgroundColor) {
        this.warnBackgroundColor = warnBackgroundColor;
    }

    public String getErrorForegroundColor() {
        return errorForegroundColor;
    }

    public void setErrorForegroundColor(String errorForegroundColor) {
        this.errorForegroundColor = errorForegroundColor;
    }

    public String getErrorBackgroundColor() {
        return errorBackgroundColor;
    }

    public void setErrorBackgroundColor(String errorBackgroundColor) {
        this.errorBackgroundColor = errorBackgroundColor;
    }

    public String getDebugForegroundColor() {
        return debugForegroundColor;
    }

    public void setDebugForegroundColor(String debugForegroundColor) {
        this.debugForegroundColor = debugForegroundColor;
    }

    public String getDebugBackgroundColor() {
        return debugBackgroundColor;
    }

    public void setDebugBackgroundColor(String debugBackgroundColor) {
        this.debugBackgroundColor = debugBackgroundColor;
    }

    public String getTraceForegroundColor() {
        return traceForegroundColor;
    }

    public void setTraceForegroundColor(String traceForegroundColor) {
        this.traceForegroundColor = traceForegroundColor;
    }

    public String getTraceBackgroundColor() {
        return traceBackgroundColor;
    }

    public void setTraceBackgroundColor(String traceBackgroundColor) {
        this.traceBackgroundColor = traceBackgroundColor;
    }

    public String getRequestStartForegroundColor() {
        return requestStartForegroundColor;
    }

    public void setRequestStartForegroundColor(String requestStartForegroundColor) {
        this.requestStartForegroundColor = requestStartForegroundColor;
    }

    public String getRequestStartBackgroundColor() {
        return requestStartBackgroundColor;
    }

    public void setRequestStartBackgroundColor(String requestStartBackgroundColor) {
        this.requestStartBackgroundColor = requestStartBackgroundColor;
    }

    public String getRequestEndForegroundColor() {
        return requestEndForegroundColor;
    }

    public void setRequestEndForegroundColor(String requestEndForegroundColor) {
        this.requestEndForegroundColor = requestEndForegroundColor;
    }

    public String getRequestEndBackgroundColor() {
        return requestEndBackgroundColor;
    }

    public void setRequestEndBackgroundColor(String requestEndBackgroundColor) {
        this.requestEndBackgroundColor = requestEndBackgroundColor;
    }

    public String getResetColor() {
        return resetColor;
    }

    public void setResetColor(String resetColor) {
        this.resetColor = resetColor;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public boolean isShowTimestamp() {
        return showTimestamp;
    }

    public void setShowTimestamp(boolean showTimestamp) {
        this.showTimestamp = showTimestamp;
    }

    public boolean isShowSeparator() {
        return showSeparator;
    }

    public void setShowSeparator(boolean showSeparator) {
        this.showSeparator = showSeparator;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getRequestStartFlag() {
        return requestStartFlag;
    }

    public void setRequestStartFlag(String requestStartFlag) {
        this.requestStartFlag = requestStartFlag;
    }

    public String getRequestEndFlag() {
        return requestEndFlag;
    }

    public void setRequestEndFlag(String requestEndFlag) {
        this.requestEndFlag = requestEndFlag;
    }

    public boolean isEnableFileLogging() {
        return enableFileLogging;
    }

    public void setEnableFileLogging(boolean enableFileLogging) {
        this.enableFileLogging = enableFileLogging;
    }

    public String getLogFileBaseDir() {
        return logFileBaseDir;
    }

    public void setLogFileBaseDir(String logFileBaseDir) {
        this.logFileBaseDir = logFileBaseDir;
    }

    public String getLogFileStrategy() {
        return logFileStrategy;
    }

    public void setLogFileStrategy(String logFileStrategy) {
        this.logFileStrategy = logFileStrategy;
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public boolean isEnableConsoleLogging() {
        return enableConsoleLogging;
    }

    public void setEnableConsoleLogging(boolean enableConsoleLogging) {
        this.enableConsoleLogging = enableConsoleLogging;
    }

    public List<String> getTracePackages() {
        return tracePackages;
    }

    public void setTracePackages(List<String> tracePackages) {
        this.tracePackages = tracePackages;
    }

    public boolean isEnableMdcTrace() {
        return enableMdcTrace;
    }

    public void setEnableMdcTrace(boolean enableMdcTrace) {
        this.enableMdcTrace = enableMdcTrace;
    }

    public String getTraceIdKey() {
        return traceIdKey;
    }

    public void setTraceIdKey(String traceIdKey) {
        this.traceIdKey = traceIdKey;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getRollingStrategy() {
        return rollingStrategy;
    }

    public void setRollingStrategy(String rollingStrategy) {
        this.rollingStrategy = rollingStrategy;
    }

    public String getRequestStartColor() {
        return requestStartColor;
    }

    public void setRequestStartColor(String requestStartColor) {
        this.requestStartColor = requestStartColor;
    }

    public String getRequestEndColor() {
        return requestEndColor;
    }

    public void setRequestEndColor(String requestEndColor) {
        this.requestEndColor = requestEndColor;
    }

    public String getErrorColor() {
        return errorColor;
    }

    public void setErrorColor(String errorColor) {
        this.errorColor = errorColor;
    }

    public int getJsonIndent() {
        return jsonIndent;
    }

    public void setJsonIndent(int jsonIndent) {
        this.jsonIndent = jsonIndent;
    }
} 