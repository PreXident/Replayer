package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.defaultmap.DefaultMapParser;
import com.paradoxplaza.eu4.replayer.events.Controller;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import com.paradoxplaza.eu4.replayer.parser.savegame.SaveGameParser;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

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

    /** Replayer settings. */
    Properties settings;

    /** Original map picture. */
    Image map;

    /** Reader for {@link #map}. */
    PixelReader reader;

    /** Image displayed in {@link #imageView}. */
    WritableImage output;

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
    Map<String, Color> provinces = new HashMap<>();

    /** Set of colors assigned to sea provinces. */
    Set<Color> seas = new HashSet<>();

    /** Loaded save game to be replayed. */
    SaveGame saveGame;

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
        if (true) return;
        saveDirectory = file.getParentFile();
        settings.setProperty("save.dir", saveDirectory.getPath());
        titleProperty.setValue(String.format(TITLE_SAVEGAME, file.getName()));

        final PixelWriter writer = output.getPixelWriter();
        saveGame = new SaveGame();
        final SaveGameParser parser = new SaveGameParser(saveGame);
        try (final InputStream is = new FileInputStream(file)) {
            parser.parse(is);
            for(Event event : saveGame.timeline.get(null)) {
                System.out.println(String.format("[%1$s]: %2$s", null, event));
                final int width = (int) map.getWidth();
                final int height = (int) map.getHeight();

                for (int y = 0; y < height; y++){
                    for (int x = 0; x < width; x++){
                        final Color color = reader.getColor(x, y);
                        if (event instanceof Controller) {
                            final com.paradoxplaza.eu4.replayer.events.Controller controller = (com.paradoxplaza.eu4.replayer.events.Controller) event;
                            final Color provColor = provinces.get(controller.id);
                            if (color.equals(provColor) && y % 2 == 0) {
                                writer.setColor(x, y, countries.get(controller.tag));
                            }
                        } else if (event instanceof Owner) {
                            final com.paradoxplaza.eu4.replayer.events.Owner owner = (com.paradoxplaza.eu4.replayer.events.Owner) event;
                            final Color provColor = provinces.get(owner.id);
                            if (color.equals(provColor) && y % 2 == 1) {
                                writer.setColor(x, y, countries.get(owner.tag));
                            }
                        }
                    }
                }
            }
//            for(Date date : new DateGenerator(saveGame.startDate, saveGame.date)) {
//                List<Event> list = saveGame.timeline.get(date);
//                if (list == null) {
//                    continue;
//                }
//                for(Event event : saveGame.timeline.get(date)) {
//                    System.out.println(String.format("[%1$s]: %2$s", date, event));
//                }
//            }
        } catch(Exception e) { e.printStackTrace(); }
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
        //
    }

    /**
     * Sets Replayer settins. Adjusts saveDirectory etc. and loads map.
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
        final Color seaColor = new Color(
                Double.parseDouble(settings.getProperty("sea.color.red", "0"))/255,
                Double.parseDouble(settings.getProperty("sea.color.green", "0"))/255,
                Double.parseDouble(settings.getProperty("sea.color.blue", "255"))/255, 1D);
        final Color landColor = new Color(
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
                writer.setColor(x, y, color);
            }
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
                provinces.put(parts[0], new Color(
                        Double.parseDouble(parts[1])/255,
                        Double.parseDouble(parts[2])/255,
                        Double.parseDouble(parts[3])/255, 1D));
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
}
