package com.paradoxplaza.eu4.replayer;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Controller for the Replayer application.
 */
public class ReplayerController implements Initializable {

    @FXML
    ImageView image;

    /** Replayer settings. */
    Properties settings;

    Image provinces;
    PixelReader reader;
    WritableImage output;
    PixelWriter writer;

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        //
    }

    /**
     * Sets Replayer settins.
     * @param settings new settings
     */
    public void setSettings(final Properties settings) {
        this.settings = settings;
        provinces = new Image("file:///" + settings.getProperty("eu4.dir") + "map/provinces.bmp");
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

        image.setImage(output);
    }
}
