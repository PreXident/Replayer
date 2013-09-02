package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.defaultmap.DefaultMapParser;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
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

    /** Replayer settings. */
    Properties settings;

    /** Original map picture. */
    Image map;

    /** Reader for {@link #map}. */
    PixelReader reader;

    /** Image displayed in {@link #imageView}. */
    WritableImage output;

    /** Writer for {@link #output}. */
    PixelWriter writer;

    /** How many pixels are added to width and height when zooming in/out */
    int zoomStep;

    /** Directory containing save games. */
    File saveDirectory;

    /** Directory containing needed game files. */
    File eu4Directory;

    /** Property binded to stage titleProperty. */
    StringProperty titleProperty = new SimpleStringProperty(TITLE);

    /** Tag -> color mapping. */
    Map<String, Color> countries = new HashMap<>();

    /** ID -> color mapping. */
    Map<String, ProvinceInfo> provinces = new HashMap<>();

    /** Color -> ID mapping. */
    Map<Color, String> colors = new HashMap<>();

    /** Set of colors assigned to sea provinces. */
    Set<Color> seas = new HashSet<>();

    /** Loaded save game to be replayed. */
    SaveGame saveGame;

    /** Color used to display sea and lakes. */
    Color seaColor;

    /** Color to display no man's land. */
    Color landColor;

    /** Timer for replaying. */
    Timeline timeline;

    /** Generates dates for replaying dates. */
    DateGenerator dateGenerator;

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

        initMap();
        saveDirectory = file.getParentFile();
        settings.setProperty("save.dir", saveDirectory.getPath());
        titleProperty.setValue(String.format(TITLE_SAVEGAME, file.getName()));

        writer = output.getPixelWriter();
        saveGame = new SaveGame();
        final SaveGameParser parser = new SaveGameParser(saveGame);
        try (final InputStream is = new FileInputStream(file)) {
            parser.parse(is);
            for(Event event : saveGame.timeline.get(null)) {
                processEvent(null, event);
            }
        } catch(Exception e) { e.printStackTrace(); }
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
        dateGenerator = new DateGenerator(saveGame.startDate, saveGame.date);
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(0.01),
                  new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        if (dateGenerator.hasNext()) {
                            final Date date = dateGenerator.next();
                            List<Event> list = saveGame.timeline.get(date);
                            if (list == null) {
                                System.out.println(String.format("[%1$s]: %2$s", date, "nothing happened"));
                                return;
                            }
                            for(Event event : list) {
                                processEvent(date, event);
                            }
                            timeline.pause();
                        } else {
                            timeline.stop();
                            timeline = null;
                        }
                      }
                }));
        timeline.playFromStart();
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
        //logContent.append("<a href=\"#\" onclick=\"return java.prov(this.textContent)\">1</a>");
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

    private void initMap() {
        final int width = (int) map.getWidth();
        final int height = (int) map.getHeight();
        seaColor = new Color(
                Double.parseDouble(settings.getProperty("sea.color.red", "0"))/255,
                Double.parseDouble(settings.getProperty("sea.color.green", "0"))/255,
                Double.parseDouble(settings.getProperty("sea.color.blue", "255"))/255, 1D);
        landColor = new Color(
                Double.parseDouble(settings.getProperty("land.color.red", "150"))/255,
                Double.parseDouble(settings.getProperty("land.color.green", "150"))/255,
                Double.parseDouble(settings.getProperty("land.color.blue", "150"))/255, 1D);
        final PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final Color color = reader.getColor(x, y);
                writer.setColor(x, y, seas.contains(color) ? seaColor : landColor);
            }
        }
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
                                        new Color(
                                            Double.parseDouble(m.group(1))/255,
                                            Double.parseDouble(m.group(2))/255,
                                            Double.parseDouble(m.group(3))/255, 1D));
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
        loadSeas();
        loadMap();
        loadCountries();
    }

    private void loadMap() {
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
        final PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < height; ++y){
            for (int x = 0; x < width; ++x){
                final Color color = reader.getColor(x, y);
                provinces.get(colors.get(color)).points.add(new Point(x, y));
                writer.setColor(x, y, color);
            }
        }

        for(ProvinceInfo info : provinces.values()) {
            info.calculateCenter();
        }

        imageView.setImage(output);
        int fitWidth = Integer.parseInt(settings.getProperty("map.fit.width", String.valueOf(width)));
        int fitHeight = Integer.parseInt(settings.getProperty("map.fit.height", String.valueOf(height)));
        imageView.setFitHeight(fitHeight);
        imageView.setFitWidth(fitWidth);
        scrollPane.setContent(null);
        scrollPane.setContent(imageView);
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
                final Color color = new Color(
                        Double.parseDouble(parts[1])/255,
                        Double.parseDouble(parts[2])/255,
                        Double.parseDouble(parts[3])/255, 1D);
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
        final DefaultMapParser parser = new DefaultMapParser(new Pair<>(seas, provinces));
        try (final InputStream is = new FileInputStream(eu4Directory.getPath() + "/map/default.map")) {
            parser.parse(is);
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void processEvent(final Date date, final Event event) {
        System.out.println(String.format("[%1$s]: %2$s", date, event));
        log.getEngine().loadContent(logContent.append(String.format("[%1$s]: %2$#s<br>", date, event)).toString());

        if (event instanceof Controller) {
            final com.paradoxplaza.eu4.replayer.events.Controller controller = (com.paradoxplaza.eu4.replayer.events.Controller) event;
            for(Point p : provinces.get(controller.id).points) {
                if ( p.y % 2 == 0) {
                    Color color = countries.get(controller.tag);
                    writer.setColor(p.x, p.y, color == null ? landColor : color);
                }
            }
        } else if (event instanceof Owner) {
            final com.paradoxplaza.eu4.replayer.events.Owner owner = (com.paradoxplaza.eu4.replayer.events.Owner) event;
            for(Point p : provinces.get(owner.id).points) {
                //if ( p.y % 2 == 1) {
                    Color color = countries.get(owner.tag);
                    writer.setColor(p.x, p.y, color == null ? landColor : color);
                //}
            }
        }
    }

    public class JavascriptBridge {

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

        public boolean prov(final String prov) {
            final Point center = provinces.get(prov).center;
            scrollPane.setHvalue(mapCoordToScrollProcent(
                    center.x, map.getWidth(), scrollPane.getWidth(), imageView.getFitWidth()));
            scrollPane.setVvalue(mapCoordToScrollProcent(
                    center.y, map.getHeight(), scrollPane.getHeight(), imageView.getFitHeight()));
            return false;
        }
    }
}
