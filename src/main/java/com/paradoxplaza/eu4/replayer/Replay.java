package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.DateGenerator.IDateListener;
import com.paradoxplaza.eu4.replayer.EventProcessor.IEventListener;
import com.paradoxplaza.eu4.replayer.events.Controller;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.events.SimpleProvinceEvent;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.savegame.BatchSaveGameParser;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import com.paradoxplaza.eu4.replayer.utils.Utils;
import java.io.File;
import java.util.ArrayList;
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

    /** Buffer bufferWidth. */
    public int bufferWidth;

    /** Buffer bufferHeight. */
    public int bufferHeight;

    /** Provinces.bmp buffer. */
    public int[] provincesBmpBuffer;

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
        provincesBmpBuffer = new int[width*height];
        politicalBuffer = new int[width*height];
        religiousBuffer = new int[width*height];
        culturalBuffer = new int[width*height];
        technologySeparateBuffer = new int[width*height];
        technologyCombinedBuffer = new int[width*height];
    }

    /**
     * Returns whether the replay is at its end.
     * @return true if the replay is at its end, false otherwise
     */
    public boolean isAtEnd() {
        return !dateGenerator.hasNext();
    }

    /**
     * Returns whether the replay is at its start.
     * @return true if the replay is at its start, false otherwise
     */
    public boolean isAtStart() {
        return !dateGenerator.hasPrev();
    }

    /**
     * Loads a batch of saves.
     * @param bridge object to notify about progress
     * @param initializationListener gets notified when progressing towards start date
     * @param files save game files
     */
    public void loadSaves(final ITaskBridge<SaveGame> bridge,
            final IEventListener initializationListener, final List<File> files) {
        saveGame = new SaveGame();
        reset();

        //map initialization
        bridge.updateTitle(l10n("replay.map.init"));
        final long size = bufferHeight * bufferWidth;
        for (int y = 0; y < bufferHeight; ++y) {
            for (int x = 0; x < bufferWidth; ++x) {
                final int pos = y * bufferWidth + x;
                final int color = provincesBmpBuffer[pos];
                int finalColor;
                final ProvinceInfo province = colors.get(color);
                if (province != null && province.isSea) {
                    finalColor = seaColor;
                } else if (borders.contains(pos)) {
                    finalColor = borderColor;
                } else {
                    finalColor = landColor;
                }
                politicalBuffer[pos] = finalColor;
                religiousBuffer[pos] = finalColor;
                culturalBuffer[pos] = finalColor;
                technologySeparateBuffer[pos] = finalColor;
                technologyCombinedBuffer[pos] = finalColor;
                bridge.updateProgress(pos, size);
            }
        }

        //parsing saves
        final BatchSaveGameParser parser = new BatchSaveGameParser(rnw, saveGame, files, bridge);
        parser.run();

        //process dynamic stuff from saves
        for (Map.Entry<String, Integer> c : saveGame.dynamicCountriesColors.entrySet()) {
            countries.put(c.getKey(), new CountryInfo(c.getKey(), c.getValue()));
        }
        for (Map.Entry<String, Date> change : saveGame.tagChanges.entrySet()) {
            final String tag = change.getKey();
            final CountryInfo country = countries.get(tag);
            if (country != null) {
                country.expectingTagChange = change.getValue();
            } else {
                System.err.printf(l10n("replay.tagchange.unknowntag"), tag);
            }
        }

        //world initialization
        dateGenerator = new DateGenerator(saveGame.startDate, saveGame.date);
        bridge.updateTitle(l10n("replay.world.init"));
        final IEventListener listenerBackup = getEventListener();
        setEventListener(initializationListener);
        eventProcessor.processEvents(null, new ProgressIterable<>(saveGame.timeline.get(null), bridge));
        //
        bridge.updateTitle(l10n("replay.progressing"));
        final Date maxDate = saveGame.startDate;
        Date date = new Date(settings.getProperty("init.start", "1300.1.1"));
        int day = 0;
        final int distance = Date.calculateDistance(date, saveGame.startDate) + 1;
        while (date.compareTo(maxDate) <= 0) {
            final List<Event> events = saveGame.timeline.get(date);
            eventProcessor.processEvents(date, events);
            bridge.updateProgress(++day, distance);
            date = date.next();
        }
        //fix colonial nations
        if (settings.getProperty("fix.colonials", "true").equals("true")) {
            bridge.updateTitle(l10n("replay.colonials.fix"));
            int colRegCounter = 0;
            final Date magicalDate = new Date(settings.getProperty("fix.colonials.date", "1444.11.11"));
            for (ColRegionInfo colreg : colRegions.values()) {
                bridge.updateProgress(colRegCounter++, colRegions.size());
                //count colonies for countries
                final Map<String, List<ProvinceInfo>> colonies = new HashMap<>();
                for (String id : colreg.provinces) {
                    final ProvinceInfo province = provinces.get(id);
                    if (province.owner == null) {
                        continue;
                    }
                    List<ProvinceInfo> list = colonies.get(province.owner);
                    if (list == null) {
                        list = new ArrayList<>();
                        colonies.put(province.owner, list);
                    }
                    list.add(province);
                }
                //set owner and controller for colonial nations' provinces
                for (List<ProvinceInfo> provs : colonies.values()) {
                    if (provs.size() > defines.MAX_CROWN_COLONIES) {
                        for (ProvinceInfo prov : provs) {
                            List<Pair<Date, Event>> events = new ArrayList<>(prov.events);
                            for (Pair<Date, Event> p : events) {
                                if (magicalDate.equals(p.getFirst())
                                        && (p.getSecond() instanceof Owner
                                            || (p.getSecond() instanceof Controller && prov.owner.equals(prov.controller)))) {
                                    SimpleProvinceEvent e = (SimpleProvinceEvent) p.getSecond();
                                    if (e.value.matches("C..")) {
                                        eventProcessor.processEvents(p.getFirst(), Arrays.asList(e));
                                    }
                                }
                                if (magicalDate.compareTo(p.getFirst()) < 0) {
                                    break;
                                }
                            }
                            prov.events = events; //processor added the reprocessed events to the list, so we need to restore backup
                        }
                    }
                }
            }
        }
        setEventListener(listenerBackup);
        bridge.updateValue(saveGame);
    }

    /**
     * Jumps forward in replay.
     * @param target target date
     */
    protected void nextTo(final Date target) {
        direction = Direction.FORWARD;
        while (dateGenerator.hasNext() && !target.equals(dateGenerator.date)) {
            dateGenerator.next();
        }
    }

    /**
     * Jumps backwards in replay.
     * @param target target date
     */
    protected void prevTo(final Date target) {
        direction = Direction.BACKWARD;
        while (dateGenerator.hasPrev() && !target.equals(dateGenerator.date)) {
            dateGenerator.prev();
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
     * Skips to the target date.
     * @param date target date
     */
    public void skipTo(final Date date) {
        if (dateGenerator.getDate().compareTo(date) < 0) {
            nextTo(date);
        } else {
            prevTo(date);
        }
    }

    /**
     * Shifts the date by a specific delta.
     * @param period delta unit
     * @param delta time delta
     */
    public void skip(final Date.Period period, final int delta) {
        final Date curr = dateGenerator.getDate();
        final Date target = curr.skip(period, delta);
        skipTo(target);
    }

    /**
     * Listens to changes of {@link #dateGenerator} and initiates event processing.
     * Then informs {@link #dateListener}.
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
