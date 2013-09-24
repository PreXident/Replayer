package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.parser.defaultmap.DefaultMapParser;
import com.paradoxplaza.eu4.replayer.parser.religion.ReligionsParser;
import com.paradoxplaza.eu4.replayer.parser.savegame.SaveGameParser;
import com.paradoxplaza.eu4.replayer.utils.GifSequenceWriter;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
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
    static final String LOG_INIT_FORMAT = String.format("<body><div id='%s'/>%%s</body>", LOG_ID);

    /** Initial content of log. */
    static final String LOG_INIT = String.format(LOG_INIT_FORMAT, "");

    /** JavaScript call to scroll to the bottom of the page. */
    static final String SCROLL_DOWN = "window.scrollTo(0,document.body.scrollHeight)";

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
    Label dateLabel;

    @FXML
    VBox bottom;

    @FXML
    ProgressBar progressBar;

    @FXML
    Menu eventMenu;

    @FXML
    ComboBox<String> daysCombo;

    /** Lock to prevent user input while background processing. */
    final Semaphore lock = new Semaphore(1);

    /** Replayer settings. */
    Properties settings;

    /** Original map picture. */
    Image map;

    /** Reader for {@link #map}. */
    PixelReader reader;

    /** Image displayed in {@link #imageView}. */
    WritableImage output;

    /** Writer for {@link #output}. */
