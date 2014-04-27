package com.paradoxplaza.eu4.replayer.gui;

import com.paradoxplaza.eu4.replayer.Utils;
import com.paradoxplaza.eu4.replayer.localization.Localizator;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.UnclosableInputStream;
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
 * Simple application to select mods without editing the property file.
 */
public class ModSelector extends Application {

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
    ModSelectorController controller;

    /** Application main stage. */
    Stage stage;

    /**
     * Launched replayer.
     * We need to stop it, as ModSelector is registered to stop event.
     */
    Replayer replayer;

    /** Application arguments. We must pass it manually to launched replayer. */
    List<String>  args;

    @Override
    public void start(final Stage stage) throws Exception {
        this.stage = stage;
        args = getParameters().getRaw();
        System.out.printf(l10n("app.starting"));
        //load default properties
        settings = new Properties(Utils.loadDefaultJarProperties());
        Utils.resetDefaultLocale(settings.getProperty("locale.language"));
        //parse arguments from command line, ie. load property file
        final List<String> args = getParameters().getRaw();
        if (helpNeeded(args)) {
            System.out.printf(l10n("app.usage"), Replayer.DEFAULT_PROPERTIES);
            System.exit(0);
        }
        if (args.size() == 1) {
            propertyFile = args.get(0);
            System.out.printf(l10n("app.properties.loading"), propertyFile);
            try (final InputStream is =
                    propertyFile.equals("-") ? new UnclosableInputStream(System.in)
                        : new FileInputStream(propertyFile)) {
            	 settings.load(is);
            } catch(Exception e) {
                System.err.printf(l10n("app.properties.error"), propertyFile);
                e.printStackTrace();
            }
        } else /*if (args.length == 0)*/ { //no property file provided
            propertyFile = Replayer.DEFAULT_PROPERTIES;
            System.out.printf(l10n("app.properties.default"), propertyFile);
            try (final InputStream is = new FileInputStream(Replayer.DEFAULT_PROPERTIES)) {
                settings.load(is);
            } catch(Exception e) {
                System.err.printf(l10n("app.properties.error"), propertyFile);
                e.printStackTrace();
            }
        }
        Utils.resetDefaultLocale(settings.getProperty("locale.language"));

        //create javafx controls
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("ModSelector.fxml"), Localizator.getInstance().getResourceBundle());
        final Parent root = (Parent) loader.load();
        controller = loader.getController();
        controller.setSettings(settings);
        controller.setModSelector(this);
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

    @Override
    public void stop() {
        if (replayer == null) { //we have not launched replayer yet
            System.out.printf(l10n("app.closing"));
            ModSelectorController.Result result = controller.getResult();
            if (result == ModSelectorController.Result.EXIT) {
                return;
            }
            if (!propertyFile.equals("-")) {
                System.out.printf(l10n("app.properties.store"), propertyFile);
                try (final OutputStream os = new FileOutputStream(propertyFile)) {
                    settings.store(os, null);
                } catch(IOException e) {
                    System.err.printf(l10n("app.properties.store.error"), propertyFile);
                    e.printStackTrace();
                }
            }
            if (result == ModSelectorController.Result.SAVE_RUN) {
                try {
                    replayer = new Replayer(args);
                    replayer.start(stage);
                } catch (Exception e) { }
            }
        } else { //inform replayer about stopping
            replayer.stop();
        }
    }
}
