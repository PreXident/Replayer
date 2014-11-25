package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.ITaskBridge;
import com.paradoxplaza.eu4.replayer.SaveGame;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import static com.paradoxplaza.eu4.replayer.parser.savegame.Utils.chooseStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Parser of save games.
 */
class SaveGameParser extends TextParser<SaveGame> {

    /**
     * Hack attribute to insert current province info into events.
     * This is needed for RNW, as current info is not stored in history.
     */
    static boolean synchronizeProvinces = false;

    public SaveGameParser(final SaveGame saveGame, final long size,
            final InputStream input, final ITaskBridge<SaveGame> bridge)
            throws IOException {
        super(saveGame, new Start(), size, chooseStream(input), bridge);
    }

    @Override
    protected void init() {
        bridge.updateTitle(l10n("parser.savegame"));
    }
}
