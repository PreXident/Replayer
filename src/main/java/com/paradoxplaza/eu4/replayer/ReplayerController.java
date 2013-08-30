package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
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
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Controller for the Replayer application.
 */
public class ReplayerController implements Initializable {

    /** Original title. */
    final static String TITLE = "Replayer";

    /** Title format when replaying save game. */
    final static String TITLE_SAVEGAME = String.format("%1$s - %2$s", TITLE, "%1$s");

    @FXML
    ImageView imageView;

    @FXML
    ScrollPane scrollPane;

    /** Replayer settings. */
    Properties settings;

    /** Original provinces picture. */
    Image provinces;

    /** Reader of {@link #provinces}. */
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
            loadProvinces();
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
        final TextParser parser = new TextParser();
        try (final InputStream is = new FileInputStream(file)) {
            parser.parse(is);
            final SaveGame saveGame = parser.getSaveGame();
            for(Event event : saveGame.timeline.get(null)) {
                System.out.println(String.format("[%1$s]: %2$s", null, event));
            }
            for(Date date : new DateGenerator(saveGame.startDate, saveGame.date)) {
                List<Event> list = saveGame.timeline.get(date);
                if (list == null) {
                    continue;
                }
                for(Event event : saveGame.timeline.get(date)) {
                    System.out.println(String.format("[%1$s]: %2$s", date, event));
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void resetZoom() {
        imageView.setFitHeight(provinces.getHeight());
        imageView.setFitWidth(provinces.getWidth());
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
        return imageView.getScene().getWindow();
    }

    private void loadCountries() {
        
    }

    private void loadData() {
        titleProperty.set(TITLE);
        loadProvinces();
        loadCountries();
    }

    private void loadProvinces() {
        try {
            provinces = new Image(new FileInputStream(eu4Directory.toString() + "/map/provinces.bmp"));
        } catch (FileNotFoundException e) {
            System.err.println("File provinces.bmp not found!");
            provinces = new WritableImage(1,1);
        }
        reader = provinces.getPixelReader();

        final int width = (int) provinces.getWidth();
        final int height = (int) provinces.getHeight();

        //Copy from source to destination pixel by pixel
        output = new WritableImage(width, height);
        writer = output.getPixelWriter();

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                Color color = reader.getColor(x, y);
                writer.setColor(x, y, color);
            }
        }

        imageView.setImage(output);
        int fitWidth = Integer.parseInt(settings.getProperty("map.fit.width", String.valueOf(width)));
        int fitHeight = Integer.parseInt(settings.getProperty("map.fit.height", String.valueOf(height)));
        imageView.setFitHeight(fitHeight);
        imageView.setFitWidth(fitWidth);
    }
}
