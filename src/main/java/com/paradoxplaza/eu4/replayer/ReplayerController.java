package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.parser.defaultmap.DefaultMapParser;
import com.paradoxplaza.eu4.replayer.events.Controller;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.parser.savegame.SaveGameParser;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.animation.Animation.Status;
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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import netscape.javascript.JSObject;

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

    /**
     * R, G, B to argb.
     * @param red
     * @param green
     * @param blue
     * @return argb
     */
    static int toColor(final int red, final int green, final int blue) {
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

    /** How many pixels are added to width and height when zooming in/out. */
    int zoomStep;

    /** Directory containing save games. */
    File saveDirectory;

    /** Directory containing needed game files. */
    File eu4Directory;

    /** Property binded to stage titleProperty. */
    StringProperty titleProperty = new SimpleStringProperty(TITLE);

    /** Tag -> color mapping. */
    Map<String, Integer> countries = new HashMap<>();

    /** ID -> color mapping. */
    Map<String, ProvinceInfo> provinces = new HashMap<>();

    /** Color -> ID mapping. */
    Map<Integer, String> colors = new HashMap<>();

    /** Set of colors assigned to sea provinces. */
    Set<Integer> seas = new HashSet<>();

    /** Loaded save game to be replayed. */
    SaveGame saveGame;

    /** Color used to display sea and lakes. */
    int seaColor;

    /** Color to display no man's land. */
    int landColor;

    /** Timer for replaying. */
    Timeline timeline;

    /** Generates dates for replaying dates. */
    DateGenerator dateGenerator;

    /** Listens to date changes and initiates event processing. */
    final DateListener dateListener = new DateListener();

    /** Content of log area with html code. */
    StringBuilder logContent = new StringBuilder();

    @FXML
    private void changeEU4Directory() {
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
    }

    @FXML
    private void close() {
        Platform.exit();
    }

    @FXML
    private void load() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(saveDirectory);
        fileChooser.setTitle("Select EU4 save to replay");

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("EU4 saves (*.eu4)", "*.eu4");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        final File file = fileChooser.showOpenDialog(getWindow());
        if (file == null) {
            return;
        }

        saveDirectory = file.getParentFile();
        settings.setProperty("save.dir", saveDirectory.getPath());
        titleProperty.setValue(String.format(TITLE_SAVEGAME, file.getName()));
        saveGame = new SaveGame();

        try {
            final InputStream is = new FileInputStream(file);
            final SaveGameParser parser = new SaveGameParser(saveGame, file.length(), is);
            final int width = (int) map.getWidth();
            final int height = (int) map.getHeight();
            buffer = new int[width * height];
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
                    final PixelWriter writer = output.getPixelWriter();

                    final long size = height * width;
                    for (int y = 0; y < height; ++y) {
                        for (int x = 0; x < width; ++x) {
                            final int color = reader.getArgb(x, y);
                            buffer[y * width + x] = seas.contains(color) ? seaColor : landColor;
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
                    new Thread(parser).start();
                }
            });

            final Task<Void> starter = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    dateGenerator = new DateGenerator(saveGame.startDate, saveGame.date);
                    updateTitle("Initializing starting date...");
                    processEvents(null, new ProgressIterable<>(saveGame.timeline.get(null)), false);

                    return null;
                }
            };

            starter.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    log.getEngine().loadContent(logContent.toString());
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
                    new JavascriptBridge().prov(settings.getProperty("center", "1"));
                }
            });

            parser.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    dateLabel.textProperty().bind(starter.titleProperty());
                    progressBar.progressProperty().bind(starter.progressProperty());
                    new Thread(starter).start();
                }
            });

            dateLabel.textProperty().bind(mapInitializer.titleProperty());
            progressBar.progressProperty().bind(mapInitializer.progressProperty());
            new Thread(mapInitializer).start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }

    @FXML
    private void play() {
        if (saveGame == null) {
            return;
        }
        if (timeline != null && timeline.getStatus() != Status.RUNNING) {
            timeline.play();
            return;
        }
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(0.1),
                  new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        if (dateGenerator.hasNext()) {
                            dateGenerator.next();
                        } else {
                            timeline.stop();
                            timeline = null;
                        }
                      }
                }));
        timeline.playFromStart();
    }

    @FXML
    private void refresh() {
        scrollPane.setContent(null);
        imageView.setImage(null);
        int width = (int) output.getWidth();
        int height = (int) output.getHeight();
        WritableImage i = new WritableImage(width, height);
        i.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbPreInstance(), buffer, 0, width);
        output = i;
        imageView.setImage(i);
        scrollPane.setContent(imageView);
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
        log.prefWidthProperty().bind(logContainer.widthProperty());
        progressBar.prefWidthProperty().bind(bottom.widthProperty());

        final WebEngine webEngine = log.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                        JSObject win = (JSObject) webEngine.executeScript("window");
                        win.setMember("java", new JavascriptBridge());
                }
            }
        });
        webEngine.setJavaScriptEnabled(true);
        logContent.append("<body onload=\"window.scrollTo(0,document.body.scrollHeight)\">");
        webEngine.loadContent(logContent.toString());
    }

    /**
     * Sets Replayer settings. Adjusts saveDirectory etc. and loads map.
     * @param settings new settings
     */
    public void setSettings(final Properties settings) {
        this.settings = settings;

        zoomStep = Integer.parseInt(settings.getProperty("zoom.step", "100"));

        saveDirectory = new File(settings.getProperty("save.dir", "/"));
        if (!saveDirectory.exists() || !saveDirectory.isDirectory()) {
            saveDirectory = null;
        }

        eu4Directory = new File(settings.getProperty("eu4.dir"));
        loadData();
    }

    public StringProperty titleProperty() {
        return titleProperty;
    }

    private Window getWindow() {
        return root.getScene().getWindow();
    }

    private void loadCountries() {
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
                                        toColor(
                                            Integer.parseInt(m.group(1)),
                                            Integer.parseInt(m.group(2)),
                                            Integer.parseInt(m.group(3))));
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

    private void loadData() {
        titleProperty.set(TITLE);
        loadProvinces();
        loadMap();
        loadSeas();
        loadCountries();
    }

    private void loadMap() {
        dateLabel.textProperty().unbind();
        dateLabel.setText("Loading map...");
        final Task<Void> mapLoader = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                InputStream is = null;
                try {
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
                buffer = new int[width*height];
                bufferWidth = width;
                bufferHeight = height;
                final PixelWriter writer = output.getPixelWriter();

                for (int y = 0; y < height; ++y){
                    for (int x = 0; x < width; ++x){
                        final int color = reader.getArgb(x, y);
                        provinces.get(colors.get(color)).points.add(y * width + x);
                        buffer[y * width + x] = color;
                        writer.setArgb(x, y, color);
                        updateProgress(y*width+x, height*width);
                    }
                }

                for(ProvinceInfo info : provinces.values()) {
                    info.calculateCenter(width);
                }

                return null;
            }
        };
        progressBar.progressProperty().bind(mapLoader.progressProperty());
        mapLoader.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                dateLabel.setText("");
                imageView.setImage(output);
                int fitWidth = Integer.parseInt(settings.getProperty("map.fit.width", "0"));
                int fitHeight = Integer.parseInt(settings.getProperty("map.fit.height", "0"));
                imageView.setFitHeight(fitHeight);
                imageView.setFitWidth(fitWidth);
                scrollPane.setContent(null);
                scrollPane.setContent(imageView);
            }
        });
        new Thread(mapLoader).start();
    }

    private void loadProvinces() {
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
                provinces.put(parts[0], new ProvinceInfo(color));
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

    private void loadSeas() {
        seas.clear();
        try (final InputStream is = new FileInputStream(eu4Directory.getPath() + "/map/default.map")) {
            final DefaultMapParser parser = new DefaultMapParser(new Pair<>(seas, provinces), Long.MAX_VALUE, is);
            parser.run();
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void processEvents(final Date date, final Iterable<Event> events, final boolean appendToLog) {
        if (events == null) {
            System.out.println(String.format("[%1$s]: %2$s", date, "nothing happened"));
            return;
        }
        final PixelWriter writer = output.getPixelWriter();
        final int width = (int) map.getWidth();
        for(Event event : events) {
            System.out.println(String.format("[%1$s]: %2$s", date, event));
            logContent.append(String.format("[%1$s]: %2$#s<br>", date, event));
            if (event instanceof Controller) {
                final com.paradoxplaza.eu4.replayer.events.Controller controller = (com.paradoxplaza.eu4.replayer.events.Controller) event;
                for(int p : provinces.get(controller.id).points) {
                    if ( p / width % 2 == 0) {
                        Integer color = countries.get(controller.tag);
                        buffer[p] = color == null ? landColor : color;
                        writer.setArgb(p % width, p / width, color == null ? landColor : color);
                    }
                }
            } else if (event instanceof Owner) {
                final com.paradoxplaza.eu4.replayer.events.Owner owner = (com.paradoxplaza.eu4.replayer.events.Owner) event;
                for(int p : provinces.get(owner.id).points) {
                    //if ( p % bufferWidth2 == 1) {
                        Integer color = countries.get(owner.tag);
                        buffer[p] = color == null ? landColor : color;
                        writer.setArgb(p % width, p / width, color == null ? landColor : color);
                    //}
                }
            }
        }
        if (appendToLog) {
            log.getEngine().loadContent(logContent.toString());
        }
    }

    /**
     * Listens to changes of {@link #dateGenerator} and initiates event processing.
     */
    class DateListener implements ChangeListener<Date> {

        @Override
        public void changed(final ObservableValue<? extends Date> ov, final Date oldVal, final Date newVal) {
            final List<Event> events = saveGame.timeline.get(newVal);
            processEvents(newVal, events, true);
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
            scrollPane.setHvalue(mapCoordToScrollProcent(
                    center.x, map.getWidth(), scrollPane.getWidth(), imageView.getFitWidth()));
            scrollPane.setVvalue(mapCoordToScrollProcent(
                    center.y, map.getHeight(), scrollPane.getHeight(), imageView.getFitHeight()));
            return false;
        }
    }
}
