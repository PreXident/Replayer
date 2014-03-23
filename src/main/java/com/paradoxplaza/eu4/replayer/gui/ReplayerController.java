package com.paradoxplaza.eu4.replayer.gui;

import com.paradoxplaza.eu4.replayer.ColRegionInfo;
import com.paradoxplaza.eu4.replayer.CountryInfo;
import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.DateGenerator;
import com.paradoxplaza.eu4.replayer.DefinesInfo;
import com.paradoxplaza.eu4.replayer.EventProcessor;
import com.paradoxplaza.eu4.replayer.FileManager;
import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import com.paradoxplaza.eu4.replayer.Replay;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Controller;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.events.SimpleProvinceEvent;
import com.paradoxplaza.eu4.replayer.generator.ModGenerator;
import com.paradoxplaza.eu4.replayer.gif.Giffer;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.climate.ClimateParser;
import com.paradoxplaza.eu4.replayer.parser.colregion.ColRegionParser;
import com.paradoxplaza.eu4.replayer.parser.country.CountryParser;
import com.paradoxplaza.eu4.replayer.parser.culture.CulturesParser;
import com.paradoxplaza.eu4.replayer.parser.defaultmap.DefaultMapParser;
import com.paradoxplaza.eu4.replayer.parser.defines.DefinesParser;
import com.paradoxplaza.eu4.replayer.parser.religion.ReligionsParser;
import com.paradoxplaza.eu4.replayer.parser.savegame.BatchSaveGameParser;
import com.paradoxplaza.eu4.replayer.utils.ColorUtils;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import com.paradoxplaza.eu4.replayer.utils.Ref;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Controller for the Replayer application.
 */
public class ReplayerController implements Initializable {

    /** Original title. */
    static final String TITLE = "Replayer";

    /** Title format when replaying save game. */
    static final String TITLE_SAVEGAME = String.format("%1$s - %2$s", TITLE, "%1$s");

    /** Pattern for mathing country colors. */
    static final Pattern TAG_COLOR_PATTERN = Pattern.compile("^\\s*color\\s*=\\s*\\{\\s*(\\d+)\\s*(\\d+)\\s*(\\d+)\\s*\\}\\s*$");

    /** ID used to append new messages to log. */
    static final String LOG_ID = "content";

    /** Log content should always start with this. */
    static final String LOG_HEADER = String.format("<span id='%s'>", LOG_ID);

    /** This is appended to log content before updating the log. */
    static final String LOG_FOOTER = "</span>";

    /** Format for initial content of log. */
    static final String LOG_INIT_FORMAT = String.format("<html><body><div id='%1$s'>%%s</div></body></html>", LOG_ID);

    /** Initial content of log. */
    static final String LOG_INIT = String.format(LOG_INIT_FORMAT, "");

    /** JavaScript call to scroll to the bottom of the page. */
    static final String SCROLL_DOWN = "window.scrollTo(0,document.body.scrollHeight)";

    /** Path appended to user's home directory to get default "game" folder on Windows. */
    static final String WIN_DIR = "/Documents/Paradox Interactive/Europa Universalis IV";

    /** Path appended to user's home directory to get default game folder on Linux. */
    static final String LINUX_DIR = "/.paradoxinteractive/Europa Universalis IV";

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
    static final String DEFAULT_SAVE_DIR = DEFAULT_BASE_DIR + "/save games";

    /** Default mod directory. */
    static final String DEFAULT_MOD_DIR = DEFAULT_BASE_DIR + "/mod";

    /**
     * Translates {@link #map} coordinate to {@link #scrollPane} procentual HValue/VValue.
     * @param mapCoord map coordinate
     * @param mapSize size of map
     * @param scrollSize size of scrollPane
     * @param imageViewSize size of imageView
     * @return scrollPane procentual scroll position
     */
    static double mapCoordToScrollProcent(final int mapCoord, final double mapSize, final double scrollSize, final double imageViewSize) {
        final double zero = scrollSize / 2; //coord of center when (H/V)Value == 0
        final double unit = imageViewSize - 2 * zero; //size of scrollable area
        final double scroll = mapCoord / mapSize * imageViewSize; //map coords to scroll coords
        final double procent = (scroll - zero) / unit; //scroll coords to procent
        if (procent < 0) {
            return 0;
        } else if (procent > 1) {
            return 1;
        }
        return procent;
    }

    /**
     * Translates procentual HValue/VValue to {@link #map} coordinate.
     * @param procent scrollPane procentual scroll position
     * @param mapSize size of map
     * @param scrollSize size of scrollPane
     * @param imageViewSize size of imageView
     * @return map coordinate
     */
    static int scrollProcentToMapCoord(final double procent, final double mapSize, final double scrollSize, final double imageViewSize) {
        final double zero = scrollSize / 2; //coord of center when (H/V)Value == 0
        final double unit = imageViewSize - 2 * zero; //size of scrollable area
        final double scroll = procent * unit + zero;
        return (int) (scroll / imageViewSize * mapSize);
    }

    /** Possible directions of replaying. */
    enum Direction { FORWARD, BACKWARD }

    @FXML
    BorderPane root;

    @FXML
    ImageView imageView;

    @FXML
    ScrollPane scrollPane;

    @FXML
    WebView log;

    @FXML
    HBox logContainer;

    @FXML
    TextField dateLabel;

    @FXML
    VBox bottom;

    @FXML
    ProgressBar progressBar;

    @FXML
    Menu eventMenu;

    @FXML
    ComboBox<String> daysCombo;

    @FXML
    TextField focusEdit;

    @FXML
    Label statusLabel;

    @FXML
    Button jumpButton;

    @FXML
    HBox provinceContainer;

    @FXML
    WebView provinceLog;

    @FXML
    ComboBox<String> periodCombo;

    @FXML
    ComboBox<String> langCombo;

    @FXML
    Menu gifMenu;

    @FXML
    CheckMenuItem bordersCheckMenuItem;

    @FXML
    CheckMenuItem subjectsCheckMenuItem;

    @FXML
    CheckMenuItem gifSwitchCheckMenuItem;

    @FXML
    CheckMenuItem gifLoopCheckMenuItem;

    @FXML
    TextField gifBreakEdit;

    @FXML
    TextField gifStepEdit;

    @FXML
    CheckMenuItem gifDateCheckMenuItem;

    @FXML
    CheckMenuItem gifSubimageCheckMenuItem;

    @FXML
    MyColorPicker gifDateColorPicker;

    @FXML
    TextField gifDateSizeEdit;

    @FXML
    TextField gifDateXEdit;

    @FXML
    TextField gifDateYEdit;

    @FXML
    TextField gifSubimageXEdit;

    @FXML
    TextField gifSubimageYEdit;

    @FXML
    TextField gifSubimageWidthEdit;

    @FXML
    TextField gifSubimageHeightEdit;

