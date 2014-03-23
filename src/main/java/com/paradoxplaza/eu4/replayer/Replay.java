package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.utils.ColorUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Class representing the replay itself with no GUI.
 */
public class Replay {

    /** Set of currently notable events. */
    public final Set<String> notableEvents = new HashSet<>();

    /** Color used to display sea and lakes. */
    public final int seaColor;

    /** Color to display no man's land. */
    public final int landColor;

    /** Color to display province bordes. */
    public final int borderColor;

    /** Buffer width. */
    public int bufferWidth;

    /** Buffer height. */
    public int bufferHeight;

    /** Buffer with political map. */
    public int[] politicalBuffer;

    /** Buffer with religious map. */
    public int[] religiousBuffer;

    /** Buffer with cultural map. */
    public int[] culturalBuffer;

    /** Buffer with technology map with separate color representation of tech branches. */
    public int[] technologySeparateBuffer;

    /** Buffer with technology map with separate color representation of tech branches. */
    public int[] technologyCombinedBuffer;

    /**
     * Flag indicating whether {@link #focusTag} is be used.
     * If true, focusTag is not empty, but contains country tag in focus.
     */
    public boolean focusing = false;

    /** Tag of state in focus. Never null. */
    public String focusTag = "";

    /** Flag indicating that subject nations should be rendered as part of their overlords. */
    public boolean subjectsAsOverlords = false;

    /** Tag -> country mapping. */
    public Map<String, CountryInfo> countries = new HashMap<>();

    /** Culture name -> color mapping. */
    public Map<String, Integer> cultures = new HashMap<>();

    /** ID -> province mapping. */
    public Map<String, ProvinceInfo> provinces = new HashMap<>();

    /** Religion name -> color mapping. */
    public Map<String, Integer> religions = new HashMap<>();

    /** Replayer settings. */
    public final Properties settings;

    /**
     * Only constructor.
     * @param settings application settings
     */
    public Replay(final Properties settings) {
        this.settings = settings;

        notableEvents.addAll(Arrays.asList(settings.getProperty("events", "").split(";")));

        seaColor = ColorUtils.toColor(
                Integer.parseInt(settings.getProperty("sea.color.red", "0")),
                Integer.parseInt(settings.getProperty("sea.color.green", "0")),
                Integer.parseInt(settings.getProperty("sea.color.blue", "255")));
        landColor = ColorUtils.toColor(
                Integer.parseInt(settings.getProperty("land.color.red", "150")),
                Integer.parseInt(settings.getProperty("land.color.green", "150")),
                Integer.parseInt(settings.getProperty("land.color.blue", "150")));
        borderColor = ColorUtils.toColor(
                Integer.parseInt(settings.getProperty("border.color.red", "0")),
                Integer.parseInt(settings.getProperty("border.color.green", "0")),
                Integer.parseInt(settings.getProperty("border.color.blue", "0")));

        focusTag = settings.getProperty("focus", "");
        focusing = !focusTag.isEmpty();

        subjectsAsOverlords = settings.getProperty("subjects.as.overlord", "false").equals("true");
    }

    public void initBuffers(final int width, final int height) {
        bufferWidth = width;
        bufferHeight = height;
        politicalBuffer = new int[width*height];
        religiousBuffer = new int[width*height];
        culturalBuffer = new int[width*height];
        technologySeparateBuffer = new int[width*height];
        technologyCombinedBuffer = new int[width*height];
    }

    /**
     * Reset the state of Replay to load a new SaveGame.
     */
    public void reset() {
        for (CountryInfo ci : countries.values()) {
            ci.reset();
        }
        for (ProvinceInfo pi : provinces.values()) {
            pi.reset();
        }
        focusTag = settings.getProperty("focus", ""); //this needs to be reset as tag changes are followed during replaying
        focusing = !focusTag.isEmpty();
    }
}
