package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.DateGenerator.IDateListener;
import com.paradoxplaza.eu4.replayer.EventProcessor.IEventListener;
import com.paradoxplaza.eu4.replayer.events.Controller;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.events.SimpleProvinceEvent;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.climate.ClimateParser;
import com.paradoxplaza.eu4.replayer.parser.colregion.ColRegionParser;
import com.paradoxplaza.eu4.replayer.parser.country.CountryParser;
import com.paradoxplaza.eu4.replayer.parser.culture.CulturesParser;
import com.paradoxplaza.eu4.replayer.parser.defaultmap.DefaultMapParser;
import com.paradoxplaza.eu4.replayer.parser.defines.DefinesParser;
import com.paradoxplaza.eu4.replayer.parser.religion.ReligionsParser;
import com.paradoxplaza.eu4.replayer.parser.savegame.BatchSaveGameParser;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import com.paradoxplaza.eu4.replayer.utils.Ref;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;

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

    /**
     * Returns color on specified coordinates from {@link #provincesBmpBuffer}.
     * @param x x-coord
     * @param y y-coord
     * @return color on specified coordinates
     */
    public int getProvinceColor(int x, int y) {
        final int pos = y * bufferWidth + x;
        return provincesBmpBuffer[pos];
    }

    public void initBuffers(final int width, final int height) {
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
     * Loads colonial regions from files inside /common/colonial_regions.
     */
    private void loadColRegions() {
        System.out.printf(l10n("replay.load.colonials"));
        colRegions.clear();
        for(final InputStream cultureStream : fileManager.listFiles("common/colonial_regions")) {
            try (final InputStream is = cultureStream) {
                final ColRegionParser parser = new ColRegionParser(
                        colRegions, Long.MAX_VALUE, is,
                        new EmptyTaskBridge<Map<String, ColRegionInfo>>());
                parser.run();
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    /**
     * Loads countries from files inside /common/country_tags directory
     * and files mentioned in them.
     */
    private void loadCountries() {
        System.out.printf(l10n("replay.load.countries"));
        countries.clear();

        for (final InputStream is : fileManager.listFiles("common/country_tags")) {
            try (final InputStream tagStream = is) {
                final Properties tags = new Properties();
                tags.load(tagStream);
                for (Object key : tags.keySet()) {
                    String path = ((String) tags.get(key)).trim();
                    if (path.startsWith("\"")) {
                        path = path.substring(1, path.length() - 1); //get rid of "
                    }
                    try (final InputStream cs = fileManager.getInputStream("common/" + path)) {
                        final Ref<Integer> color = new Ref<>();
                        final CountryParser parser = new CountryParser(
                                color, Long.MAX_VALUE, cs,
                                new EmptyTaskBridge<Ref<Integer>>());
                        parser.run();
                        countries.put((String) key, new CountryInfo((String) key, color.val));
                    } catch(Exception e) { e.printStackTrace(); }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads religion colors from common/religions/*.
     */
    private void loadCultures() {
        System.out.printf(l10n("replay.load.cultures"));
        cultures.clear();
        for(final InputStream cultureStream : fileManager.listFiles("common/cultures")) {
            try (final InputStream is = cultureStream) {
                final CulturesParser parser = new CulturesParser(
                        new Pair<>(countries, cultures),
                        Long.MAX_VALUE, is, new EmptyTaskBridge<Pair<Map<String, CountryInfo>, Map<String, Integer>>>());
                parser.run();
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    /**
     * Loads game data in proper order.
     * @param finisher gets informed about map loading progress and called when finished
     */
    public void loadData(final ITaskBridge<Void> finisher) {
        System.out.printf(l10n("replay.load.data"));
        saveGame = null;
        fileManager.loadMods();
        loadDefines();
        loadProvinces();
        loadColRegions();
        loadMap(finisher);
        loadSeas();
        loadWastelands();
        loadCountries();
        loadCultures();
        loadReligions();
    }

    /**
     * Loads defines from common/defines.lua.
     */
    private void loadDefines() {
        System.out.printf(l10n("replay.load.defines"));
        try (final InputStream is = fileManager.getInputStream("common/defines.lua")) {
            final DefinesParser parser = new DefinesParser(
                    defines, Long.MAX_VALUE, is, new EmptyTaskBridge<DefinesInfo>());
            parser.run();
        } catch(Exception e) { e.printStackTrace(); }
    }

    /**
     * Starts loading the map from map/provinces.bmp.
     */
    private void loadMap(final ITaskBridge<?> finisher) {
        System.out.printf(l10n("replay.map.load"));
        finisher.updateTitle(l10n("replay.map.load"));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedImage map;
                    try (InputStream is = fileManager.getInputStream("map/provinces.bmp")) {
                        map = ImageIO.read(is);
                    } catch (FileNotFoundException e) {
                        System.err.printf(l10n("replay.map.notfound"));
                        map = new BufferedImage(1,1, BufferedImage.TYPE_4BYTE_ABGR);
                    }

                    bufferWidth = map.getWidth();
                    bufferHeight = map.getHeight();

                    //Copy from source to destination pixel by pixel
                    initBuffers(bufferWidth, bufferHeight);
                    for (int y = 0; y < bufferHeight; ++y){
                        for (int x = 0; x < bufferWidth; ++x){
                            int color = map.getRGB(x, y);
                            final int pos = y * bufferWidth + x;
                            boolean border = false;
                            if (drawBorders) {
                                if (x > 0 && map.getRGB(x-1, y) != color) {
                                    border = true;
                                } else if (x < bufferWidth - 1 && map.getRGB(x+1, y) != color) {
                                    border = true;
                                } else if (y > 0 && map.getRGB(x, y-1) != color) {
                                    border = true;
                                } else if (y < bufferHeight - 1 && map.getRGB(x, y+1) != color) {
                                    border = true;
                                }
                            }
                            if (border) {
                                color = borderColor;
                                borders.add(pos);
                            } else {
                                final ProvinceInfo province = colors.get(color);
                                if (province != null) {
                                    province.points.add(pos);
                                } else {
                                    System.err.printf(l10n("replay.map.unknowncolor"), x, y, color);
                                }
                            }
                            provincesBmpBuffer[pos] = color;
                            politicalBuffer[pos] = color;
                            religiousBuffer[pos] = color;
                            culturalBuffer[pos] = color;
                            technologySeparateBuffer[pos] = color;
                            technologyCombinedBuffer[pos] = color;
                            finisher.updateProgress(y * bufferWidth + x, bufferHeight * bufferWidth);
                        }
                    }

                    for(ProvinceInfo info : provinces.values()) {
                        info.calculateCenter(bufferWidth);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.printf(l10n("replay.map.loaded"));
                finisher.run();
            }
        }, "MapLoader").start();
    }

    /**
     * Loads provinces from map/definition.csv.
     */
    private void loadProvinces() {
        System.out.printf(l10n("replay.provinces.load"));
        provinces.clear();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(fileManager.getInputStream("map/definition.csv"), StandardCharsets.ISO_8859_1));
            //skip first line
            reader.readLine();
            String line = line = reader.readLine();
            while (line != null) {
                if (line.isEmpty()) {
                    line = reader.readLine();
                    continue;
                }
                final String[] parts = line.split(";");
                final int color = Utils.toColor(
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]));
                final ProvinceInfo province = new ProvinceInfo(parts[0], parts[4], color);
                provinces.put(parts[0], province);
                final ProvinceInfo original = colors.put(color, province);
                if (original != null) {
                    throw new RuntimeException(String.format(l10n("replay.provinces.error"), parts[0], original));
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            System.err.printf(l10n("replay.provinces.notfound"));
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) { }
            }
        }
        if (rnw) {
            final ProvinceInfo sea = new ProvinceInfo("SEA", "SEA", Utils.SEA_COLOR);
            sea.isSea = true;
            provinces.put(sea.id, sea);
            colors.put(sea.color, sea);
            final ProvinceInfo wasteland = new ProvinceInfo("WASTELAND", "WASTELAND", Utils.WASTELAND_COLOR);
            wasteland.isWasteland = true;
            provinces.put(wasteland.id, wasteland);
            colors.put(wasteland.color, wasteland);
        }
    }

    /**
     * Loads religion colors from common/religions/*.
     */
    private void loadReligions() {
        System.out.printf(l10n("replay.load.religions"));
        religions.clear();
        for(final InputStream religionStream : fileManager.listFiles("common/religions")) {
            try (final InputStream is = religionStream) {
                final ReligionsParser parser = new ReligionsParser(
                        religions, Long.MAX_VALUE, is,
                        new EmptyTaskBridge<Map<String, Integer>>());
                parser.run();
            } catch(Exception e) { e.printStackTrace(); }
        }
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
     * Loads sea provinces from map/default.map.
     */
    private void loadSeas() {
        System.out.printf(l10n("replay.load.seas"));
        try (final InputStream is = fileManager.getInputStream("/map/default.map")) {
            final DefaultMapParser parser = new DefaultMapParser(
                    provinces, Long.MAX_VALUE, is,
                    new EmptyTaskBridge<Map<String, ProvinceInfo>>());
            parser.run();
        } catch(Exception e) { e.printStackTrace(); }
    }

    /**
     * Loads wasteland provinces from map/climate.txt.
     */
    private void loadWastelands() {
        System.out.printf(l10n("replay.load.wastelands"));
        try (final InputStream is =  fileManager.getInputStream("/map/climate.txt")) {
            final ClimateParser parser = new ClimateParser(
                    provinces, Long.MAX_VALUE, is,
                    new EmptyTaskBridge<Map<String, ProvinceInfo>>());
            parser.run();
        } catch(Exception e) { e.printStackTrace(); }
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
