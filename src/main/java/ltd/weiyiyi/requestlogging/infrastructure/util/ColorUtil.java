package ltd.weiyiyi.requestlogging.infrastructure.util;

/**
 * 颜色工具类
 * 用于处理ANSI颜色转义序列
 *
 * @author weihan
 */
public class ColorUtil {
    private static final String ANSI_RESET = "\u001B[0m";

    /**
     * 将文本使用指定的前景色和背景色着色
     *
     * @param text 要着色的文本
     * @param foregroundColor 前景色RGB值，格式为(R,G,B)
     * @param backgroundColor 背景色RGB值，格式为(R,G,B)
     * @return 着色后的文本
     */
    public static String colorize(String text, String foregroundColor, String backgroundColor) {
        StringBuilder code = new StringBuilder();
        
        if (foregroundColor != null) {
            code.append(parseColor(foregroundColor, true));
        }
        if (backgroundColor != null) {
            if (!code.isEmpty()) {
                code.append(";");
            }
            code.append(parseColor(backgroundColor, false));
        }
        
        if (!code.isEmpty()) {
            return "\u001B[" + code + "m" + text + ANSI_RESET;
        }
        return text;
    }

    /**
     * 解析RGB颜色值为ANSI代码
     *
     * @param color RGB颜色值，格式为(R,G,B)
     * @param isForeground 是否为前景色
     * @return ANSI代码
     */
    private static String parseColor(String color, boolean isForeground) {
        if (color == null || color.isEmpty()) {
            return "";
        }

        // 解析RGB值
        String rgb = color.replaceAll("[()]", "");
        String[] components = rgb.split(",");
        if (components.length != 3) {
            return "";
        }

        try {
            int r = Integer.parseInt(components[0].trim());
            int g = Integer.parseInt(components[1].trim());
            int b = Integer.parseInt(components[2].trim());

            // 使用24位真彩色
            return String.format("%d;2;%d;%d;%d", isForeground ? 38 : 48, r, g, b);
        } catch (NumberFormatException e) {
            return "";
        }
    }
} 