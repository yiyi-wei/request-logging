package ltd.weiyiyi.requestlogging.infrastructure.color;

/**
 * 颜色处理工厂类
 * 负责将颜色值转换为ANSI转义序列
 *
 * @author weihan
 */
public class ColorProcessorFactory {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    /**
     * 处理颜色
     *
     * @param colorPair 颜色对
     * @param text 文本
     * @return 处理后的文本
     */
    public static String processColor(ColorPair colorPair, String text) {
        if (colorPair == null || text == null) {
            return text;
        }

        String foregroundColor = getAnsiCode(colorPair.getForegroundColor());
        return foregroundColor + text + ANSI_RESET;
    }

    private static String getAnsiCode(String color) {
        if (color == null) {
            return "";
        }

        return switch (color.toLowerCase()) {
            case "black" -> ANSI_BLACK;
            case "red" -> ANSI_RED;
            case "green" -> ANSI_GREEN;
            case "yellow" -> ANSI_YELLOW;
            case "blue" -> ANSI_BLUE;
            case "purple" -> ANSI_PURPLE;
            case "cyan" -> ANSI_CYAN;
            case "white" -> ANSI_WHITE;
            default -> "";
        };
    }
} 