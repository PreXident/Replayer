package com.paradoxplaza.eu4.replayer;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller for the Replayer application.
 */
public class ReplayerController implements Initializable {

    @FXML
    ImageView image;

    /** Replayer settings. */
    Properties settings;

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
        image.setImage(new Image("file:///" + settings.getProperty("eu4.dir") + "map/provinces.bmp"));
    }
}