    /** Lock to prevent user input while background processing. */
    final Semaphore lock = new Semaphore(1);

    /** Replayer settings. */
    public Properties settings;

    /** Original map picture. */
    Image map;

    /** Reader for {@link #map}. */
    PixelReader reader;

    /** Image displayed in {@link #imageView}. */
    public WritableImage output;

    /** Buffer for emergency refresh. */
    public int[] buffer;

    /** How many pixels are added to width and height when zooming in/out. */
    int zoomStep;

    /** Directory containing save games. */
    File saveDirectory;

    /** Path to save game file. */
    String saveFileName;

    /** Directory containing needed game files. */
    public File eu4Directory;

    /** Property binded to stage titleProperty. */
    StringProperty titleProperty = new SimpleStringProperty(TITLE);

    /** Province color -> province mapping. */
    Map<Integer, ProvinceInfo> colors = new HashMap<>();

    /** Set of points constituting borders. */
    Set<Integer> borders = new HashSet<>();

    /** Colonial region name -> colonial name mapping. */
    Map<String, ColRegionInfo> colRegions = new HashMap<>();

    /** Loaded save game to be replayed. */
    SaveGame saveGame;

    /** Flag indication whether borders should be drawn. */
    boolean drawBorders;

    /** Number of periods processed in one tick. */
    int deltaPerTick;

    /** Period per tick. */
    Date.Period period;

    /** Timer for replaying. */
    Timeline timeline;

    /** Current direction of replaying. */
    Direction direction = null;

    /** Generates dates for replaying dates. */
    DateGenerator dateGenerator;

    /** Listens to date changes and initiates event processing. */
    final DateListener dateListener = new DateListener();

    /** Content of log area with html code. */
    public final StringBuilder logContent = new StringBuilder();

    /** Writer of gif output. */
    Giffer giffer = null;

    /** Selected province. */
    ProvinceInfo selectedProvince;

    /** Content of selected province log. */
    String selectedProvinceLogContent;

    /** Holds information from defines.lua. */
    final DefinesInfo defines = new DefinesInfo();

    /** Save game file. */
    File file;

    /** Random New World feature is on. */
    public boolean rnw = false;

    /** Replay object itself. */
    Replay replay;

    /** Jumper that fast forwards/rewinds the save. */
    Jumper finalizer;

    /** Handles files with respect to mods. */
    FileManager fileManager = new FileManager(this);

    /** Listener that updates log, appends to logContent and updates buffer. */
    final EventProcessor.Listener standardListener = new EventListener(this);

    /** Listener that does not update log. */
    final EventProcessor.Listener noLogUpdateListener = new EventListener(this) {
        @Override
        public void updateLog() { }
    };

     /** Listener that does not update log nor {@link #output}, only appends to logContent. */
    final EventProcessor.Listener bufferChangeOnlyListener = new EventListener(this) {
        @Override
        public void updateLog() { }

        @Override
        public void setColor(int[] buffer, final int pos, final int color) { }
    };

    /** Event processor. */
    final EventProcessor eventProcessor = new EventProcessor(standardListener);

    @FXML
    private void backPlay() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (saveGame == null) {
            lock.release();
            return;
        }
        if (timeline != null) {
            if (direction == Direction.BACKWARD) {
                timeline.play();
                lock.release();
                return;
            } else {
                timeline.stop();
            }
        }

