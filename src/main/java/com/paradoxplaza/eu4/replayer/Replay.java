package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.DateGenerator.IDateListener;
import com.paradoxplaza.eu4.replayer.EventProcessor.IEventListener;
import com.paradoxplaza.eu4.replayer.events.Event;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.Utils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Class representing the replay itself with no GUI.
 */
public class Replay {

    /** Path appended to user's home directory to get default "game" folder on Windows. */
    static public final String WIN_DIR = "/Documents/Paradox Interactive/Europa Universalis IV";

    /** Path appended to user's home directory to get default game folder on Linux. */
    static public final String LINUX_DIR = "/.paradoxinteractive/Europa Universalis IV";

    /** Default base direcotry containing settings, mods, saves, etc. */
    static public final String DEFAULT_BASE_DIR =
            System.getProperty("user.home", "") +
            (
                //Windows
                System.getProperty("os.name", "").startsWith("Windows") ? WIN_DIR :
                //Linux
                System.getProperty("os.name", "").startsWith("Linux") ? LINUX_DIR :
                //Mac?
                ""
            );

    /** Default save game directory. */
    static public final String DEFAULT_SAVE_DIR = DEFAULT_BASE_DIR + "/save games";

    /** Default mod directory. */
    static public final String DEFAULT_MOD_DIR = DEFAULT_BASE_DIR + "/mod";

    /** Default date listener that does nothing. */
    static protected final IDateListener defaultDateListener = new IDateListener() {
        @Override
        public void update(Date date, double progress) { }
    };

    /** Possible directions of replaying. */
    enum Direction { FORWARD, BACKWARD }

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

    /** Flag indication whether borders should be drawn. */
    public boolean drawBorders;

    /** Flag indicating that subject nations should be rendered as part of their overlords. */
    public boolean subjectsAsOverlords = false;

    /** Flag whether the Random New World feature is on. */
    public final boolean rnw;

    /** Current direction of replaying. */
    Direction direction = null;

    /** Generates dates for replaying dates. */
    public DateGenerator dateGenerator;

    /** Loaded save game to be replayed. */
    public SaveGame saveGame;

    /** Handles files with respect to mods. */
    public FileManager fileManager;

    /** Date listener. */
    protected IDateListener dateListener = defaultDateListener;

    /** Event processor. */
    public EventProcessor eventProcessor = new EventProcessor(this);

    /** Tag -> country mapping. */
    public Map<String, CountryInfo> countries = new HashMap<>();

    /** Culture name -> color mapping. */
    public final Map<String, Integer> cultures = new HashMap<>();

    /** ID -> province mapping. */
    public final Map<String, ProvinceInfo> provinces = new HashMap<>();

    /** Religion name -> color mapping. */
    public final Map<String, Integer> religions = new HashMap<>();

    /** Province color -> province mapping. */
    public final Map<Integer, ProvinceInfo> colors = new HashMap<>();

    /** Set of points constituting borders. */
    public final Set<Integer> borders = new HashSet<>();

    /** Colonial region name -> colonial name mapping. */
    public final Map<String, ColRegionInfo> colRegions = new HashMap<>();

    /** Holds information from defines.lua. */
    public final DefinesInfo defines = new DefinesInfo();

    /** Replayer settings. */
    public final Properties settings;

    /**
     * Only constructor.
     * @param settings application settings
     */
    public Replay(final Properties settings) {
        this.settings = settings;
        fileManager = new FileManager(settings);
        rnw = Utils.isRNW(settings);

        notableEvents.addAll(Arrays.asList(settings.getProperty("events", "").split(";")));

        seaColor = Utils.toColor(
                Integer.parseInt(settings.getProperty("sea.color.red", "0")),
                Integer.parseInt(settings.getProperty("sea.color.green", "0")),
                Integer.parseInt(settings.getProperty("sea.color.blue", "255")));
        landColor = Utils.toColor(
                Integer.parseInt(settings.getProperty("land.color.red", "150")),
                Integer.parseInt(settings.getProperty("land.color.green", "150")),
                Integer.parseInt(settings.getProperty("land.color.blue", "150")));
        borderColor = Utils.toColor(
                Integer.parseInt(settings.getProperty("border.color.red", "0")),
                Integer.parseInt(settings.getProperty("border.color.green", "0")),
                Integer.parseInt(settings.getProperty("border.color.blue", "0")));

        focusTag = settings.getProperty("focus", "");
        focusing = !focusTag.isEmpty();

        subjectsAsOverlords = settings.getProperty("subjects.as.overlord", "false").equals("true");
    }

    /**
     * Returns current date.
     * @return current date
     */
    public Date getDate() {
        return dateGenerator.getDate();
    }

    /**
     * Returns {@link #dateListener}.
     * @return the dateListener
     */
    public IDateListener getDateListener() {
        return dateListener;
    }

    /**
     * Sets {@link #dateListener}.
     * @param dateListener the dateListener to set
     */
    public void setDateListener(final IDateListener dateListener) {
        this.dateListener = dateListener;
        //TODO hack
        dateGenerator.setListener(new DateListener());
    }

    /**
     * Sets {@link #dateListener} to {@link #defaultDateListener}.
     */
    public void resetDateListener() {
        this.dateListener = defaultDateListener;
    }

    /**
     * Returns event listener.
     * @return event listener
     */
    public IEventListener getEventListener() {
        return eventProcessor.getListener();
    }

    /**
     * Sets event listener.
     * @param eventListener new event listener
     */
    public void setEventListener(final IEventListener eventListener) {
        eventProcessor.setListener(eventListener);
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

    public boolean isAtEnd() {
        return !dateGenerator.hasNext();
    }

    public void next() {
        direction = Direction.FORWARD;
        dateGenerator.next();
    }

    public void nextTo(final Date target) {
        direction = Direction.FORWARD;
        while (dateGenerator.hasNext() && !target.equals(dateGenerator.date)) {
            dateGenerator.next();
        }
    }

    public boolean isAtStart() {
        return !dateGenerator.hasPrev();
    }

    public void prev() {
        direction = Direction.BACKWARD;
        dateGenerator.prev();
    }

    public void prevTo(final Date target) {
        direction = Direction.BACKWARD;
        while (dateGenerator.hasPrev() && !target.equals(dateGenerator.date)) {
            dateGenerator.prev();
        }
    }

    public void skipTo(final Date date) {
        if (dateGenerator.getDate().compareTo(date) < 0) {
            nextTo(date);
        } else {
            prevTo(date);
        }
    }

    public void skip(final Date.Period period, final int delta) {
        final Date curr = dateGenerator.getDate();
        final Date target = curr.skip(period, delta);
        if (delta > 0) {
            nextTo(target);
        } else {
            prevTo(target);
        }
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

    /**
     * Listens to changes of {@link #dateGenerator} and initiates event processing.
     */
    class DateListener implements IDateListener {

        @Override
        public void update(final Date date, final double progress) {
            assert direction != null : l10n("replay.direction.unknown");
            switch (direction) {
                case FORWARD:
                    final List<Event> forwardsEvents = saveGame.timeline.get(date);
                    eventProcessor.processEvents(date, forwardsEvents);
                    break;
                case BACKWARD:
                    final Date backwardDate = date.next();
                    final List<Event> backwardEvents = saveGame.timeline.get(backwardDate);
                    eventProcessor.unprocessEvents(backwardDate, backwardEvents);
                    break;
                default:
                    assert false : l10n("replay.direction.unknown");
            }
            dateListener.update(date, progress);
        }
    }
}
