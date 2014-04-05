package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.ITaskBridge;
import com.paradoxplaza.eu4.replayer.SaveGame;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Controls loading of a batch of saves.
 */
public class BatchSaveGameParser implements Runnable {

    /** Result save game. */
    final SaveGame saveGame;

    /** Save game files to parse. */
    final List<File> files;

    /** Flag indicating whether the RNW is used. */
    final boolean rnw;

    /** Index of currently running save game parser. */
    int currentIndex;

    /** Gets informed about progress. */
    final ITaskBridge<SaveGame> bridge;

    /** Adds file counter to title. */
    final BridgeDecorator decoratedBridge;

    /**
     * Only constructor.
     * @param rnw is RNW used?
     * @param saveGame SaveGame to fill
     * @param files sorted inputs to parse
     * @param bridge bridge listening to progress
     */
    public BatchSaveGameParser(final boolean rnw,
            final SaveGame saveGame, final List<File> files,
            final ITaskBridge<SaveGame> bridge) {
        this.saveGame = saveGame;
        this.files = files;
        this.rnw = rnw;
        this.bridge = bridge;
        this.decoratedBridge = new BridgeDecorator(bridge);
    }

    @Override
    public void run() {
        bridge.updateTitle(l10n("parser.batch.init"));
        SaveGameParser.synchronizeProvinces = rnw;
        runParser(saveGame, 0);
        SaveGameParser.synchronizeProvinces = false;
        for (int i = 1; i < files.size(); ++i) {
            final SaveGame currentSaveGame = runParser(new SaveGame(), i);
            saveGame.concatenate(currentSaveGame);
        }
        bridge.updateValue(saveGame);
    }

    /**
     * Creates and runs the save game parser.
     * Sets currentFile and currentIndex as it runs.
     * @param saveGame store parse result here
     * @param index which file to process
     * @return processed save game
     * @throws FileNotFoundException if file is not found
     */
    private SaveGame runParser(final SaveGame saveGame, final int index) {
        try {
            currentIndex = index;
            final File currentFile = files.get(index);
            final InputStream is = new FileInputStream(currentFile);
            final SaveGameParser parser = new SaveGameParser(
                    saveGame, currentFile.length(), is, decoratedBridge);
            parser.run();
            return saveGame;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return saveGame;
        }
    }

    /**
     * Simple decorator to add file counter to title.
     */
    class BridgeDecorator implements ITaskBridge<SaveGame> {

        /** Decorated bridge/ */
        final ITaskBridge<SaveGame> decorated;

        /**
         * Only constructor.
         * @param decorated decorated bridge
         */
        public BridgeDecorator(final ITaskBridge<SaveGame> decorated) {
            this.decorated = decorated;
        }

        @Override
        public boolean isCancelled() {
            return decorated.isCancelled();
        }

        @Override
        public void run() {
            decorated.run();
        }

        @Override
        public void updateMessage(String message) {
            decorated.updateMessage(message);
        }

        @Override
        public void updateProgress(double workDone, double max) {
            decorated.updateProgress(workDone, max);
        }

        @Override
        public void updateProgress(long workDone, long max) {
            decorated.updateProgress(workDone, max);
        }

        @Override
        public void updateTitle(String title) {
            decorated.updateTitle(title + " (" + (currentIndex + 1) + "/" + files.size() + ")");
        }

        @Override
        public void updateValue(SaveGame value) {
            decorated.updateValue(value);
        }
    }
}