        //final Timeline tl = new Timeline();
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(0.1),
                  new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        Date iter = dateGenerator.dateProperty().get();
                        final Date target = iter.skip(period, -deltaPerTick);
                        while (!target.equals(iter)) {
                            if (dateGenerator.hasPrev()) {
                                iter = dateGenerator.prev();
                            } else {
                                timeline.stop();
                                timeline = null;
                                break;
                            }
                        }
                    }
                }));
        direction = Direction.BACKWARD;
        timeline.playFromStart();
        lock.release();
    }

    @FXML
    private void borders() {
        settings.setProperty("borders", bordersCheckMenuItem.isSelected() ? "true" : "false");
    }

    @FXML
    private void changeEU4Directory() throws InterruptedException{
        if (!lock.tryAcquire()) {
           return;
        }
        pause();
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(l10n("app.eu4dir.select"));
        if (eu4Directory != null && eu4Directory.exists() && eu4Directory.isDirectory()) {
            directoryChooser.setInitialDirectory(eu4Directory);
        }
        final File dir = directoryChooser.showDialog(getWindow());
        if (dir != null) {
            eu4Directory = dir;
            settings.setProperty("eu4.dir", eu4Directory.getPath());
            loadData();
        }
        lock.release();
    }

    @FXML
    private void close() {
        Platform.exit();
    }

    @FXML
    private void culturalMapMode() {
        buffer = replay.culturalBuffer;
        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
    }

    @FXML
    private void finish() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (saveGame == null || !dateGenerator.hasNext()) {
            lock.release();
            return;
        }
        pause();
        direction = null;
        finalizer = new Jumper() {
            Date bound;
            {
                updateInitFormat = l10n("replay.finishing");
                updateDoneFormat = l10n("replay.fastforward.done");
            }

            @Override
            protected EventProcessor.Listener getEventListener() {
                return bufferChangeOnlyListener;
            }

            @Override
            protected Date getBound() {
                bound = saveGame.date.next();
                return bound;
            }

            @Override
            protected Date getCurrentDate() {
                return currentDate;
            }

            @Override
            protected Date getIter() {
                return dateGenerator.dateProperty().get().next();
            }

            @Override
            protected Date getTarget() {
                return saveGame.date;
            }

            @Override
            protected Date iterNext(Date iter) {
                if (giffer != null) {
                    giffer.updateGif(buffer, iter);
                }
                final Date next = iter.next();
                if (next.equals(bound)) {
                    endGif();
                }
                return iter.next();
            }

            @Override
            protected void processEvents(final Date date, final List<Event> events) {
                eventProcessor.processEvents(date, events);
            }

            @Override
            protected int updateDay(final int day) {
                return day + 1;
            }
        };
        statusLabel.textProperty().bind(finalizer.titleProperty());
        progressBar.progressProperty().bind(finalizer.progressProperty());
        new Thread(finalizer, "Game finalizer").start();
    }

    @FXML
    private void generateMod() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (rnw) {
            statusLabel.setText(l10n("generator.rnw"));
        } else {
            new ModGenerator(settings).generate(replay.provinces.values());
            statusLabel.setText(l10n("generator.done"));
        }
        lock.release();
    }

    @FXML
    private void gifDate() {
        final boolean drawDate = gifDateCheckMenuItem.isSelected();
        if (!lock.tryAcquire()) {
            gifDateCheckMenuItem.setSelected(drawDate);
            return;
        }
        settings.setProperty("gif.date", drawDate ? "true" : "false");
        if (giffer != null) {
            giffer.setGifDateDraw(drawDate);
        }
        lock.release();
    }

    @FXML
    private void gifLoop() {
        settings.setProperty("gif.loop", gifLoopCheckMenuItem.isSelected() ? "true" : "false");
    }

    @FXML
    private void gifSubimage() {
        final boolean subimage = gifSubimageCheckMenuItem.isSelected();
        if (!lock.tryAcquire()) {
            gifSubimageCheckMenuItem.setSelected(subimage);
            return;
        }
        settings.setProperty("gif.subimage", subimage ? "true" : "false");
        if (giffer != null) {
            giffer.setGifSubImage(subimage);
        }
        lock.release();
    }

    @FXML
    private void gifSwitch() {
        if (!lock.tryAcquire()) {
            //undo
            gifSwitchCheckMenuItem.setSelected(!gifSwitchCheckMenuItem.isSelected());
            return;
        }
        if (gifSwitchCheckMenuItem.isSelected()) {
            if (giffer == null && file != null) {
                final int width = (int) map.getWidth();
                final int height = (int) map.getHeight();
                giffer = new Giffer(settings, width, height, file.getAbsolutePath());
                giffer.updateGif(buffer, dateGenerator.dateProperty().get());
            } else {
                gifSwitchCheckMenuItem.setSelected(false);
            }
        } else {
            endGif();
        }
        lock.release();
    }

    @FXML
    private void jump() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (saveGame == null) {
            lock.release();
            return;
        }
        pause();
        try {
            final Date target = new Date(dateLabel.getText());
            direction = null;
            if (target.compareTo(dateGenerator.getMin()) < 0 || target.compareTo(dateGenerator.getMax()) > 0) {
                statusLabel.setText(l10n("replay.jump.outside"));
                lock.release();
                return;
            }
            pause();
            final Date date = dateGenerator.dateProperty().get();
            if (date.equals(target)) {
                lock.release();
                return;
            }
            if (date.compareTo(target) < 0) {
                finalizer = new Jumper() {

                    @Override
                    protected EventProcessor.Listener getEventListener() {
                        return bufferChangeOnlyListener;
                    }

                    @Override
                    protected Date getBound() {
                        return target.next();
                    }

                    @Override
                    protected Date getCurrentDate() {
                        return currentDate;
                    }

                    @Override
                    protected Date getIter() {
                        return date.next();
                    }

                    @Override
                    protected Date getTarget() {
                        return target;
                    }

                    @Override
                    protected Date iterNext(final Date iter) {
                        return iter.next();
                    }

                    @Override
                    protected void processEvents(final Date date, final List<Event> events) {
                        eventProcessor.processEvents(date, events);
                    }

                    @Override
                    protected int updateDay(final int day) {
                        return day + 1;
                    }
                };
            } else /* if (date.compareTo(target) > 0 */ {
                finalizer = new Jumper() {

                    @Override
                    protected EventProcessor.Listener getEventListener() {
                        return bufferChangeOnlyListener;
                    }

                    @Override
                    protected Date getBound() {
                        return target;
                    }

                    @Override
                    protected Date getCurrentDate() {
                        return currentDate.prev();
                    }

                    @Override
                    protected Date getIter() {
                        return date;
                    }

                    @Override
                    protected Date getTarget() {
                        return target;
                    }

                    @Override
                    protected Date iterNext(final Date iter) {
                        return iter.prev();
                    }

                    @Override
                    protected void processEvents(final Date date, final List<Event> events) {
                        eventProcessor.unprocessEvents(date, events);
                    }

                    @Override
                    protected int updateDay(final int day) {
                        return day - 1;
                    }
                };
            }
            statusLabel.textProperty().bind(finalizer.titleProperty());
            progressBar.progressProperty().bind(finalizer.progressProperty());
            new Thread(finalizer, "Jumper").start();
        } catch (Exception e) {
            statusLabel.setText(l10n("replay.jump.parse.error"));
            lock.release();
        }
    }

    @FXML
    private void load() throws InterruptedException {
        if (!lock.tryAcquire()) {
           return;
        }
        pause();
        endGif();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(saveDirectory);
        fileChooser.setTitle(l10n("replay.eu4save.select"));

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(l10n("replay.eu4save.ext"), "*.eu4");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        final List<File> files = fileChooser.showOpenMultipleDialog(getWindow());
        if (files == null || files.isEmpty()) {
            lock.release();
            return;
        }
        final File[] fileArr = files.toArray(new File[files.size()]);
        Arrays.sort(fileArr, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });

        saveDirectory = fileArr[fileArr.length-1].getParentFile();
        settings.setProperty("save.dir", saveDirectory.getPath());
        file = fileArr[fileArr.length-1];
        titleProperty.setValue(String.format(TITLE_SAVEGAME, file.getName()));
        saveGame = new SaveGame();
        replay.reset();

        try {
            final BatchSaveGameParser parser = new BatchSaveGameParser(rnw, saveGame, fileArr);
            final int width = (int) map.getWidth();
            final int height = (int) map.getHeight();
            imageView.setImage(null);
            final Task<Void> mapInitializer = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateTitle(l10n("replay.map.init"));
                    final int width = (int) map.getWidth();
                    final int height = (int) map.getHeight();

                    final long size = height * width;
                    for (int y = 0; y < height; ++y) {
                        for (int x = 0; x < width; ++x) {
                            final int pos = y * width + x;
                            final int color = reader.getArgb(x, y);
                            int finalColor;
                            final ProvinceInfo province = colors.get(color);
                            if (province != null && province.isSea) {
                                finalColor = replay.seaColor;
                            } else if (borders.contains(pos)) {
                                finalColor = replay.borderColor;
                            } else {
                                finalColor = replay.landColor;
                            }
                            replay.politicalBuffer[pos] = finalColor;
                            replay.religiousBuffer[pos] = finalColor;
                            replay.culturalBuffer[pos] = finalColor;
                            replay.technologySeparateBuffer[pos] = finalColor;
                            replay.technologyCombinedBuffer[pos] = finalColor;
                            updateProgress(pos, size);
                        }
                    }
                    return null;
                }
            };
            mapInitializer.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    output.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbPreInstance(), buffer, 0, width);
                    statusLabel.textProperty().bind(parser.titleProperty());
                    progressBar.progressProperty().bind(parser.progressProperty());
                    new Thread(parser, "Parser").start();
                }
            });

            final Task<Void> starter = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    dateGenerator = new DateGenerator(saveGame.startDate, saveGame.date);
                    updateTitle(l10n("replay.world.init"));
                    eventProcessor.setListener(noLogUpdateListener);
                    eventProcessor.processEvents(null, new ProgressIterable<>(saveGame.timeline.get(null)));
                    //
                    updateTitle(l10n("replay.progressing"));
                    final Date maxDate = saveGame.startDate;
                    Date date = new Date(settings.getProperty("init.start", "1300.1.1"));
                    int day = 0;
                    final int distance = Date.calculateDistance(date, saveGame.startDate) + 1;
                    eventProcessor.setListener(bufferChangeOnlyListener);
                    while (date.compareTo(maxDate) <= 0) {
                        final List<Event> events = saveGame.timeline.get(date);
                        eventProcessor.processEvents(date, events);
                        updateProgress(++day, distance);
                        date = date.next();
                    }
                    //fix colonial nations
                    if (settings.getProperty("fix.colonials", "true").equals("true")) {
                        updateTitle(l10n("replay.colonials.fix"));
                        int colRegCounter = 0;
                        final Date magicalDate = new Date(settings.getProperty("fix.colonials.date", "1444.11.11"));
                        for (ColRegionInfo colreg : colRegions.values()) {
                            updateProgress(colRegCounter++, colRegions.size());
                            //count colonies for countries
                            final Map<String, List<ProvinceInfo>> colonies = new HashMap<>();
                            for (String id : colreg.provinces) {
                                final ProvinceInfo province = replay.provinces.get(id);
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
                    eventProcessor.setListener(standardListener);
                    return null;
                }
            };

            starter.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    if ("true".equals(settings.getProperty("gif"))) {
                        giffer = new Giffer(settings, width, height, fileArr[fileArr.length-1].getAbsolutePath());
                        giffer.updateGif(buffer, saveGame.startDate);
                    }
                    output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
                    log.getEngine().loadContent(String.format(LOG_INIT_FORMAT, logContent.toString()));
                    logContent.setLength(LOG_HEADER.length());
                    progressBar.progressProperty().bind(dateGenerator.progressProperty());
                    dateGenerator.dateProperty().addListener(dateListener);
                    dateLabel.setText(dateGenerator.dateProperty().get().toString());
                    imageView.setImage(output);
                    new JavascriptBridge().prov(settings.getProperty("center.id", "1"));
                    statusLabel.textProperty().unbind();
                    statusLabel.setText(l10n("replay.save.loaded"));
                    lock.release();
                }
            });

            starter.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(final WorkerStateEvent t) {
                    final Throwable e = starter.getException();
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            });

            parser.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    statusLabel.textProperty().bind(starter.titleProperty());
                    progressBar.progressProperty().bind(starter.progressProperty());
                    for (Entry<String, Integer> c : saveGame.dynamicCountriesColors.entrySet()) {
                        replay.countries.put(c.getKey(), new CountryInfo(c.getKey(), c.getValue()));
                    }
                    for (Map.Entry<String, Date> change : saveGame.tagChanges.entrySet()) {
                        final String tag = change.getKey();
                        final CountryInfo country = replay.countries.get(tag);
                        if (country != null) {
                            country.expectingTagChange = change.getValue();
                        } else {
                            System.err.printf(l10n("replay.tagchange.unknowntag"), tag);
                        }
                    }
                    new Thread(starter, "Starter").start();
                }
            });

            parser.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(final WorkerStateEvent t) {
                    final Throwable e = starter.getException();
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            });

            statusLabel.textProperty().bind(mapInitializer.titleProperty());
            progressBar.progressProperty().bind(mapInitializer.progressProperty());
            new Thread(mapInitializer, "MapInitializer").start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void pause() {
        if (timeline != null) {
            timeline.pause();
        }
        if (finalizer != null) {
            finalizer.cancel();
            finalizer = null;
        }
    }

    @FXML
    private void play() throws InterruptedException {
        if (!lock.tryAcquire()) {
           return;
        }
        if (saveGame == null) {
            lock.release();
            return;
        }
        if (timeline != null) {
            if (direction == Direction.FORWARD) {
                timeline.play();
                lock.release();
                return;
            } else {
                timeline.stop();
            }
        }

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(0.1),
                  new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        Date iter = dateGenerator.dateProperty().get();
                        final Date target = iter.skip(period, deltaPerTick);
                        while (!target.equals(iter)) {
                            if (dateGenerator.hasNext()) {
                                iter = dateGenerator.next();
                            } else {
                                timeline.stop();
                                timeline = null;
                                endGif();
                                break;
                            }
                        }
                    }
                }));
        direction = Direction.FORWARD;
        timeline.playFromStart();
        lock.release();
    }

    @FXML
    private void politicalMapMode() {
        buffer = replay.politicalBuffer;
        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
    }

    @FXML
    private void refresh() {
        if (!lock.tryAcquire()) {
           return;
        }
        pause();
        scrollPane.setContent(null);
        imageView.setImage(null);
        int width = (int) output.getWidth();
        int height = (int) output.getHeight();
        WritableImage i = new WritableImage(width, height);
        i.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbPreInstance(), buffer, 0, width);
        output = i;
        imageView.setImage(i);
        scrollPane.setContent(imageView);
        lock.release();
    }

    @FXML
    private void religiousMapMode() {
        buffer = replay.religiousBuffer;
        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
    }

    @FXML
    private void resetZoom() {
        imageView.setFitHeight(map.getHeight());
        imageView.setFitWidth(map.getWidth());
        //if scrollbars were not visible we need to do this to make them appear
        //again if needed
        scrollPane.setContent(null);
        scrollPane.setContent(imageView);
    }

    @FXML
    private void subjectsAsOverlords() {
        settings.setProperty("subjects.as.overlord", bordersCheckMenuItem.isSelected() ? "true" : "false");
    }

    @FXML
    private void technologyCombinedMapMode() {
        buffer = replay.technologyCombinedBuffer;
        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
    }

    @FXML
    private void technologySeparateMapMode() {
        buffer = replay.technologySeparateBuffer;
        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
    }

    @FXML
    private void toStart() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (saveGame == null || !dateGenerator.hasPrev()) {
            lock.release();
            return;
        }
        lock.release();
        dateLabel.setText(saveGame.startDate.toString());
        jump();
    }

    @FXML
    private void zoomIn() {
        Bounds bounds = imageView.getBoundsInParent();
        final int x = scrollProcentToMapCoord(
                scrollPane.getHvalue(), map.getWidth(),
                scrollPane.getWidth(), bounds.getWidth());
        final int y = scrollProcentToMapCoord(
                scrollPane.getVvalue(), map.getHeight(),
                scrollPane.getHeight(), bounds.getHeight());
        imageView.setFitHeight(imageView.getFitHeight() + zoomStep);
        imageView.setFitWidth(imageView.getFitWidth() + zoomStep);
        bounds = imageView.getBoundsInParent();
        scrollPane.setHvalue(mapCoordToScrollProcent(
                x, map.getWidth(), scrollPane.getWidth(), bounds.getWidth()));
        scrollPane.setVvalue(mapCoordToScrollProcent(
                y, map.getHeight(), scrollPane.getHeight(), bounds.getHeight()));
    }

    @FXML
    private void zoomOut() {
        Bounds bounds = imageView.getBoundsInParent();
        final int x = scrollProcentToMapCoord(
                scrollPane.getHvalue(), map.getWidth(),
                scrollPane.getWidth(), bounds.getWidth());
        final int y = scrollProcentToMapCoord(
                scrollPane.getVvalue(), map.getHeight(),
                scrollPane.getHeight(), bounds.getHeight());
        double h = imageView.getFitHeight() - zoomStep;
        imageView.setFitHeight(h < 0 ? imageView.getFitHeight() : h);
        double w = imageView.getFitWidth() - zoomStep;
        imageView.setFitWidth(w < 0 ? imageView.getFitWidth() : w);
        bounds = imageView.getBoundsInParent();
        scrollPane.setHvalue(mapCoordToScrollProcent(
                x, map.getWidth(), scrollPane.getWidth(), bounds.getWidth()));
        scrollPane.setVvalue(mapCoordToScrollProcent(
                y, map.getHeight(), scrollPane.getHeight(), bounds.getHeight()));
    }

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        System.out.printf(l10n("replay.initializating"));
        log.prefWidthProperty().bind(logContainer.widthProperty());
        provinceLog.prefWidthProperty().bind(provinceContainer.widthProperty());
        progressBar.prefWidthProperty().bind(bottom.widthProperty());

        //center map if too small
        imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                final Bounds bounds = imageView.getBoundsInParent();
                if (bounds.getWidth() < scrollPane.getWidth()) {
                    imageView.setTranslateX((scrollPane.getWidth() - bounds.getWidth()) / 2);
                } else {
                    imageView.setTranslateX(0);
                }
                if (bounds.getHeight()< scrollPane.getHeight()) {
                    imageView.setTranslateY((scrollPane.getHeight()- bounds.getHeight()) / 2);
                } else {
                    imageView.setTranslateY(0);
                }
            }
        });

        //mouse wheel to zoom in/out the map
        scrollPane.addEventFilter(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent t) {
                if (t.getDeltaY() < 0) {
                    zoomOut();
                } else {
                    zoomIn();
                }
                t.consume();
            }
        });

        //to prevent menu from closing
        final EventHandler<MouseEvent> filter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                t.consume();
            }
        };
        //add change handler and event filter to event menu
        for(final MenuItem item : eventMenu.getItems()) {
            if (item instanceof CustomMenuItem) {
                final String event = item.getText();
                final CustomMenuItem customItem = ((CustomMenuItem) item);
                if (customItem.getContent() instanceof CheckBox) {
                    final CheckBox checkBox = (CheckBox) customItem.getContent();
                    checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(final ObservableValue<? extends Boolean> ov, final Boolean oldVal, final Boolean newVal) {
                            if (newVal) {
                                replay.notableEvents.add(event);
                            } else {
                                replay.notableEvents.remove(event);
                            }
                        }
                    });
                    checkBox.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
                }
            }
        }

        daysCombo.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        daysCombo.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                try {
                    final int parsed = Integer.parseInt(newVal);
                    if (parsed <= 0) {
                        daysCombo.setValue(oldVal);
                        return;
                    }
                    deltaPerTick = parsed;
                    settings.setProperty("delta.per.tick", newVal);
                } catch (NumberFormatException e) {
                    daysCombo.setValue(oldVal);
                }
            }
        });

        periodCombo.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        periodCombo.getItems().addAll(
                l10n("fxml.settings.period.days"),
                l10n("fxml.settings.period.months"),
                l10n("fxml.settings.period.years")
        );
        periodCombo.getSelectionModel().selectFirst();
        periodCombo.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                if (newVal == null) {
                    return;
                }
                switch (newVal.intValue()) {
                    case 0:
                        period = Date.DAY;
                        break;
                    case 1:
                        period = Date.MONTH;
                        break;
                    case 2:
                        period = Date.YEAR;
                        break;
                    default:
                        periodCombo.getSelectionModel().select(oldVal.intValue());
                        return;
                }
                settings.setProperty("period.per.tick", newVal.toString());
            }
        });

        focusEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        focusEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                replay.focusTag = newVal;
                replay.focusing = !"".equals(newVal);
                settings.setProperty("focus", newVal);
            }
        });

        langCombo.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (newVal == null) {
                    return;
                }
                try {
                    Locale.setDefault(new Locale(newVal));
                    settings.setProperty("locale.language", newVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        imageView.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                final Bounds imageBounds = imageView.getBoundsInParent();
                int x = (int) (t.getX() * replay.bufferWidth / imageBounds.getWidth());
                int y = (int) (t.getY() * replay.bufferHeight / imageBounds.getHeight());
                final String coords = "[" + x + "," + y + "]\n";
                final String provinceHint = coords + colors.get(reader.getArgb(x, y)).toString();
                if (!scrollPane.getTooltip().getText().equals(provinceHint)) {
                    scrollPane.setTooltip(new Tooltip(provinceHint));
                }
            }
        });

        imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                final Bounds imageBounds = imageView.getBoundsInParent();
                int x = (int) (t.getX() * replay.bufferWidth / imageBounds.getWidth());
                int y = (int) (t.getY() * replay.bufferHeight / imageBounds.getHeight());
                selectedProvince = colors.get(reader.getArgb(x, y));
                if (selectedProvince != null) {
                    final String provinceLogContent = selectedProvince.getLog();
                    if (!provinceLogContent.equals(selectedProvinceLogContent)) {
                        provinceLog.getEngine().loadContent(provinceLogContent);
                        selectedProvinceLogContent = provinceLogContent;
                    }
                }
            }
        });

        dateLabel.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldVal, final String newVal) {
                if (dateGenerator == null) {
                    return;
                }
                jumpButton.setVisible(!newVal.equals(dateGenerator.dateProperty().get().toString()));
            }
        });

        gifBreakEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifBreakEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                int gifBreak;
                try {
                    gifBreak = Integer.parseInt(newVal);
                    if (gifBreak < 0) {
                        gifBreak = 0;
                    }
                } catch (NumberFormatException e) {
                    gifBreak = 0;
                }
                settings.setProperty("gif.new.file", Integer.toString(gifBreak));
            }
        });

        gifStepEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifStepEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                int gifStep;
                try {
                    gifStep = Integer.parseInt(newVal);
                    if (gifStep <= 0) {
                        gifStep = 1;
                    }
                } catch (NumberFormatException e) {
                    gifStep = 1;
                }
                settings.setProperty("gif.step", Integer.toString(gifStep));
            }
        });

        gifDateColorPicker.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifDateColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> ov, Color oldVal, Color newVal) {
                if (!lock.tryAcquire()) {
                    gifDateColorPicker.setValue(oldVal);
                    return;
                }
                final int red = (int) (newVal.getRed() * 255);
                final int green = (int) (newVal.getGreen() * 255);
                final int blue = (int) (newVal.getBlue() * 255);
                settings.setProperty("gif.date.color", String.format("0x%02X%02X%02X", red, green, blue));
                if (giffer != null) {
                    giffer.setGifDateColor(new java.awt.Color(red, green, blue));
                }
                lock.release();
            }
        });

        gifDateSizeEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifDateSizeEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final float size = Float.parseFloat(newVal);
                    if (giffer != null) {
                        giffer.setGifDateSize(size);
                    }
                    settings.setProperty("gif.date.size", newVal);
                } catch (NumberFormatException e) {
                    //gifDateSizeEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifDateXEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifDateXEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int x = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifDateX(x);
                    }
                    settings.setProperty("gif.date.x", newVal);
                } catch (NumberFormatException e) {
                    //gifDateXEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifDateYEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifDateYEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int y = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifDateY(y);
                    }
                    settings.setProperty("gif.date.y", newVal);
                } catch (NumberFormatException e) {
                    //gifDateYEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifSubimageXEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifSubimageXEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int x = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifSubImageX(x);
                    }
                    settings.setProperty("gif.subimage.x", newVal);
                } catch (NumberFormatException e) {
                    //gifSubimageXEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifSubimageYEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifSubimageYEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int y = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifSubImageY(y);
                    }
                    settings.setProperty("gif.subimage.y", newVal);
                } catch (NumberFormatException e) {
                    //gifSubimageYEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifSubimageWidthEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifSubimageWidthEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int w = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifSubImageWidth(w);
                    }
                    settings.setProperty("gif.subimage.width", newVal);
                } catch (NumberFormatException e) {
                    //gifSubimageWidthEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifSubimageHeightEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifSubimageHeightEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int h = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifSubImageHeight(h);
                    }
                    settings.setProperty("gif.subimage.height", newVal);
                } catch (NumberFormatException e) {
                    //gifSubimageHeightEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        final WebEngine provinceEngine = provinceLog.getEngine();
        provinceEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    JSObject win = (JSObject) provinceEngine.executeScript("window");
                    win.setMember("java", new JavascriptBridge());
                }
            }
        });

        log.setContextMenuEnabled(false); //throws exception when in fxml
        final ContextMenu cm = new ContextMenu();
        final MenuItem clearLog = new MenuItem(l10n("replay.log.clear"));
        clearLog.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                log.getEngine().loadContent(LOG_INIT);
            }
        });
        cm.getItems().add(clearLog);
        log.addEventHandler(MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        cm.show(log, e.getScreenX(), e.getScreenY());
                    }
                }
        });
        final WebEngine webEngine = log.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    JSObject win = (JSObject) webEngine.executeScript("window");
                    win.setMember("java", new JavascriptBridge());
                    webEngine.executeScript(SCROLL_DOWN);
                    log.getEngine().setUserStyleSheetLocation(getClass().getResource("Log.css").toExternalForm());
                }
            }
        });
        webEngine.setJavaScriptEnabled(true);
        webEngine.loadContent(LOG_INIT);
        logContent.append(LOG_HEADER);
    }

    /**
     * Sets Replayer settings. Adjusts saveDirectory etc. and loads map.
     * @param settings new settings
     */
    public void setSettings(final Properties settings) {
        this.settings = settings;

        replay = new Replay(settings);
        eventProcessor.setReplay(replay);

        for(final MenuItem item : eventMenu.getItems()) {
            if (item instanceof CustomMenuItem) {
                final String event = item.getText();
                final CustomMenuItem customItem = ((CustomMenuItem) item);
                if (customItem.getContent() instanceof CheckBox) {
                    final CheckBox checkBox = (CheckBox) customItem.getContent();
                    checkBox.setSelected(replay.notableEvents.contains(event));
                }
            }
        }

        zoomStep = Integer.parseInt(settings.getProperty("zoom.step", "100"));

        daysCombo.getItems().addAll(settings.getProperty("list.delta.per.tick", "1;30;365").split(";"));
        daysCombo.getSelectionModel().select(settings.getProperty("delta.per.tick", "1"));
        try {
            periodCombo.getSelectionModel().select(Integer.parseInt(settings.getProperty("period.per.tick", "0")));
        } catch (NumberFormatException e) {
            periodCombo.getSelectionModel().selectFirst();
            settings.setProperty("period.per.tick", "0");
        }

        langCombo.getSelectionModel().select(settings.getProperty("locale.language", "en"));

        drawBorders = "true".equals(settings.getProperty("borders", "false"));

        focusEdit.setText(replay.focusTag);

        saveDirectory = new File(settings.getProperty("save.dir", ""));
        if (!saveDirectory.exists() || !saveDirectory.isDirectory()) {
            saveDirectory = new File(DEFAULT_SAVE_DIR);
            if (!saveDirectory.exists() || !saveDirectory.isDirectory()) {
                saveDirectory = new File(System.getProperty("user.home"), "/");
            }
        }

        subjectsCheckMenuItem.setSelected(replay.subjectsAsOverlords);

        final String rnwMap = settings.getProperty("rnw.map");
        rnw = rnwMap != null  && !rnwMap.isEmpty();

        gifMenu.setVisible(!settings.getProperty("gif", "false").equals("true"));
        gifLoopCheckMenuItem.setSelected(settings.getProperty("gif.loop", "true").equals("true"));
        gifBreakEdit.setText(settings.getProperty("gif.new.file", "0"));
        gifStepEdit.setText(settings.getProperty("gif.step", "100"));
        gifDateCheckMenuItem.setSelected(settings.getProperty("gif.date", "true").equals("true"));
        gifDateColorPicker.setValue(Color.web(settings.getProperty("gif.date.color", "0x000000")));
        gifDateSizeEdit.setText(settings.getProperty("gif.date.size", "12"));
        gifDateXEdit.setText(settings.getProperty("gif.date.x", "60"));
        gifDateYEdit.setText(settings.getProperty("gif.date.y", "60"));
        gifSubimageCheckMenuItem.setSelected(settings.getProperty("gif.subimage", "false").equals("true"));
        gifSubimageXEdit.setText(settings.getProperty("gif.subimage.x", "0"));
        gifSubimageYEdit.setText(settings.getProperty("gif.subimage.y", "0"));
        gifSubimageWidthEdit.setText(settings.getProperty("gif.subimage.width", ""));
        gifSubimageHeightEdit.setText(settings.getProperty("gif.subimage.height", ""));

        bordersCheckMenuItem.setSelected(settings.getProperty("borders", "false").equals("true"));

        eu4Directory = new File(settings.getProperty("eu4.dir"));
        try {
            lock.acquire();
            loadData();
        } catch (InterruptedException e) { }
    }

    /**
     * Ends gif.
     */
    private void endGif() {
        if (giffer != null) {
            giffer.endGif();
            giffer = null;
        }
        if (gifSwitchCheckMenuItem.isSelected()) {
            if (Platform.isFxApplicationThread()) {
                gifSwitchCheckMenuItem.setSelected(false);
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        gifSwitchCheckMenuItem.setSelected(false);
                    }
                });
            }
        }
    }

    /**
     * Returns title property.
     * @return title property
     */
    public StringProperty titleProperty() {
        return titleProperty;
    }

    /**
     * Returns window of the root.
     * @return window of the root
     */
    private Window getWindow() {
        return root.getScene().getWindow();
    }

    /**
     * Loads colonial regions from files inside /common/colonial_regions.
     */
    private void loadColRegions() {
        System.out.printf(l10n("replay.load.colonials"));
        colRegions.clear();
        for(final InputStream cultureStream : fileManager.listFiles("common/colonial_regions")) {
            try (final InputStream is = cultureStream) {
                final ColRegionParser parser = new ColRegionParser(colRegions, Long.MAX_VALUE, is);
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
        replay.countries.clear();

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
                        final CountryParser parser = new CountryParser(color, Long.MAX_VALUE, cs);
                        parser.run();
                        replay.countries.put((String) key, new CountryInfo((String) key, color.val));
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
        replay.cultures.clear();
        for(final InputStream cultureStream : fileManager.listFiles("common/cultures")) {
            try (final InputStream is = cultureStream) {
                final CulturesParser parser = new CulturesParser(new Pair<>(replay.countries, replay.cultures), Long.MAX_VALUE, is);
                parser.run();
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    /**
     * Loads data in proper order.
     */
    private void loadData() {
        System.out.printf(l10n("replay.load.data"));
        titleProperty.set(TITLE);
        fileManager.loadMods();
        loadDefines();
        loadProvinces();
        loadColRegions();
        loadMap();
        loadSeas();
        loadWastelands();
        loadCountries();
        loadCultures();
        loadReligions();
    }

    private void loadDefines() {
        System.out.printf(l10n("replay.load.defines"));
        try (final InputStream is = fileManager.getInputStream("common/defines.lua")) {
            final DefinesParser parser = new DefinesParser(defines, Long.MAX_VALUE, is);
            parser.run();
        } catch(Exception e) { e.printStackTrace(); }
    }

    /**
     * Starts loading the map from map/provinces.bmp.
     */
    private void loadMap() {
        System.out.printf(l10n("replay.map.load"));
        final Task<Void> mapLoader = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateTitle(l10n("replay.map.load"));
                try {
                    try (InputStream is = fileManager.getInputStream("map/provinces.bmp")) {
                        map = new Image(is);
                    } catch (FileNotFoundException e) {
                        System.err.printf(l10n("replay.map.notfound"));
                        map = new WritableImage(1,1);
                    }
                    reader = map.getPixelReader();

                    final int width = (int) map.getWidth();
                    final int height = (int) map.getHeight();

                    //Copy from source to destination pixel by pixel
                    output = new WritableImage(width, height);
                    replay.initBuffers(width, height);
                    buffer = replay.politicalBuffer;
                    final PixelWriter writer = output.getPixelWriter();

                    for (int y = 0; y < height; ++y){
                        for (int x = 0; x < width; ++x){
                            int color = reader.getArgb(x, y);
                            boolean border = false;
                            if (drawBorders) {
                                if (x > 0 && reader.getArgb(x-1, y) != color) {
                                    border = true;
                                } else if (x < width - 1 && reader.getArgb(x+1, y) != color) {
                                    border = true;
                                } else if (y > 0 && reader.getArgb(x, y-1) != color) {
                                    border = true;
                                } else if (y < height - 1 && reader.getArgb(x, y+1) != color) {
                                    border = true;
                                }
                            }
                            if (border) {
                                color = replay.borderColor;
                                borders.add(y * width + x);
                            } else {
                                final ProvinceInfo province = colors.get(color);
                                if (province != null) {
                                    province.points.add(y * width + x);
                                } else {
                                    System.err.printf(l10n("replay.map.unknowncolor"), x, y, color);
                                }
                            }
                            replay.politicalBuffer[y * width + x] = color;
                            replay.religiousBuffer[y * width + x] = color;
                            replay.culturalBuffer[y * width + x] = color;
                            replay.technologySeparateBuffer[y * width + x] = color;
                            replay.technologyCombinedBuffer[y * width + x] = color;
                            writer.setArgb(x, y, color);
                            updateProgress(y*width+x, height*width);
                        }
                    }

                    for(ProvinceInfo info : replay.provinces.values()) {
                        info.calculateCenter(width);
                    }

                    lock.release();
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        };
        progressBar.progressProperty().bind(mapLoader.progressProperty());
        statusLabel.textProperty().bind(mapLoader.titleProperty());
        mapLoader.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.out.printf(l10n("replay.map.loaded"));
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                statusLabel.textProperty().unbind();
                statusLabel.setText(l10n("replay.map.loaded"));
                scrollPane.setContent(null);
                imageView.setImage(output);
                scrollPane.setContent(imageView);
                int fitWidth = Integer.parseInt(settings.getProperty("map.fit.width", "0"));
                int fitHeight = Integer.parseInt(settings.getProperty("map.fit.height", "0"));
                imageView.setFitHeight(fitHeight);
                imageView.setFitWidth(fitWidth);
            }
        });
        new Thread(mapLoader, "MapLoader").start();
    }

    /**
     * Loads provinces from map/definition.csv.
     */
    private void loadProvinces() {
        System.out.printf(l10n("replay.provinces.load"));
        replay.provinces.clear();
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
                final int color = ColorUtils.toColor(
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]));
                final ProvinceInfo province = new ProvinceInfo(parts[0], parts[4], color);
                replay.provinces.put(parts[0], province);
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
            final ProvinceInfo sea = new ProvinceInfo("SEA", "SEA", ColorUtils.SEA_COLOR);
            sea.isSea = true;
            replay.provinces.put(sea.id, sea);
            colors.put(sea.color, sea);
            final ProvinceInfo wasteland = new ProvinceInfo("WASTELAND", "WASTELAND", ColorUtils.WASTELAND_COLOR);
            wasteland.isWasteland = true;
            replay.provinces.put(wasteland.id, wasteland);
            colors.put(wasteland.color, wasteland);
        }
    }

    /**
     * Loads religion colors from common/religions/*.
     */
    private void loadReligions() {
        System.out.printf(l10n("replay.load.religions"));
        replay.religions.clear();
        for(final InputStream religionStream : fileManager.listFiles("common/religions")) {
            try (final InputStream is = religionStream) {
                final ReligionsParser parser = new ReligionsParser(replay.religions, Long.MAX_VALUE, is);
                parser.run();
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    /**
     * Loads sea provinces from map/default.map.
     */
    private void loadSeas() {
        System.out.printf(l10n("replay.load.seas"));
        try (final InputStream is = new FileInputStream(eu4Directory.getPath() + "/map/default.map")) {
            final DefaultMapParser parser = new DefaultMapParser(replay.provinces, Long.MAX_VALUE, is);
            parser.run();
        } catch(Exception e) { e.printStackTrace(); }
    }

    /**
     * Loads wasteland provinces from map/climate.txt.
     */
    private void loadWastelands() {
        System.out.printf(l10n("replay.load.wastelands"));
        try (final InputStream is = new FileInputStream(eu4Directory.getPath() + "/map/climate.txt")) {
            final ClimateParser parser = new ClimateParser(replay.provinces, Long.MAX_VALUE, is);
            parser.run();
        } catch(Exception e) { e.printStackTrace(); }
    }

    /**
     * Called when application is stopped to store settings and end gif if needed.
     */
    public void stop() {
        endGif();
        final StringBuilder s = new StringBuilder();
        for(String event : replay.notableEvents) {
            s.append(";");
            s.append(event);
        }
        settings.setProperty("events", s.substring(1));
    }

    /**
     * Appends {@link #logContent} to {@link #log} asynchronously.
     */
    public void updateLog() {
        final Document doc = log.getEngine().getDocument();
        logContent.append(LOG_FOOTER);
        final WebEngine e = new WebEngine();
        e.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    Node fragmentNode = e.getDocument().getElementById(LOG_ID);
                    fragmentNode = doc.importNode(fragmentNode, true);
                    doc.getElementById(LOG_ID).appendChild(fragmentNode);
                    log.getEngine().executeScript(SCROLL_DOWN);
                }
            }
        });
        e.loadContent(logContent.toString());
        logContent.setLength(LOG_HEADER.length());

        if (selectedProvince != null) {
            final String provinceLogContent = selectedProvince.getLog();
            if (!provinceLogContent.equals(selectedProvinceLogContent)) {
                provinceLog.getEngine().loadContent(provinceLogContent);
                selectedProvinceLogContent = provinceLogContent;
            }
        }
    }

    /**
     * Listens to changes of {@link #dateGenerator} and initiates event processing.
     */
    class DateListener implements ChangeListener<Date> {

        @Override
        public void changed(final ObservableValue<? extends Date> ov, final Date oldVal, final Date newVal) {
            dateLabel.setText(newVal.toString());

            if (direction == null) {
                return;
            }
            statusLabel.textProperty().unbind();
            statusLabel.setText("");
            switch (direction) {
                case FORWARD:
                    final List<Event> forwardsEvents = saveGame.timeline.get(newVal);
                    eventProcessor.processEvents(newVal, forwardsEvents);
                    break;
                case BACKWARD:
                    final List<Event> backwardEvents = saveGame.timeline.get(newVal.next());
                    eventProcessor.unprocessEvents(newVal, backwardEvents);
                    break;
                default:
                    assert false : l10n("replay.direction.unknown");
            }
            if (giffer != null) {
                giffer.updateGif(buffer, newVal);
            }
        }
    }

    /**
     * Bridge between Javascript and JavaFX.
     * Its public methods are accessible from javascript in {@link #logContent}.
     */
    public class JavascriptBridge {

        /**
         * Centers {@link #scrollPane} to province, if possible.
         * @param prov id of the province
         * @return false to prevent link following and thus refreshing
         */
        public boolean prov(final String prov) {
            final Point center = replay.provinces.get(prov).center;
            if (center == null) {
                return false;
            }
            final Bounds imageBounds = imageView.getBoundsInParent();
            scrollPane.setHvalue(mapCoordToScrollProcent(
                    center.x, map.getWidth(), scrollPane.getWidth(), imageBounds.getWidth()));
            scrollPane.setVvalue(mapCoordToScrollProcent(
                    center.y, map.getHeight(), scrollPane.getHeight(), imageBounds.getHeight()));
            return false;
        }
    }

    /**
     * Class for jumping to specified date.
     */
    abstract class Jumper extends Task<Void> {

        /** Title when jumping. */
        protected String updateInitFormat = l10n("replay.jumping");

        /** Title when jumped. */
        protected String updateDoneFormat = l10n("replay.jumped");

        /** Last date that was processed. */
        protected Date currentDate;

        /** Previous event listener to be restored after finish. */
        protected EventProcessor.Listener prevListener;

        /**
         * Only constructor.
         */
        public Jumper() {
            prevListener = eventProcessor.getListener();
            eventProcessor.setListener(getEventListener());
            this.stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue<? extends State> ov, State oldVal, State newVal) {
                    if (newVal == State.SUCCEEDED || newVal == State.CANCELLED) {
                        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
                        final WebEngine e = log.getEngine();
                        e.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                            @Override
                            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                                if (newState == State.SUCCEEDED) {
                                    log.getEngine().executeScript(SCROLL_DOWN);
                                    logContent.setLength(LOG_HEADER.length());
                                    e.getLoadWorker().stateProperty().removeListener(this);
                                    dateGenerator.skipTo(getCurrentDate());
                                    statusLabel.textProperty().unbind();
                                    dateLabel.textProperty().set(dateGenerator.dateProperty().get().toString());
                                    jumpButton.setVisible(false);
                                    finalizer = null;
                                    lock.release();
                                }
                            }
                        });
                        e.loadContent(String.format(LOG_INIT_FORMAT, logContent.toString()));
                    } else if (newVal == State.FAILED) {
                        getException().printStackTrace();
                    }
                }
            });
        }

        abstract protected EventProcessor.Listener getEventListener();

        abstract protected Date getBound();

        abstract protected Date getCurrentDate();

        abstract protected Date getIter();

        abstract protected Date getTarget();

        abstract protected Date iterNext(final Date iter);

        abstract protected void processEvents(final Date date, final List<Event> events);

        abstract protected int updateDay(final int day);

        @Override
        protected final Void call() throws Exception {
            Date iter = getIter();
            final Date target = getTarget();
            final Date bound = getBound();
            updateTitle(String.format(updateInitFormat, target));

            int day = Date.calculateDistance(saveGame.startDate, iter);
            final int distance = Date.calculateDistance(saveGame.startDate, saveGame.date) + 1;
            while (!iter.equals(bound) && !isCancelled()) {
                final List<Event> events = saveGame.timeline.get(iter);
                processEvents(iter, events);
                day = updateDay(day);
                updateProgress(day, distance);
                currentDate = iter;
                iter = iterNext(iter);
            }
            if (!isCancelled()) {
                updateTitle(String.format(updateDoneFormat, target));
            } else {
                updateTitle(l10n("replay.cancel"));
            }
            eventProcessor.setListener(prevListener);
            return null;
        }
    }
}