//    PixelWriter writer;

    /** Buffer for emergency refresh. */
    int[] buffer;

    /** Buffer width. */
    int bufferWidth;

    /** Buffer height. */
    int bufferHeight;

    /** Buffer with political map. */
    int[] politicalBuffer;

    /** Buffer with religious map. */
    int[] religiousBuffer;

    /** How many pixels are added to width and height when zooming in/out. */
    int zoomStep;

    /** Directory containing save games. */
    File saveDirectory;

    /** Directory containing needed game files. */
    File eu4Directory;

    /** Property binded to stage titleProperty. */
    StringProperty titleProperty = new SimpleStringProperty(TITLE);

    /** Tag -> country mapping. */
    Map<String, CountryInfo> countries = new HashMap<>();

    /** ID -> province mapping. */
    Map<String, ProvinceInfo> provinces = new HashMap<>();

    /** Province color -> ID mapping. */
    Map<Integer, String> colors = new HashMap<>();

    /** Set of colors assigned to sea provinces. */
    Set<Integer> seas = new HashSet<>();

    /** Religion name -> color mapping. */
    Map<String, Integer> religions = new HashMap<>();

    /** Loaded save game to be replayed. */
    SaveGame saveGame;

    /** Color used to display sea and lakes. */
    int seaColor;

    /** Color to display no man's land. */
    int landColor;

    /** Number of days processed in one tick. */
    int daysPerTick;

    /** Timer for replaying. */
    Timeline timeline;

    /** Generates dates for replaying dates. */
    DateGenerator dateGenerator;

    /** Listens to date changes and initiates event processing. */
    final DateListener dateListener = new DateListener();

    /** Content of log area with html code. */
    final StringBuilder logContent = new StringBuilder();

    /** Set of currently notable events. */
    final Set<String> notableEvents = new HashSet<>();

    /** Writer of gif output. */
    GifSequenceWriter gifWriter = null;

    /** Outputstream of gif picture. */
    ImageOutputStream gifOutput = null;

    /** Buffered image representation of {@link #buffer}. */
    BufferedImage gifBufferedImage = null;

    /** Sized representation of {@link #gifBufferedImage}. */
    BufferedImage gifSizedImage = null;

    /** Period in ms between two frames in gif. */
    int gifStep;

    /** Standard event processor. */
    final EventProcessor eventProcessor = new EventProcessor(this);

    /** Event processor that does not update log. */
    final EventProcessor notLogUpdatingProcessor = new EventProcessor(this) {
        @Override
        protected void updateLog() { }
    };

    /** Event processor that does not update log nor {@link #output}. */
    final EventProcessor bufferChangeOnlyProcessor = new EventProcessor(this) {
        @Override
        protected void setColor(int[] buffer, final int pos, final int color) {
            buffer[pos] = color;
        }
        @Override
        protected void updateLog() { }
    };

    @FXML
    private void changeEU4Directory() throws InterruptedException{
        if (!lock.tryAcquire()) {
           return;
        }
        pause();
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select EU4 directory");
        if (eu4Directory != null && eu4Directory.exists() && eu4Directory.isDirectory()) {
            directoryChooser.setInitialDirectory(eu4Directory);
        }
        File dir = directoryChooser.showDialog(getWindow());
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
    private void finish() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (saveGame == null || !dateGenerator.hasNext()) {
            lock.release();
            return;
        }
        pause();
        final Task<Void> finalizer = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateTitle("Finishing gameplay...");

                    final Date maxDate = saveGame.date;
                    Date date = dateGenerator.dateProperty().get();
                    int day = Date.calculateDistance(saveGame.startDate, date);
                    final int distance = Date.calculateDistance(saveGame.startDate, saveGame.date);
                    int counter = 0;
                    while (date.compareTo(maxDate) < 0) {
                        final List<Event> events = saveGame.timeline.get(date);
                        bufferChangeOnlyProcessor.processEvents(date, events);
                        updateProgress(++day, distance);
                        date = date.next();
                        if (++counter >= daysPerTick) {
                            updateGif(date);
                            counter = 0;
                        }
                    }
                    updateTitle(date.toString());
                    return null;
                }
            };
            finalizer.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    output.getPixelWriter().setPixels(0, 0, bufferWidth, bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, bufferWidth);
                    final WebEngine e = log.getEngine();
                    e.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                        @Override
                        public void changed(ObservableValue ov, State oldState, State newState) {
                            if (newState == State.SUCCEEDED) {
                                log.getEngine().executeScript(SCROLL_DOWN);
                                dateGenerator = new DateGenerator(saveGame.date, saveGame.date);
                                logContent.setLength(LOG_HEADER.length());
                                endGif();
                                e.getLoadWorker().stateProperty().removeListener(this);
                                lock.release();
                            }
                        }
                    });
                    e.loadContent(String.format(LOG_INIT_FORMAT, logContent.toString()));
                }
            });
         dateLabel.textProperty().bind(finalizer.titleProperty());
         progressBar.progressProperty().bind(finalizer.progressProperty());
         new Thread(finalizer, "Game finalizer").start();
    }

    @FXML
    private void load() throws InterruptedException {
        if (!lock.tryAcquire()) {
           return;
        }
        pause();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(saveDirectory);
        fileChooser.setTitle("Select EU4 save to replay");

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("EU4 saves (*.eu4)", "*.eu4");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        final File file = fileChooser.showOpenDialog(getWindow());
        if (file == null) {
            lock.release();
            return;
        }

        saveDirectory = file.getParentFile();
        settings.setProperty("save.dir", saveDirectory.getPath());
        titleProperty.setValue(String.format(TITLE_SAVEGAME, file.getName()));
        saveGame = new SaveGame();
        for (CountryInfo ci : countries.values()) {
            ci.controls.clear();
            ci.owns.clear();
            ci.expectingTagChange = null;
        }

        try {
            final InputStream is = new FileInputStream(file);
            final SaveGameParser parser = new SaveGameParser(saveGame, file.length(), is);
            final int width = (int) map.getWidth();
            final int height = (int) map.getHeight();
//            buffer = new int[width * height];
//            politicalBuffer = buffer;
            imageView.setImage(null);
            final Task<Void> mapInitializer = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateTitle("Initializing map...");
                    final int width = (int) map.getWidth();
                    final int height = (int) map.getHeight();
                    seaColor = toColor(
                            Integer.parseInt(settings.getProperty("sea.color.red", "0")),
                            Integer.parseInt(settings.getProperty("sea.color.green", "0")),
                            Integer.parseInt(settings.getProperty("sea.color.blue", "255")));
                    landColor = toColor(
                            Integer.parseInt(settings.getProperty("land.color.red", "150")),
                            Integer.parseInt(settings.getProperty("land.color.green", "150")),
                            Integer.parseInt(settings.getProperty("land.color.blue", "150")));
                    //final PixelWriter writer = output.getPixelWriter();

                    final long size = height * width;
                    for (int y = 0; y < height; ++y) {
                        for (int x = 0; x < width; ++x) {
                            final int color = reader.getArgb(x, y);
                            politicalBuffer[y * width + x] = seas.contains(color) ? seaColor : landColor;
                            religiousBuffer[y * width + x] = seas.contains(color) ? seaColor : landColor;
                            //writer.setArgb(x, y, seas.contains(color) ? seaColor : landColor);
                            updateProgress(y*width+x, size);
                        }
                    }
                    return null;
                }
            };
            mapInitializer.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    output.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbPreInstance(), buffer, 0, width);
                    dateLabel.textProperty().bind(parser.titleProperty());
                    progressBar.progressProperty().bind(parser.progressProperty());
                    new Thread(parser, "parser").start();
                }
            });

            final Task<Void> starter = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    dateGenerator = new DateGenerator(saveGame.startDate, saveGame.date);
                    updateTitle("Initializing world...");
                    notLogUpdatingProcessor.processEvents(null, new ProgressIterable<>(saveGame.timeline.get(null)));
                    //
                    updateTitle("Progressing to starting date...");
                    final Date maxDate = saveGame.startDate;
                    Date date = new Date(settings.getProperty("init.start", "1300.1.1"));
                    int day = 0;
                    final int distance = Date.calculateDistance(date, saveGame.startDate);
                    while (date.compareTo(maxDate) < 0) {
                        final List<Event> events = saveGame.timeline.get(date);
                        bufferChangeOnlyProcessor.processEvents(date, events);
                        updateProgress(++day, distance);
                        date = date.next();
                    }
                    return null;
                }
            };

            starter.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    if ("true".equals(settings.getProperty("gif"))) {
                        try {
                            final File gifOutputFile = new File(file.getAbsolutePath() + ".gif");
                            gifOutputFile.delete();
                            gifOutput = new FileImageOutputStream(gifOutputFile);
                            gifWriter = new GifSequenceWriter(gifOutput, BufferedImage.TYPE_INT_ARGB, gifStep, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        updateGif(saveGame.startDate);
                    }
                    output.getPixelWriter().setPixels(0, 0, bufferWidth, bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, bufferWidth);
                    log.getEngine().loadContent(String.format(LOG_INIT_FORMAT, logContent.toString()));
                    logContent.setLength(LOG_HEADER.length());
                    progressBar.progressProperty().bind(dateGenerator.progress);
                    dateGenerator.dateProperty().addListener(dateListener);
                    dateLabel.textProperty().bind(new StringBinding() {
                        {
                            bind(dateGenerator.date);
                        }
                        @Override
                        protected String computeValue() {
                            return dateGenerator.date.get().toString();
                        }
                    });
                    imageView.setImage(output);
                    new JavascriptBridge().prov(settings.getProperty("center.id", "1"));
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
                    dateLabel.textProperty().bind(starter.titleProperty());
                    progressBar.progressProperty().bind(starter.progressProperty());
                    for (Map.Entry<String, Date> change : saveGame.tagChanges.entrySet()) {
                        countries.get(change.getKey()).expectingTagChange = change.getValue();
                    }
                    new Thread(starter, "starter").start();
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

            dateLabel.textProperty().bind(mapInitializer.titleProperty());
            progressBar.progressProperty().bind(mapInitializer.progressProperty());
            new Thread(mapInitializer, "mapInitializer").start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void pause() {
        if (timeline != null) {
            timeline.pause();
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
            timeline.play();
            lock.release();
            return;
        }

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(0.1),
                  new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        for (int i = 0; i < daysPerTick; ++i) {
                            if (dateGenerator.hasNext()) {
                                dateGenerator.next();
                            } else {
                                timeline.stop();
                                timeline = null;
                                //endGif();
                                break;
                            }
                        }
                        //updateGif();
                      }
                }));
        timeline.playFromStart();
        lock.release();
    }

    @FXML
    private void politicalMapMode() {
        buffer = politicalBuffer;
        output.getPixelWriter().setPixels(0, 0, bufferWidth, bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, bufferWidth);
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
        buffer = religiousBuffer;
        output.getPixelWriter().setPixels(0, 0, bufferWidth, bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, bufferWidth);
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
    private void zoomIn() {
        imageView.setFitHeight(imageView.getFitHeight() + zoomStep);
        imageView.setFitWidth(imageView.getFitWidth() + zoomStep);
    }

    @FXML
    private void zoomOut() {
        double h = imageView.getFitHeight() - zoomStep;
        imageView.setFitHeight(h < 0 ? imageView.getFitHeight() : h);
        double w = imageView.getFitWidth() - zoomStep;
        imageView.setFitWidth(w < 0 ? imageView.getFitWidth() : w);
    }

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        System.out.printf("Initializing...\n");
        log.prefWidthProperty().bind(logContainer.widthProperty());
        progressBar.prefWidthProperty().bind(bottom.widthProperty());

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
                                notableEvents.add(event);
                            } else {
                                notableEvents.remove(event);
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
                    daysPerTick = Integer.parseInt(newVal);
                    settings.setProperty("days.per.tick", newVal);
                } catch (NumberFormatException e) {
                    daysCombo.setValue(oldVal);
                }
            }
        });

        imageView.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                final Bounds imageBounds = imageView.getBoundsInParent();
                int x = (int) (t.getX() * bufferWidth / imageBounds.getWidth());
                int y = (int) (t.getY() * bufferHeight / imageBounds.getHeight());
                final String provinceHint = provinces.get(colors.get(reader.getArgb(x, y))).toString();
                if (!scrollPane.getTooltip().getText().equals(provinceHint)) {
                    scrollPane.setTooltip(new Tooltip(provinceHint));
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

        final ClassLoader loader = getClass().getClassLoader();
        notableEvents.clear();
        notableEvents.addAll(Arrays.asList(settings.getProperty("events", "").split(";")));
        for(final MenuItem item : eventMenu.getItems()) {
            if (item instanceof CustomMenuItem) {
                final String event = item.getText();
                final CustomMenuItem customItem = ((CustomMenuItem) item);
                if (customItem.getContent() instanceof CheckBox) {
                    final CheckBox checkBox = (CheckBox) customItem.getContent();
                    checkBox.setSelected(notableEvents.contains(event));
                }
            }
        }

        zoomStep = Integer.parseInt(settings.getProperty("zoom.step", "100"));

        daysCombo.getItems().addAll(settings.getProperty("list.days.per.tick", "1;30;365").split(";"));
        daysCombo.getSelectionModel().select(settings.getProperty("days.per.tick", "1"));

        gifStep = Integer.parseInt(settings.getProperty("gif.step", "100"));

        saveDirectory = new File(settings.getProperty("save.dir", "/"));
        if (!saveDirectory.exists() || !saveDirectory.isDirectory()) {
            saveDirectory = null;
        }

        eu4Directory = new File(settings.getProperty("eu4.dir"));
        try {
            lock.acquire();
            loadData();
        } catch (InterruptedException e) { }
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

    void endGif() {
        if (gifWriter != null) {
            try {
                gifWriter.close();
                gifOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            gifWriter = null;
            gifOutput = null;
        }
    }

    /**
     * Loads countries from files inside /common/country_tags directory
     * and files mentioned in them.
     */
    private void loadCountries() {
        System.out.printf("Loading countries...\n");
        countries.clear();
        final File countryTagDir = new File(eu4Directory + "/common/country_tags");
        for(final File tagFile : countryTagDir.listFiles()) {
            if (!tagFile.isFile()) {
                continue;
            }

            try (final InputStream is = new FileInputStream(tagFile)) {
                final Properties tags = new Properties();
                tags.load(is);
                for (Object key : tags.keySet()) {
                    String path = ((String) tags.get(key)).trim();
                    //get rid of "
                    path = path.substring(1, path.length() - 1);
                    try (final BufferedReader countryReader = new BufferedReader(new FileReader(eu4Directory + "/common/" + path))) {
                        String line = countryReader.readLine();
                        while (line != null) {
                            final Matcher m = TAG_COLOR_PATTERN.matcher(line);
                            if (m.matches()) {
                                countries.put((String)key,
                                        new CountryInfo((String) key,
                                            toColor(
                                                Integer.parseInt(m.group(1)),
                                                Integer.parseInt(m.group(2)),
                                                Integer.parseInt(m.group(3)))));
                                break;
                            }
                            line = countryReader.readLine();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads data in proper order.
     */
    private void loadData() {
        System.out.printf("Loading data:\n");
        titleProperty.set(TITLE);
        loadProvinces();
        loadMap();
        loadSeas();
        loadCountries();
        loadReligions();
    }

    /**
     * Starts loading the map from map/provinces.bmp.
     */
    private void loadMap() {
        dateLabel.textProperty().unbind();
        dateLabel.setText("Loading map...");
        final Task<Void> mapLoader = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    InputStream is = null;
                    try {
                        System.out.printf("Map file: %s\n", eu4Directory.getPath() + "/map/provinces.bmp");
                        is = new FileInputStream(eu4Directory.getPath() + "/map/provinces.bmp");
                        map = new Image(is);
                    } catch (FileNotFoundException e) {
                        System.err.println("File map/provinces.bmp not found!");
                        map = new WritableImage(1,1);
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) { }
                        }
                    }
                    reader = map.getPixelReader();

                    final int width = (int) map.getWidth();
                    final int height = (int) map.getHeight();

                    //Copy from source to destination pixel by pixel
                    output = new WritableImage(width, height);
                    if ("true".equals(settings.getProperty("gif"))) {
                        gifBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        int gifWidth = width;
                        int gifHeight = height;
                        try {
                            gifWidth = Integer.parseInt(settings.getProperty("gif.width"));
                            gifHeight = Integer.parseInt(settings.getProperty("gif.height"));
                        } catch (Exception e) { }
                        gifSizedImage = new BufferedImage(gifWidth, gifHeight, BufferedImage.TYPE_INT_ARGB);
    //                    buffer = ((DataBufferInt)gifBufferedImage.getRaster().getDataBuffer()).getData();
                    } /*else {
                        buffer = new int[width*height];
                    }*/
                    politicalBuffer = new int[width*height];
                    religiousBuffer = new int[width*height];
                    buffer = politicalBuffer;
                    bufferWidth = width;
                    bufferHeight = height;
                    final PixelWriter writer = output.getPixelWriter();

                    for (int y = 0; y < height; ++y){
                        for (int x = 0; x < width; ++x){
                            final int color = reader.getArgb(x, y);
                            provinces.get(colors.get(color)).points.add(y * width + x);
                            politicalBuffer[y * width + x] = color;
                            religiousBuffer[y * width + x] = color;
                            writer.setArgb(x, y, color);
                            updateProgress(y*width+x, height*width);
                        }
                    }

                    for(ProvinceInfo info : provinces.values()) {
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
        mapLoader.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("Map loaded");
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                dateLabel.setText("Map loaded");
                scrollPane.setContent(null);
                imageView.setImage(output);
                scrollPane.setContent(imageView);
                int fitWidth = Integer.parseInt(settings.getProperty("map.fit.width", "0"));
                int fitHeight = Integer.parseInt(settings.getProperty("map.fit.height", "0"));
                imageView.setFitHeight(fitHeight);
                imageView.setFitWidth(fitWidth);
            }
        });
        new Thread(mapLoader, "mapLoader").start();
    }

    /**
     * Loads provinces from map/definition.csv.
     */
    private void loadProvinces() {
        System.out.printf("Loading provinces...\n");
        provinces.clear();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(eu4Directory.getPath() + "/map/definition.csv"));
            //skip first line
            String line = reader.readLine();
            line = reader.readLine();
            while (line != null) {
                final String[] parts = line.split(";");
                final int color = toColor(
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]));
                provinces.put(parts[0], new ProvinceInfo(parts[0], parts[4], color));
                final String original = colors.put(color, parts[0]);
                if (original != null) {
                    throw new RuntimeException(String.format("Provinces %1$s and %2$s share a color!", parts[0], original));
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            System.err.println("File map/definition.csv not found!");
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
    }

    /**
     * Loads religion colors from common/religions/*.
     */
    private void loadReligions() {
        System.out.printf("Loading religions...\n");
        religions.clear();
        final File religionDir = new File(eu4Directory + "/common/religions");
        for(final File religionFile : religionDir.listFiles()) {
            if (!religionFile.isFile()) {
                continue;
            }

            try (final InputStream is = new FileInputStream(religionFile)) {
                final ReligionsParser parser = new ReligionsParser(religions, religionFile.length(), is);
                parser.run();
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    /**
     * Loads sea provinces from map/default.map.
     */
    private void loadSeas() {
        System.out.printf("Loading seas...\n");
        seas.clear();
        try (final InputStream is = new FileInputStream(eu4Directory.getPath() + "/map/default.map")) {
            final DefaultMapParser parser = new DefaultMapParser(new Pair<>(seas, provinces), Long.MAX_VALUE, is);
            parser.run();
        } catch(Exception e) { e.printStackTrace(); }
    }

    /**
     * Called when application is stopped to store settings and end gif if needed.
     */
    public void stop() {
        endGif();
        final StringBuilder s = new StringBuilder();
        for(String event : notableEvents) {
            s.append(";");
            s.append(event);
        }
        settings.setProperty("events", s.substring(1));
    }

    void updateGif(final Date date) {
        final int[] a = ( (DataBufferInt) gifBufferedImage.getRaster().getDataBuffer() ).getData();
        System.arraycopy(buffer, 0, a, 0, buffer.length);
        final Graphics g = gifSizedImage.getGraphics();
        g.drawImage(gifBufferedImage, 0, 0, gifSizedImage.getWidth(), gifSizedImage.getHeight(), null);
        g.setColor(Color.BLACK);
        g.drawString(date.toString(), 60, 60);
        g.dispose();
        try {
            gifWriter.writeToSequence(gifSizedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            public void changed(ObservableValue ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    Node fragmentNode = e.getDocument().getElementById(LOG_ID);
                    fragmentNode = doc.importNode(fragmentNode, true);
                    doc.getElementById(LOG_ID).appendChild(fragmentNode);
                    log.getEngine().executeScript(SCROLL_DOWN);
                    logContent.setLength(LOG_HEADER.length());
                }
            }
        });
        e.loadContent(logContent.toString());
    }

    /**
     * Listens to changes of {@link #dateGenerator} and initiates event processing.
     */
    class DateListener implements ChangeListener<Date> {

        @Override
        public void changed(final ObservableValue<? extends Date> ov, final Date oldVal, final Date newVal) {
            final List<Event> events = saveGame.timeline.get(newVal);
            eventProcessor.processEvents(newVal, events);
            //timeline.pause();
        }
    }

    /**
     * Bridge between Javascript and JavaFX.
     * Its public methods are accessible from javascript in {@link #logContent}.
     */
    public class JavascriptBridge {

        /**
         * Translates {@link #map} coordinate to {@link #scrollPane} procentual HValue/VValue.
         * @param mapCoord map coordinate
         * @param mapSize size of map
         * @param scrollSize size of scrollPane
         * @param imageViewSize size of imageView
         * @return scrollPane procentual scroll position
         */
        private double mapCoordToScrollProcent(final int mapCoord, final double mapSize, final double scrollSize, final double imageViewSize) {
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
         * Centers {@link #scrollPane} to province, if possible.
         * @param prov id of the province
         * @return false to prevent link following and thus refreshing
         */
        public boolean prov(final String prov) {
            final Point center = provinces.get(prov).center;
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
}
