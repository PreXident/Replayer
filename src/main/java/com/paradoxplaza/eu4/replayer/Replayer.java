package com.paradoxplaza.eu4.replayer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Replayer application.
 */
public class Replayer extends Application {

    /** Path to default property file. The file should be inside the jar. */
    private static final String DEFAULT_JAR_PROPERTIES = "replayer.defprops";

    /** Default path to property file. */
    private static final String DEFAULT_PROPERTIES = "replayer.properties";

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /** Settings of the Replayer. */
    Properties settings;

    /** File containing properties. Used to save properties. */
    String propertyFile;

    /** Application controller. */
    ReplayerController controller;

    @Override
    public void start(final Stage stage) throws Exception {
        //load default properties
        settings = new Properties(loadDefaulJarProperties());

        //parse arguments from command line, ie. load property file
        final List<String> args = getParameters().getRaw();
        if (helpNeeded(args)) {
            //TODO
            System.out.printf("USAGE: java -jar replayer.jar [property_file]\n");
            System.out.printf("If property_file argument is not provided, default property file \"%s\" will be used\n", DEFAULT_PROPERTIES);
            System.exit(0);
        }
        System.out.printf("Starting...\n");
        if (args.size() == 1) {
            propertyFile = args.get(0);
            try (final InputStream is = new FileInputStream(propertyFile)) {
                settings.load(is);
            } catch(Exception e) {
                System.err.printf("Error with specified property file\n");
                e.printStackTrace();
            }
        } else /*if (args.length == 0)*/ { //no property file provided
            propertyFile = DEFAULT_PROPERTIES;
            try (final InputStream is = new FileInputStream(DEFAULT_PROPERTIES)) {
                System.out.printf("No property file specified, using default path.\n");
                settings.load(is);
            } catch(Exception e) {
                System.err.printf(String.format("Error with default property file \"%s\"\n", DEFAULT_PROPERTIES));
                e.printStackTrace();
            }
        }

        //create javafx controls
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("Replayer.fxml"));
        final Parent root = (Parent) loader.load();
        controller = loader.getController();
        controller.setSettings(settings);
        final Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.titleProperty().bind(controller.titleProperty());
        stage.getIcons().add(new Image(getClass().getResourceAsStream("eu4.png")));
        stage.show();
        stage.toFront();
    }

    /**
     * Checks whether arguments suggest printing usage.
     * @param args parameters from command line
     * @return true if help needed, false otherwise
     */
    private boolean helpNeeded(final List<String> args) {
        if (args.size() > 1) {
            return true;
        }
        if (args.size() == 1) {
            final String param = args.get(0);
            return param.equals("--help") || param.equals("-h") || param.equals("/?");
        }
        return false;
    }

    /**
     * Loads and returns default properties from {@link #DEFAULT_JAR_PROPERTIES}.
     * @return default properties
     */
    private Properties loadDefaulJarProperties() {
        final Properties res = new Properties();
        try {
            res.load(getClass().getClassLoader().getResourceAsStream(DEFAULT_JAR_PROPERTIES));
        } catch(Exception e) {
            //someone messed with our properties!
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void stop() {
        System.out.println("Closing...\n");
        final StringBuilder s = new StringBuilder();
        for(Class c : controller.notableEvents) {
            s.append(";");
            s.append(c.getSimpleName());
        }
        settings.setProperty("events", s.substring(1));
        try (final OutputStream os = new FileOutputStream(propertyFile)) {
            settings.store(os, null);
        } catch(IOException e) {
            System.err.printf("Error while storing settings to \"%1$s\"", propertyFile);
            e.printStackTrace();
        }
    }
}
