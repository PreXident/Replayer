package com.paradoxplaza.eu4.replayer.gui;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.DateGenerator.IDateListener;
import com.paradoxplaza.eu4.replayer.EventProcessor.IEventListener;
import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import com.paradoxplaza.eu4.replayer.Replay;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.generator.ModGenerator;
import com.paradoxplaza.eu4.replayer.gif.Giffer;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.IgnoreCaseFileNameComparator;
import java.awt.Point;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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

    /** ID used to append new messages to log. */
    static final String LOG_ID = "content";

    /** Log content should always start with this. */
    static final String LOG_HEADER = String.format("<span id='%s'>", LOG_ID);

    /** This is appended to log content before updating the log. */
    static final String LOG_FOOTER = "</span>";

    /** Format for initial content of log. */
    static final String LOG_INIT_FORMAT = String.format("<html><body><div id='%1$s'>%%s</div></body></html>", LOG_ID);

    /** Initial content of log. */
    static final String LOG_INIT = String.format(LOG_INIT_FORMAT, "");

    /** JavaScript call to scroll to the bottom of the page. */
    static final String SCROLL_DOWN = "window.scrollTo(0,document.body.scrollHeight)";

    /** Key of property disabling the log. */
    static final String LOG_DISABLE_KEY = "log.disable";
    
    /**
     * Translates {@link #map} coordinate to {@link #scrollPane} procentual HValue/VValue.
     * @param mapCoord map coordinate
     * @param mapSize size of map
     * @param scrollSize size of scrollPane
     * @param imageViewSize size of imageView
     * @return scrollPane procentual scroll position
     */
    static double mapCoordToScrollProcent(final int mapCoord, final double mapSize, final double scrollSize, final double imageViewSize) {
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
     * Translates procentual HValue/VValue to {@link #map} coordinate.
     * @param procent scrollPane procentual scroll position
     * @param mapSize size of map
     * @param scrollSize size of scrollPane
     * @param imageViewSize size of imageView
     * @return map coordinate
     */
    static int scrollProcentToMapCoord(final double procent, final double mapSize, final double scrollSize, final double imageViewSize) {
        final double zero = scrollSize / 2; //coord of center when (H/V)Value == 0
        final double unit = imageViewSize - 2 * zero; //size of scrollable area
        final double scroll = procent * unit + zero;
        return (int) (scroll / imageViewSize * mapSize);
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
    TextField dateEdit;

    @FXML
    VBox bottom;

    @FXML
    ProgressBar progressBar;

    @FXML
    Menu eventMenu;

    @FXML
    ComboBox<String> daysCombo;

    @FXML
    TextField focusEdit;

    @FXML
    Label statusLabel;

    @FXML
    Button jumpButton;

    @FXML
    HBox provinceContainer;

    @FXML
    WebView provinceLog;

    @FXML
    ComboBox<String> periodCombo;

    @FXML
    ComboBox<String> langCombo;

    @FXML
    Menu gifMenu;

    @FXML
    CheckMenuItem bordersCheckMenuItem;

    @FXML
    CheckMenuItem subjectsCheckMenuItem;

    @FXML
    CheckMenuItem gifSwitchCheckMenuItem;

    @FXML
    CheckMenuItem gifLoopCheckMenuItem;

    @FXML
    TextField gifWidthEdit;

    @FXML
    TextField gifHeightEdit;

    @FXML
    TextField gifBreakEdit;

    @FXML
    TextField gifStepEdit;

    @FXML
    CheckMenuItem gifDateCheckMenuItem;

    @FXML
    CheckMenuItem gifSubimageCheckMenuItem;

    @FXML
    MyColorPicker gifDateColorPicker;

    @FXML
    TextField gifDateSizeEdit;

    @FXML
    TextField gifDateXEdit;

    @FXML
    TextField gifDateYEdit;

    @FXML
    TextField gifSubimageXEdit;

    @FXML
    TextField gifSubimageYEdit;

    @FXML
    TextField gifSubimageWidthEdit;

    @FXML
    TextField gifSubimageHeightEdit;

    /** Lock to prevent user input while background processing. */
    final Semaphore lock = new Semaphore(1);

    /** Replayer settings. */
    public Properties settings;

    /** Image displayed in {@link #imageView}. */
    public WritableImage output;

    /** Buffer for emergency refresh. */
    public int[] buffer;

    /** How many pixels are added to width and height when zooming in/out. */
    int zoomStep;
    
    /** Flag indicating whether the event log should be disabled. */
    boolean disableLog = false;

    /** Directory containing save games. */
    File saveDirectory;

    /** Directory containing serialized games. */
    File serDirectory;

    /** Path to save game file. */
    String saveFileName;

    /** Directory containing needed game files. */
    public File eu4Directory;

    /** Property binded to stage titleProperty. */
    StringProperty titleProperty = new SimpleStringProperty(TITLE);

    /** Number of periods processed in one tick. */
    int deltaPerTick;

    /** Period per tick. */
    Date.Period period;

    /** Timer for replaying. */
    Timeline timeline;

    /** Listens to date changes and initiates event processing. */
    final DateListener dateListener = new DateListener();

    /** Content of log area with html code. */
    public final StringBuilder logContent = new StringBuilder();

    /** Writer of gif output. */
    Giffer giffer = null;

    /** Selected province. */
    ProvinceInfo selectedProvince;

    /** Content of selected province log. */
    String selectedProvinceLogContent;

    /** The last file of save game batch. */
    File file;

    /** Replay object itself. */
    Replay replay;

    /** Jumper that fast forwards/rewinds the save. */
    Jumper finalizer;

    /** IEventListener that updates log, appends to logContent and updates buffer. */
    final IEventListener standardListener = new EventListener(this);

     /** IEventListener that does not update log nor {@link #output}, only appends to logContent. */
    final IEventListener logAppendOnlyListener = new EventListener(this) {
        @Override
        public void updateLog() { }

        @Override
        public void setColor(int[] buffer, final int pos, final int color) { }
    };

    @FXML
    private void backPlay() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (replay.saveGame == null) {
            lock.release();
            return;
        }
        if (timeline != null) {
            timeline.stop();
            timeline = null;
            lock.release();
            return;
        }

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(0.1),
                  new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        replay.skip(period, -deltaPerTick);
                        if (replay.isAtStart()) {
                            timeline.stop();
                            timeline = null;
                        }
                    }
                }));
        timeline.playFromStart();
        lock.release();
    }

    @FXML
    private void borders() {
        settings.setProperty("borders", bordersCheckMenuItem.isSelected() ? "true" : "false");
    }

    @FXML
    private void changeEU4Directory() throws InterruptedException{
        if (!lock.tryAcquire()) {
           return;
        }
        pause();
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(l10n("app.eu4dir.select"));
        if (eu4Directory != null && eu4Directory.exists() && eu4Directory.isDirectory()) {
            directoryChooser.setInitialDirectory(eu4Directory);
        }
        final File dir = directoryChooser.showDialog(getWindow());
        if (dir != null) {
            eu4Directory = dir;
            settings.setProperty("eu4.dir", eu4Directory.getPath());
            loadData();
        }
        lock.release();
    }

    @FXML
    private void close() {
        Platform.exit();
    }

    @FXML
    private void culturalMapMode() {
        buffer = replay.culturalBuffer;
        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
    }

    @FXML
    private void deserialize() {
        if (!lock.tryAcquire()) {
           return;
        }
        pause();
        endGif();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(serDirectory);
        fileChooser.setTitle(l10n("replay.eu4ser.select"));

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(l10n("replay.eu4ser.ext"), "*.ser");
        fileChooser.getExtensionFilters().add(extFilter);
        //Show open file dialog
        final File file = fileChooser.showOpenDialog(getWindow());
        if (file == null) {
            lock.release();
            return;
        }
        serDirectory = file.getParentFile();
        settings.setProperty("ser.dir", serDirectory.getPath());
        titleProperty.setValue(String.format(TITLE_SAVEGAME, file.getName()));

        final Task<SaveGame> deser = new Task<SaveGame>() {
            @Override
            protected SaveGame call() throws Exception {
                replay.deserializeSave(this, logAppendOnlyListener, file.getAbsolutePath());
                return replay.saveGame;
            }
        };

        deser.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                if ("true".equals(settings.getProperty("gif"))) {
                    giffer = new Giffer(settings, replay.bufferWidth, replay.bufferHeight, file.getAbsolutePath());
                    giffer.updateGif(buffer, replay.saveGame.startDate);
                }
                output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
                log.getEngine().loadContent(String.format(LOG_INIT_FORMAT, logContent.toString()));
                logContent.setLength(LOG_HEADER.length());
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().set(replay.dateGenerator.getProgress());
                replay.setDateListener(dateListener);
                replay.setEventListener(standardListener);
                dateEdit.setText(replay.getDate().toString());
                imageView.setImage(output);
                new JavascriptBridge().prov(settings.getProperty("center.id", "1"));
                statusLabel.textProperty().unbind();
                statusLabel.setText(l10n("replay.save.loaded"));
                lock.release();
            }
        });

        deser.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(final WorkerStateEvent t) {
                final Throwable e = t.getSource().getException();
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });

        statusLabel.textProperty().bind(deser.titleProperty());
        progressBar.progressProperty().bind(deser.progressProperty());
        new Thread(deser, "SaveGameDeserializer").start();
    }

    @FXML
    private void finish() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (replay.saveGame == null || !replay.dateGenerator.hasNext()) {
            lock.release();
            return;
        }
        pause();
        //finalizer = new Jumper(replay.saveGame.date, logAppendOnlyListener);
        finalizer = new Jumper(this, replay.saveGame.date, logAppendOnlyListener);
        statusLabel.textProperty().bind(finalizer.titleProperty());
        progressBar.progressProperty().bind(finalizer.progressProperty());
        new Thread(finalizer, "Game finalizer").start();
    }

    @FXML
    private void generateMod() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (replay.rnw) {
            statusLabel.setText(l10n("generator.rnw"));
        } else {
            new ModGenerator(settings).generate(replay.provinces.values());
            statusLabel.setText(l10n("generator.done"));
        }
        lock.release();
    }

    @FXML
    private void gifDate() {
        final boolean drawDate = gifDateCheckMenuItem.isSelected();
        if (!lock.tryAcquire()) {
            gifDateCheckMenuItem.setSelected(drawDate);
            return;
        }
        settings.setProperty("gif.date", drawDate ? "true" : "false");
        if (giffer != null) {
            giffer.setGifDateDraw(drawDate);
        }
        lock.release();
    }

    @FXML
    private void gifLoop() {
        settings.setProperty("gif.loop", gifLoopCheckMenuItem.isSelected() ? "true" : "false");
    }

    @FXML
    private void gifSubimage() {
        final boolean subimage = gifSubimageCheckMenuItem.isSelected();
        if (!lock.tryAcquire()) {
            gifSubimageCheckMenuItem.setSelected(subimage);
            return;
        }
        settings.setProperty("gif.subimage", subimage ? "true" : "false");
        if (giffer != null) {
            giffer.setGifSubImage(subimage);
        }
        lock.release();
    }

    @FXML
    private void gifSwitch() {
        if (!lock.tryAcquire()) {
            //undo
            gifSwitchCheckMenuItem.setSelected(!gifSwitchCheckMenuItem.isSelected());
            return;
        }
        if (gifSwitchCheckMenuItem.isSelected()) {
            if (giffer == null && file != null) {
                final int width = replay.bufferWidth;
                final int height = replay.bufferHeight;
                giffer = new Giffer(settings, width, height, file.getAbsolutePath());
                giffer.updateGif(buffer, replay.getDate());
            } else {
                gifSwitchCheckMenuItem.setSelected(false);
            }
        } else {
            endGif();
        }
        lock.release();
    }

    @FXML
    private void jump() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (replay.saveGame == null) {
            lock.release();
            return;
        }
        pause();
        try {
            final Date target = new Date(dateEdit.getText());
            if (target.compareTo(replay.dateGenerator.getMin()) < 0 || target.compareTo(replay.dateGenerator.getMax()) > 0) {
                statusLabel.setText(l10n("replay.jump.outside"));
                lock.release();
                return;
            }
            pause();
            final Date date = replay.getDate();
            if (date.equals(target)) {
                lock.release();
                return;
            }
            finalizer = new Jumper(this, target, logAppendOnlyListener);
            statusLabel.textProperty().bind(finalizer.titleProperty());
            progressBar.progressProperty().bind(finalizer.progressProperty());
            new Thread(finalizer, "Jumper").start();
        } catch (Exception e) {
            statusLabel.setText(l10n("replay.jump.parse.error"));
            lock.release();
        }
    }

    @FXML
    private void load() throws InterruptedException {
        if (!lock.tryAcquire()) {
           return;
        }
        pause();
        endGif();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(saveDirectory);
        fileChooser.setTitle(l10n("replay.eu4save.select"));

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(l10n("replay.eu4save.ext"), "*.eu4");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        final List<File> filesUnmod = fileChooser.showOpenMultipleDialog(getWindow());
        if (filesUnmod == null || filesUnmod.isEmpty()) {
            lock.release();
            return;
        }
        final List<File> files = new ArrayList<>(filesUnmod);
        Collections.sort(files, new IgnoreCaseFileNameComparator());

        file = files.get(files.size() - 1);
        saveDirectory = file.getParentFile();
        settings.setProperty("save.dir", saveDirectory.getPath());
        titleProperty.setValue(String.format(TITLE_SAVEGAME, file.getName()));

        final Task<SaveGame> loader = new Task<SaveGame>() {
            @Override
            protected SaveGame call() throws Exception {
                replay.loadSaves(this, logAppendOnlyListener, files);
                return replay.saveGame;
            }
        };

        loader.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                if ("true".equals(settings.getProperty("gif"))) {
                    giffer = new Giffer(settings, replay.bufferWidth, replay.bufferHeight, file.getAbsolutePath());
                    giffer.updateGif(buffer, replay.saveGame.startDate);
                }
                output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
                log.getEngine().loadContent(String.format(LOG_INIT_FORMAT, logContent.toString()));
                logContent.setLength(LOG_HEADER.length());
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().set(replay.dateGenerator.getProgress());
                replay.setDateListener(dateListener);
                replay.setEventListener(standardListener);
                dateEdit.setText(replay.getDate().toString());
                imageView.setImage(output);
                new JavascriptBridge().prov(settings.getProperty("center.id", "1"));
                statusLabel.textProperty().unbind();
                statusLabel.setText(l10n("replay.save.loaded"));
                lock.release();
            }
        });

        loader.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(final WorkerStateEvent t) {
                final Throwable e = t.getSource().getException();
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });

        statusLabel.textProperty().bind(loader.titleProperty());
        progressBar.progressProperty().bind(loader.progressProperty());
        new Thread(loader, "SaveGameLoader").start();
    }

    @FXML
    private void pause() {
        if (timeline != null) {
            timeline.pause();
        }
        if (finalizer != null) {
            finalizer.cancel();
            finalizer = null;
        }
    }

    @FXML
    private void play() throws InterruptedException {
        if (!lock.tryAcquire()) {
           return;
        }
        if (replay.saveGame == null) {
            lock.release();
            return;
        }
        if (timeline != null) {
            timeline.stop();
            timeline = null;
            lock.release();
            return;
        }

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(0.1),
                  new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        replay.skip(period, deltaPerTick);
                        if (replay.isAtStart()) {
                            timeline.stop();
                            timeline = null;
                            endGif();
                        }
                    }
                }));
        timeline.playFromStart();
        lock.release();
    }

    @FXML
    private void politicalMapMode() {
        buffer = replay.politicalBuffer;
        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
    }

    @FXML
    private void refresh() {
        if (!lock.tryAcquire()) {
           return;
        }
        pause();
        scrollPane.setContent(null);
        imageView.setImage(null);
        int width = (int) output.getWidth();
        int height = (int) output.getHeight();
        WritableImage i = new WritableImage(width, height);
        i.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbPreInstance(), buffer, 0, width);
        output = i;
        imageView.setImage(i);
        scrollPane.setContent(imageView);
        lock.release();
    }

    @FXML
    private void religiousMapMode() {
        buffer = replay.religiousBuffer;
        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
    }

    @FXML
    private void resetZoom() {
        imageView.setFitHeight(replay.bufferHeight);
        imageView.setFitWidth(replay.bufferWidth);
        //if scrollbars were not visible we need to do this to make them appear
        //again if needed
        scrollPane.setContent(null);
        scrollPane.setContent(imageView);
    }

    @FXML
    private void serialize() {
        if (replay.saveGame == null) {
            return;
        }
        if (!lock.tryAcquire()) {
           return;
        }
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(serDirectory);
        fileChooser.setInitialFileName(file.getName() + ".ser");
        fileChooser.setTitle(l10n("replay.eu4ser.select"));

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(l10n("replay.eu4ser.ext"), "*.ser");
        fileChooser.getExtensionFilters().add(extFilter);
        //Show open file dialog
        final File file = fileChooser.showSaveDialog(getWindow());
        if (file == null) {
            lock.release();
            return;
        }
        serDirectory = file.getParentFile();
        settings.setProperty("ser.dir", serDirectory.getPath());

        try {
            replay.serializeSave(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText(e.getLocalizedMessage());
        }
        lock.release();
    }

    @FXML
    private void subjectsAsOverlords() {
        settings.setProperty("subjects.as.overlord", subjectsCheckMenuItem.isSelected() ? "true" : "false");
    }

    @FXML
    private void technologyCombinedMapMode() {
        buffer = replay.technologyCombinedBuffer;
        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
    }

    @FXML
    private void technologySeparateMapMode() {
        buffer = replay.technologySeparateBuffer;
        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
    }

    @FXML
    private void toStart() {
        if (!lock.tryAcquire()) {
           return;
        }
        if (replay.saveGame == null || !replay.dateGenerator.hasPrev()) {
            lock.release();
            return;
        }
        lock.release();
        dateEdit.setText(replay.saveGame.startDate.toString());
        jump();
    }

    @FXML
    private void zoomIn() {
        Bounds bounds = imageView.getBoundsInParent();
        final int x = scrollProcentToMapCoord(
                scrollPane.getHvalue(), replay.bufferWidth,
                scrollPane.getWidth(), bounds.getWidth());
        final int y = scrollProcentToMapCoord(
                scrollPane.getVvalue(), replay.bufferHeight,
                scrollPane.getHeight(), bounds.getHeight());
        imageView.setFitHeight(imageView.getFitHeight() + zoomStep);
        imageView.setFitWidth(imageView.getFitWidth() + zoomStep);
        bounds = imageView.getBoundsInParent();
        scrollPane.setHvalue(mapCoordToScrollProcent(
                x, replay.bufferWidth, scrollPane.getWidth(), bounds.getWidth()));
        scrollPane.setVvalue(mapCoordToScrollProcent(
                y, replay.bufferHeight, scrollPane.getHeight(), bounds.getHeight()));
    }

    @FXML
    private void zoomOut() {
        Bounds bounds = imageView.getBoundsInParent();
        final int x = scrollProcentToMapCoord(
                scrollPane.getHvalue(), replay.bufferWidth,
                scrollPane.getWidth(), bounds.getWidth());
        final int y = scrollProcentToMapCoord(
                scrollPane.getVvalue(), replay.bufferHeight,
                scrollPane.getHeight(), bounds.getHeight());
        double h = imageView.getFitHeight() - zoomStep;
        imageView.setFitHeight(h < 0 ? imageView.getFitHeight() : h);
        double w = imageView.getFitWidth() - zoomStep;
        imageView.setFitWidth(w < 0 ? imageView.getFitWidth() : w);
        bounds = imageView.getBoundsInParent();
        scrollPane.setHvalue(mapCoordToScrollProcent(
                x, replay.bufferWidth, scrollPane.getWidth(), bounds.getWidth()));
        scrollPane.setVvalue(mapCoordToScrollProcent(
                y, replay.bufferHeight, scrollPane.getHeight(), bounds.getHeight()));
    }

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        System.out.printf(l10n("replay.initializating"));
        log.prefWidthProperty().bind(logContainer.widthProperty());
        provinceLog.prefWidthProperty().bind(provinceContainer.widthProperty());
        progressBar.prefWidthProperty().bind(bottom.widthProperty());

        //center map if too small
        imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                final Bounds bounds = imageView.getBoundsInParent();
                if (bounds.getWidth() < scrollPane.getWidth()) {
                    imageView.setTranslateX((scrollPane.getWidth() - bounds.getWidth()) / 2);
                } else {
                    imageView.setTranslateX(0);
                }
                if (bounds.getHeight()< scrollPane.getHeight()) {
                    imageView.setTranslateY((scrollPane.getHeight()- bounds.getHeight()) / 2);
                } else {
                    imageView.setTranslateY(0);
                }
            }
        });

        //mouse wheel to zoom in/out the map
        scrollPane.addEventFilter(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent t) {
                if (t.getDeltaY() < 0) {
                    zoomOut();
                } else {
                    zoomIn();
                }
                t.consume();
            }
        });

        //to prevent menu from closing
        final EventHandler<MouseEvent> filter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                t.consume();
            }
        };
        //add change handler and event filter to event menu
        for(final MenuItem item : eventMenu.getItems()) {
            if (item instanceof CustomMenuItem) {
                final String event = item.getText();
                final CustomMenuItem customItem = ((CustomMenuItem) item);
                if (customItem.getContent() instanceof CheckBox) {
                    final CheckBox checkBox = (CheckBox) customItem.getContent();
                    checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(final ObservableValue<? extends Boolean> ov, final Boolean oldVal, final Boolean newVal) {
                            if (newVal) {
                                replay.notableEvents.add(event);
                            } else {
                                replay.notableEvents.remove(event);
                            }
                        }
                    });
                    checkBox.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
                }
            }
        }

        daysCombo.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        daysCombo.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                try {
                    final int parsed = Integer.parseInt(newVal);
                    if (parsed <= 0) {
                        daysCombo.setValue(oldVal);
                        return;
                    }
                    deltaPerTick = parsed;
                    settings.setProperty("delta.per.tick", newVal);
                } catch (NumberFormatException e) {
                    daysCombo.setValue(oldVal);
                }
            }
        });

        periodCombo.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        periodCombo.getItems().addAll(
                l10n("fxml.settings.period.days"),
                l10n("fxml.settings.period.months"),
                l10n("fxml.settings.period.years")
        );
        periodCombo.getSelectionModel().selectFirst();
        periodCombo.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                if (newVal == null) {
                    return;
                }
                switch (newVal.intValue()) {
                    case 0:
                        period = Date.DAY;
                        break;
                    case 1:
                        period = Date.MONTH;
                        break;
                    case 2:
                        period = Date.YEAR;
                        break;
                    default:
                        periodCombo.getSelectionModel().select(oldVal.intValue());
                        return;
                }
                settings.setProperty("period.per.tick", newVal.toString());
            }
        });

        focusEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        focusEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                replay.setFocus(newVal);
                settings.setProperty("focus", newVal);
            }
        });

        langCombo.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (newVal == null) {
                    return;
                }
                try {
                    Locale.setDefault(new Locale(newVal));
                    settings.setProperty("locale.language", newVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        imageView.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                final Bounds imageBounds = imageView.getBoundsInParent();
                int x = (int) (t.getX() * replay.bufferWidth / imageBounds.getWidth());
                int y = (int) (t.getY() * replay.bufferHeight / imageBounds.getHeight());
                final String coords = "[" + x + "," + y + "]\n";
                final ProvinceInfo pi = replay.colors.get(replay.getProvinceColor(x, y));
                final String provinceHint = coords + (pi != null ? pi.toString() : "");
                if (!scrollPane.getTooltip().getText().equals(provinceHint)) {
                    scrollPane.setTooltip(new Tooltip(provinceHint));
                }
            }
        });

        imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                final Bounds imageBounds = imageView.getBoundsInParent();
                int x = (int) (t.getX() * replay.bufferWidth / imageBounds.getWidth());
                int y = (int) (t.getY() * replay.bufferHeight / imageBounds.getHeight());
                selectedProvince = replay.colors.get(replay.getProvinceColor(x, y));
                if (selectedProvince != null) {
                    final String provinceLogContent = selectedProvince.getLog();
                    if (!provinceLogContent.equals(selectedProvinceLogContent)) {
                        provinceLog.getEngine().loadContent(provinceLogContent);
                        selectedProvinceLogContent = provinceLogContent;
                    }
                }
            }
        });

        dateEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldVal, final String newVal) {
                if (replay.dateGenerator == null) {
                    return;
                }
                jumpButton.setVisible(!newVal.equals(replay.getDate().toString()));
            }
        });

        gifWidthEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifWidthEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                int gifWidth;
                try {
                    gifWidth = Integer.parseInt(newVal);
                    if (gifWidth < 0) {
                        gifWidth = 0;
                    }
                } catch (NumberFormatException e) {
                    gifWidth = 0;
                }
                settings.setProperty("gif.width", Integer.toString(gifWidth));
            }
        });

        gifHeightEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifHeightEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                int gifHeight;
                try {
                    gifHeight = Integer.parseInt(newVal);
                    if (gifHeight < 0) {
                        gifHeight = 0;
                    }
                } catch (NumberFormatException e) {
                    gifHeight = 0;
                }
                settings.setProperty("gif.height", Integer.toString(gifHeight));
            }
        });

        gifBreakEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifBreakEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                int gifBreak;
                try {
                    gifBreak = Integer.parseInt(newVal);
                    if (gifBreak < 0) {
                        gifBreak = 0;
                    }
                } catch (NumberFormatException e) {
                    gifBreak = 0;
                }
                settings.setProperty("gif.new.file", Integer.toString(gifBreak));
            }
        });

        gifStepEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifStepEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                int gifStep;
                try {
                    gifStep = Integer.parseInt(newVal);
                    if (gifStep <= 0) {
                        gifStep = 1;
                    }
                } catch (NumberFormatException e) {
                    gifStep = 1;
                }
                settings.setProperty("gif.step", Integer.toString(gifStep));
            }
        });

        gifDateColorPicker.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifDateColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> ov, Color oldVal, Color newVal) {
                if (!lock.tryAcquire()) {
                    gifDateColorPicker.setValue(oldVal);
                    return;
                }
                final int red = (int) (newVal.getRed() * 255);
                final int green = (int) (newVal.getGreen() * 255);
                final int blue = (int) (newVal.getBlue() * 255);
                settings.setProperty("gif.date.color", String.format("0x%02X%02X%02X", red, green, blue));
                if (giffer != null) {
                    giffer.setGifDateColor(new java.awt.Color(red, green, blue));
                }
                lock.release();
            }
        });

        gifDateSizeEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifDateSizeEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final float size = Float.parseFloat(newVal);
                    if (giffer != null) {
                        giffer.setGifDateSize(size);
                    }
                    settings.setProperty("gif.date.size", newVal);
                } catch (NumberFormatException e) {
                    //gifDateSizeEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifDateXEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifDateXEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int x = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifDateX(x);
                    }
                    settings.setProperty("gif.date.x", newVal);
                } catch (NumberFormatException e) {
                    //gifDateXEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifDateYEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifDateYEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int y = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifDateY(y);
                    }
                    settings.setProperty("gif.date.y", newVal);
                } catch (NumberFormatException e) {
                    //gifDateYEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifSubimageXEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifSubimageXEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int x = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifSubImageX(x);
                    }
                    settings.setProperty("gif.subimage.x", newVal);
                } catch (NumberFormatException e) {
                    //gifSubimageXEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifSubimageYEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifSubimageYEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int y = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifSubImageY(y);
                    }
                    settings.setProperty("gif.subimage.y", newVal);
                } catch (NumberFormatException e) {
                    //gifSubimageYEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifSubimageWidthEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifSubimageWidthEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int w = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifSubImageWidth(w);
                    }
                    settings.setProperty("gif.subimage.width", newVal);
                } catch (NumberFormatException e) {
                    //gifSubimageWidthEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        gifSubimageHeightEdit.addEventFilter(MouseEvent.MOUSE_CLICKED, filter);
        gifSubimageHeightEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                if (!lock.tryAcquire()) {
                    return;
                }
                try {
                    final int h = Integer.parseInt(newVal);
                    if (giffer != null) {
                        giffer.setGifSubImageHeight(h);
                    }
                    settings.setProperty("gif.subimage.height", newVal);
                } catch (NumberFormatException e) {
                    //gifSubimageHeightEdit.setText(oldVal);
                } finally {
                    lock.release();
                }
            }
        });

        final WebEngine provinceEngine = provinceLog.getEngine();
        provinceEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    JSObject win = (JSObject) provinceEngine.executeScript("window");
                    win.setMember("java", new JavascriptBridge());
                }
            }
        });

        log.setContextMenuEnabled(false); //throws exception when in fxml
        final ContextMenu cm = new ContextMenu();
        final MenuItem clearLog = new MenuItem(l10n("replay.log.clear"));
        clearLog.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                log.getEngine().loadContent(LOG_INIT);
            }
        });
        cm.getItems().add(clearLog);
        log.addEventHandler(MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        cm.show(log, e.getScreenX(), e.getScreenY());
                    }
                }
        });
        final WebEngine webEngine = log.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    JSObject win = (JSObject) webEngine.executeScript("window");
                    win.setMember("java", new JavascriptBridge());
                    webEngine.executeScript(SCROLL_DOWN);
                    log.getEngine().setUserStyleSheetLocation(getClass().getResource("Log.css").toExternalForm());
                }
            }
        });
        webEngine.setJavaScriptEnabled(true);
        webEngine.loadContent(LOG_INIT);
        logContent.append(LOG_HEADER);
    }

    /**
     * Sets Replayer settings. Adjusts saveDirectory etc. and loads map.
     * @param settings new settings
     */
    public void setSettings(final Properties settings) {
        this.settings = settings;

        replay = new Replay(settings);

        for(final MenuItem item : eventMenu.getItems()) {
            if (item instanceof CustomMenuItem) {
                final String event = item.getText();
                final CustomMenuItem customItem = ((CustomMenuItem) item);
                if (customItem.getContent() instanceof CheckBox) {
                    final CheckBox checkBox = (CheckBox) customItem.getContent();
                    checkBox.setSelected(replay.notableEvents.contains(event));
                }
            }
        }

        zoomStep = Integer.parseInt(settings.getProperty("zoom.step", "100"));

        daysCombo.getItems().addAll(settings.getProperty("list.delta.per.tick", "1;30;365").split(";"));
        daysCombo.getSelectionModel().select(settings.getProperty("delta.per.tick", "1"));
        try {
            periodCombo.getSelectionModel().select(Integer.parseInt(settings.getProperty("period.per.tick", "0")));
        } catch (NumberFormatException e) {
            periodCombo.getSelectionModel().selectFirst();
            settings.setProperty("period.per.tick", "0");
        }

        langCombo.getSelectionModel().select(settings.getProperty("locale.language", "en"));

        replay.drawBorders = "true".equals(settings.getProperty("borders", "false"));

        focusEdit.setText(settings.getProperty("focus", ""));

        saveDirectory = new File(settings.getProperty("save.dir", ""));
        if (!saveDirectory.exists() || !saveDirectory.isDirectory()) {
            saveDirectory = new File(Replay.DEFAULT_SAVE_DIR);
            if (!saveDirectory.exists() || !saveDirectory.isDirectory()) {
                saveDirectory = new File(System.getProperty("user.home"), "/");
            }
        }

        serDirectory = new File(settings.getProperty("ser.dir", ""));
        if (!serDirectory.exists() || !serDirectory.isDirectory()) {
            serDirectory = new File(Replay.DEFAULT_SAVE_DIR);
            if (!serDirectory.exists() || !serDirectory.isDirectory()) {
                serDirectory = new File(System.getProperty("user.home"), "/");
            }
        }

        subjectsCheckMenuItem.setSelected(replay.subjectsAsOverlords);

        gifMenu.setVisible(!settings.getProperty("gif", "false").equals("true"));
        gifLoopCheckMenuItem.setSelected(settings.getProperty("gif.loop", "true").equals("true"));
        gifWidthEdit.setText(settings.getProperty("gif.width", "0"));
        gifHeightEdit.setText(settings.getProperty("gif.height", "0"));
        gifBreakEdit.setText(settings.getProperty("gif.new.file", "0"));
        gifStepEdit.setText(settings.getProperty("gif.step", "100"));
        gifDateCheckMenuItem.setSelected(settings.getProperty("gif.date", "true").equals("true"));
        gifDateColorPicker.setValue(Color.web(settings.getProperty("gif.date.color", "0x000000")));
        gifDateSizeEdit.setText(settings.getProperty("gif.date.size", "12"));
        gifDateXEdit.setText(settings.getProperty("gif.date.x", "60"));
        gifDateYEdit.setText(settings.getProperty("gif.date.y", "60"));
        gifSubimageCheckMenuItem.setSelected(settings.getProperty("gif.subimage", "false").equals("true"));
        gifSubimageXEdit.setText(settings.getProperty("gif.subimage.x", "0"));
        gifSubimageYEdit.setText(settings.getProperty("gif.subimage.y", "0"));
        gifSubimageWidthEdit.setText(settings.getProperty("gif.subimage.width", ""));
        gifSubimageHeightEdit.setText(settings.getProperty("gif.subimage.height", ""));

        bordersCheckMenuItem.setSelected(settings.getProperty("borders", "false").equals("true"));
        disableLog = settings.getProperty("log.disable", "false").equals("true");

        eu4Directory = new File(settings.getProperty("eu4.dir"));
        try {
            lock.acquire();
            loadData();
        } catch (InterruptedException e) { }
    }

    /**
     * Ends gif.
     */
    private void endGif() {
        if (giffer != null) {
            giffer.endGif();
            giffer = null;
        }
        if (gifSwitchCheckMenuItem.isSelected()) {
            if (Platform.isFxApplicationThread()) {
                gifSwitchCheckMenuItem.setSelected(false);
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        gifSwitchCheckMenuItem.setSelected(false);
                    }
                });
            }
        }
    }

    /**
     * Returns title property.
     * @return title property
     */
    public StringProperty titleProperty() {
        return titleProperty;
    }

    /**
     * Returns window of the root.
     * @return window of the root
     */
    private Window getWindow() {
        return root.getScene().getWindow();
    }

    /**
     * Loads data in proper order.
     */
    private void loadData() {
        titleProperty.set(TITLE);
        imageView.setImage(null);
        scrollPane.setContent(null);
        final Task<Void> finisher = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        buffer = replay.politicalBuffer;
                        output = new WritableImage(replay.bufferWidth, replay.bufferHeight);
                        output.getPixelWriter().setPixels(0, 0, replay.bufferWidth, replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), buffer, 0, replay.bufferWidth);
                        progressBar.progressProperty().unbind();
                        progressBar.setProgress(0);
                        statusLabel.textProperty().unbind();
                        statusLabel.setText(l10n("replay.map.loaded"));
                        scrollPane.setContent(null);
                        imageView.setImage(output);
                        scrollPane.setContent(imageView);
                        int fitWidth = Integer.parseInt(settings.getProperty("map.fit.width", "0"));
                        int fitHeight = Integer.parseInt(settings.getProperty("map.fit.height", "0"));
                        imageView.setFitHeight(fitHeight);
                        imageView.setFitWidth(fitWidth);
                        lock.release();
                    }
                });
                return null;
            }
        };
        progressBar.progressProperty().bind(finisher.progressProperty());
        statusLabel.textProperty().bind(finisher.titleProperty());
        replay.loadData(finisher);
   }

    /**
     * Called when application is stopped to store settings and end gif if needed.
     */
    public void stop() {
        endGif();
        final StringBuilder s = new StringBuilder();
        for(String event : replay.notableEvents) {
            s.append(";");
            s.append(event);
        }
        settings.setProperty("events", s.substring(1));
    }

    /**
     * Updates gif for the date if possible.
     * @param date current date
     */
    public void updateGif(final Date date) {
        if (giffer != null) {
            giffer.updateGif(buffer, date);
            if (replay.isAtEnd()) {
                endGif();
            }
        }
    }

    /**
     * Appends {@link #logContent} to {@link #log} asynchronously.
     */
    public void updateLog() {
        final Document doc = log.getEngine().getDocument();
        logContent.append(LOG_FOOTER);
        final WebEngine e = new WebEngine();
        e.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    Node fragmentNode = e.getDocument().getElementById(LOG_ID);
                    fragmentNode = doc.importNode(fragmentNode, true);
                    doc.getElementById(LOG_ID).appendChild(fragmentNode);
                    log.getEngine().executeScript(SCROLL_DOWN);
                }
            }
        });
        e.loadContent(logContent.toString());
        logContent.setLength(LOG_HEADER.length());

        if (selectedProvince != null) {
            final String provinceLogContent = selectedProvince.getLog();
            if (!provinceLogContent.equals(selectedProvinceLogContent)) {
                provinceLog.getEngine().loadContent(provinceLogContent);
                selectedProvinceLogContent = provinceLogContent;
            }
        }
    }

    /**
     * Listens to changes of {@link #dateGenerator} and updates GUI.
     */
    class DateListener implements IDateListener {
        @Override
        public void update(final Date date, final double progress) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    dateEdit.setText(date.toString());
                    statusLabel.textProperty().unbind();
                    statusLabel.setText("");
                    progressBar.progressProperty().unbind();
                    progressBar.progressProperty().set(progress);
                }
            });
            updateGif(date);
        }
    }

    /**
     * Bridge between Javascript and JavaFX.
     * Its public methods are accessible from javascript in {@link #logContent}.
     */
    public class JavascriptBridge {

        /**
         * Centers {@link #scrollPane} to province, if possible.
         * @param prov id of the province
         * @return false to prevent link following and thus refreshing
         */
        public boolean prov(final String prov) {
            final Point center = replay.provinces.get(prov).center;
            if (center == null) {
                return false;
            }
            final Bounds imageBounds = imageView.getBoundsInParent();
            scrollPane.setHvalue(mapCoordToScrollProcent(
                    center.x, replay.bufferWidth, scrollPane.getWidth(), imageBounds.getWidth()));
            scrollPane.setVvalue(mapCoordToScrollProcent(
                    center.y, replay.bufferHeight, scrollPane.getHeight(), imageBounds.getHeight()));
            return false;
        }
    }
}
