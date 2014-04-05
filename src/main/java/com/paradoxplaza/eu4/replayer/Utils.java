package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.localization.Localizator;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import java.util.Locale;
import java.util.Properties;

/**
 * A few methods useful throughout the code.
 */
public class Utils {

    /** Path to default property file. The file should be inside the jar. */
    public static final String DEFAULT_JAR_PROPERTIES = "replayer.defprops";

    /** Color used in EU4 mapmode screenshots for seas. */
    static final public int SEA_COLOR = toColor(68, 107, 163);

    /** Color used in EU4 mapmode screenshots for wastelands. */
    static final public int WASTELAND_COLOR = toColor(94, 94, 94);

    /**
     * Loads and returns default properties from {@link #DEFAULT_JAR_PROPERTIES}.
     * @return default properties
     */
    static public Properties loadDefaultJarProperties() {
        System.out.printf(l10n("app.properties.jar"));
        final Properties res = new Properties();
        try {
            res.load(Utils.class.getClassLoader().getResourceAsStream(DEFAULT_JAR_PROPERTIES));
        } catch(Exception e) {
            //someone messed with our properties!
            e.printStackTrace();
        }
        return res;
    }

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
     * Resets default locale and {@link Localizator} if langCode is not null.
     * @param langCode new default locale language code
     */
    static public void resetDefaultLocale(final String langCode) {
        if (langCode != null) {
            Locale.setDefault(new Locale(langCode));
            Localizator.getInstance().reloadResourceBundle();
        }
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

    /**
     * Returns whether the Random New World feature is on.
     * @param settings application settings
     * @return true if RNW is on, false otherwise
     */
    static public boolean isRNW(final Properties settings) {
        final String rnwMap = settings.getProperty("rnw.map");
        return rnwMap != null  && !rnwMap.isEmpty();
    }

    /** Utility classes need no constructors. */
    private Utils() { }
}
