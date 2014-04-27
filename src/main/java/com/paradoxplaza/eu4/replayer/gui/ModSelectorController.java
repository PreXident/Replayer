package com.paradoxplaza.eu4.replayer.gui;

import com.paradoxplaza.eu4.replayer.EmptyTaskBridge;
import com.paradoxplaza.eu4.replayer.ModInfo;
import com.paradoxplaza.eu4.replayer.Replay;
import com.paradoxplaza.eu4.replayer.parser.mod.ModParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 */
public class ModSelectorController implements Initializable {

    /** Original title. */
    static final String TITLE = "Mod Selector";

    /**
     * Indicator how should ModSelector respond to stop.
     */
    public enum Result {
        /** Exit without saving. */
        EXIT,
        /** Save the properties. */
        EXIT_SAVE,
        /** Save the properties and run the replayer. */
        SAVE_RUN
    }

    @FXML
    private BorderPane root;

    @FXML
    private ListView<CheckModInfo> listView;

    /** Property binded to stage titleProperty. */
    StringProperty titleProperty = new SimpleStringProperty(TITLE);

    /** Result of the selection. */
    private Result result = Result.EXIT;

    /** Application settings. */
    private Properties settings;

    /** List of mods with information about checking. */
    private List<CheckModInfo> mods = new ArrayList<>();

    /** Parent ModSelector. */
    private ModSelector modSelector;

    @FXML
    private void cancel() {
        result = Result.EXIT;
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void exit() {
        result = Result.EXIT_SAVE;
        saveModsToSettings();
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void run() {
        result = Result.SAVE_RUN;
        saveModsToSettings();
        modSelector.stop();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //
        listView.setCellFactory(CheckBoxListCell.forListView(new Callback<CheckModInfo, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(final CheckModInfo m) {
                return m.checked;
            }
        }, new StringConverter<CheckModInfo>() {
            @Override
            public String toString(CheckModInfo m) {
                return m.name;
            }
            @Override
            public CheckModInfo fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }));
    }

    /**
     * Sets the ModSelector.
     * @param modSelector new ModSelector
     */
    void setModSelector(ModSelector modSelector) {
        this.modSelector = modSelector;
    }

    /**
     * Sets application settings.
     * @param settings new application settings
     */
    public void setSettings(Properties settings) {
        this.settings = settings;
        final Set<String> active = new HashSet<>();
        active.addAll(Arrays.asList(settings.getProperty("mod.list", "").split(";")));
        final List<ModInfo> rawMods = new ArrayList<>();
        String modDirPath = settings.getProperty("mod.basedir", Replay.DEFAULT_BASE_DIR) + "/mod";
        for (File file : new File(modDirPath).listFiles(new ModFileFilter())) {
            try (final InputStream is = new FileInputStream(file)) {
                final ModParser parser =
                        new ModParser(rawMods, Long.MAX_VALUE, is, new EmptyTaskBridge<List<ModInfo>>());
                parser.run();
                ModInfo mod = rawMods.get(0);
                final String modDesc = "mod/" + file.getName();
                final String modName = mod.name != null ? mod.name : file.getName();
                CheckModInfo checkModInfo = new CheckModInfo(modDesc, modName);
                checkModInfo.checked.set(active.contains(modDesc));
                mods.add(checkModInfo);
                rawMods.clear();
            } catch(Exception e) { e.printStackTrace(); }
        }
        Collections.sort(mods, new Comparator<CheckModInfo>() {
            @Override
            public int compare(CheckModInfo m1, CheckModInfo m2) {
                return m1.name.compareToIgnoreCase(m2.name);
            }
        });
        listView.getItems().addAll(mods);
    }

    /**
     * Returns selection result.
     * @return selection restult
     */
    public Result getResult() {
        return result;
    }

    /**
     * Iterates through mods and sets mod.list property in settings.
     */
    public void saveModsToSettings() {
        StringBuilder modlist = new StringBuilder();
        for (CheckModInfo mod : mods) {
            if (mod.checked.get()) {
                modlist.append(mod.modDesc);
                modlist.append(';');
            }
        }
        settings.setProperty("mod.list", modlist.toString());
    }

    /**
     * Returns title property.
     * @return title property
     */
    public StringProperty titleProperty() {
        return titleProperty;
    }

    /**
     * Simple container of mod information.
     */
    static class CheckModInfo {

        /** Path to mod descriptor. */
        final String modDesc;

        /** Mod name. */
        final String name;

        /** Indicator whether the mod is selected. */
        final BooleanProperty checked = new SimpleBooleanProperty(false);

        /**
         * Only constructor.
         * @param modDesc mod descriptor
         * @param name mod name
         */
        public CheckModInfo(final String modDesc, final String name) {
            this.name = name;
            this.modDesc = modDesc;
        }
    }

    /**
     * Simple filter for mod descriptors (.mod) files.
     */
    static class ModFileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".mod");
        }

    }
}
