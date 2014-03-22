package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.localization.Localizator;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.UnclosableStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
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
        System.out.printf(l10n("app.starting"));
        //load default properties
        settings = new Properties(loadDefaultJarProperties());
        resetDefaultLocale(settings.getProperty("locale.language"));

        //parse arguments from command line, ie. load property file
        final List<String> args = getParameters().getRaw();
        if (helpNeeded(args)) {
            System.out.printf(l10n("app.usage"), DEFAULT_PROPERTIES);
            System.exit(0);
        }
        if (args.size() == 1) {
            propertyFile = args.get(0);
            System.out.printf(l10n("app.properties.loading"), propertyFile);
            try (final InputStream is =
                    propertyFile.equals("-") ? new UnclosableStream(System.in)
                        : new FileInputStream(propertyFile)) {
            	 settings.load(is);
            } catch(Exception e) {
                System.err.printf(l10n("app.properties.error"), propertyFile);
                e.printStackTrace();
            }
        } else /*if (args.length == 0)*/ { //no property file provided
            propertyFile = DEFAULT_PROPERTIES;
            System.out.printf(l10n("app.properties.default"), propertyFile);
            try (final InputStream is = new FileInputStream(DEFAULT_PROPERTIES)) {
                settings.load(is);
            } catch(Exception e) {
                System.err.printf(l10n("app.properties.error"), propertyFile);
                e.printStackTrace();
            }
        }
        resetDefaultLocale(settings.getProperty("locale.language"));

        //ask for eu4 directory if property is not valid
        final String eu4dir = settings.getProperty("eu4.dir");
        if (eu4dir == null || !new File(eu4dir).exists()) {
            File dir = null;
            if (System.getenv("EU4_HOME") != null) {
                dir = new File(System.getenv("EU4_HOME"));
            }
            if (dir == null || !dir.exists()) {
                final DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle(l10n("app.eu4dir.select"));
                dir = directoryChooser.showDialog(null);
            }
            if (dir == null || !dir.exists()) {
                System.out.printf(l10n("app.eu4dir.error"));
                System.exit(-1);
            } else {
                settings.put("eu4.dir", dir.getPath());
            }
        }

        //create javafx controls
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("Replayer.fxml"), Localizator.getInstance().getResourceBundle());
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
    private Properties loadDefaultJarProperties() {
        System.out.printf(l10n("app.properties.jar"));
        final Properties res = new Properties();
        try {
            res.load(getClass().getClassLoader().getResourceAsStream(DEFAULT_JAR_PROPERTIES));
        } catch(Exception e) {
            //someone messed with our properties!
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Resets default locale and {@link Localizer} if langCode is not null.
     * @param langCode new default locale language code
     */
    private void resetDefaultLocale(final String langCode) {
        if (langCode != null) {
            Locale.setDefault(new Locale(langCode));
            Localizator.getInstance().reloadResourceBundle();
        }
    }

    @Override
    public void stop() {
        System.out.printf(l10n("app.closing"));
        controller.stop();
        if (!propertyFile.equals("-")) {
            System.out.printf(l10n("app.properties.store"), propertyFile);
            try (final OutputStream os = new FileOutputStream(propertyFile)) {
                settings.store(os, null);
            } catch(IOException e) {
                System.err.printf(l10n("app.properties.store.error"), propertyFile);
                e.printStackTrace();
            }
        }
    }
}
