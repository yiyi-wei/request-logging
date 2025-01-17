package ltd.weiyiyi.requestlogging.infrastructure.color;

/**
 * 颜色对，包含前景色和背景色
 *
 * @author weihan
 */
public class ColorPair {
    private final String foregroundColor;
    private final String backgroundColor;

    public ColorPair(String foregroundColor, String backgroundColor) {
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    public String getForegroundColor() {
        return foregroundColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }
} 