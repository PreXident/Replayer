package com.paradoxplaza.eu4.replayer;

/**
 * A few methods for working with colors.
 */
public class ColorUtils {

    /** Color used in EU4 mapmode screenshots for seas. */
    static final public int SEA_COLOR = toColor(68, 107, 163);

    /** Color used in EU4 mapmode screenshots for wastelands. */
    static final public int WASTELAND_COLOR = toColor(94, 94, 94);

    /**
     * Creates a string "red green blue" out of int color.
     * The individual colors are integers 0-255.
     * @param color input color
     * @param separator delimiter to use
     * @return string "REDseparatorGREENseparator"
     */
    static public String colorToString255(final int color, final String separator) {
        final int red = (color & 0x00FF0000) >> 16;
        final int green = (color & 0x0000FF00) >> 8;
        final int blue = color & 0x000000FF;
        return red + separator + green + separator + blue;
    }

    /**
     * Creates a string "red green blue" out of int color.
     * The individual colors are floats 0-1.
     * @param color input color
     * @param separator delimiter to use
     * @return string "REDseparatorGREENseparator"
     */
    static public String colorToStringFloat(final int color, final String separator) {
        final double red = ((color & 0x00FF0000) >> 16) / 255.0;
        final double green = ((color & 0x0000FF00) >> 8) / 255.0;
        final double blue = (color & 0x000000FF) / 255.0;
        return red + separator + green + separator + blue;
    }

    /**
     * R, G, B to argb.
     * @param red
     * @param green
     * @param blue
     * @return argb
     */
    static public int toColor(final int red, final int green, final int blue) {
        return 255 << 24 | red << 16 | green << 8 | blue;
    }

    /** Utility classes need no constructors. */
    private ColorUtils() { }
}
