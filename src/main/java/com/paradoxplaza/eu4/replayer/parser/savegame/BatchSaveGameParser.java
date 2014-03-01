package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.SaveGame;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

/**
 * Controls loading of a batch of saves.
 */
public class BatchSaveGameParser extends Task<SaveGame> {

    /** Result save game. */
    final SaveGame saveGame;

    /** Save game files to parse. */
    final File[] files;

    /** Currently running save game parser. */
    SaveGameParser currentParser;

    /**
     * Only constructor.
     * @param saveGame SaveGame to fill
     * @param files sorted inputs to parse
     */
    public BatchSaveGameParser(final SaveGame saveGame, final File[] files) {
        this.saveGame = saveGame;
        this.files = files;
    }

    @Override
    protected SaveGame call() throws Exception {
        updateTitle(l10n("parser.batch.init"));
        runParser(saveGame, 0);
        for (int i = 1; i < files.length; ++i) {
            final SaveGame currentSaveGame = runParser(new SaveGame(), i);
            saveGame.concatenate(currentSaveGame);
        }
        return saveGame;
    }

    @Override
    protected void cancelled() {
        if (currentParser != null) {
            currentParser.cancel();
        }
    }

    /**
     * Creates and runs the save game parser.
     * Sets currentFile and currentIndex as it runs.
     * @param saveGame store parse result here
     * @param index which file to process
     * @return processed save game
     * @throws FileNotFoundException if file is not found
     */
    private SaveGame runParser(final SaveGame saveGame, final int index)
            throws FileNotFoundException {
        final File currentFile = files[index];
        final InputStream is = new FileInputStream(currentFile);
        currentParser = new SaveGameParser(saveGame, currentFile.length(), is);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                currentParser.titleProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                        updateTitle(currentFile.getName() + " (" + (index + 1) + "/" + files.length + ") - " + t1);
                    }
                });
                currentParser.progressProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                        updateProgress(1, 1 / t1.doubleValue());
                    }
                });
            }
        });
        currentParser.run();
        return saveGame;
    }
}
