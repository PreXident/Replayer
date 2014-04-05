package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.ITaskBridge;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import java.io.InputStream;

/**
 * Parser of save games
 */
public class SaveGameParser extends TextParser<SaveGame> {

    /**
     * Hack attribute to insert current province info into events.
     * This is needed for RNW, as current info is not stored in history.
     */
    static boolean synchronizeProvinces = false;

    /**
     * Only constructor.
     * @param saveGame SaveGame to fill
     * @param size size of parsed file
     * @param input input to parse
     * @param bridge bridge listening to progress
     */
    public SaveGameParser(final SaveGame saveGame, final long size,
            final InputStream input, final ITaskBridge<SaveGame> bridge) {
        super(saveGame, new Start(), size, input, bridge);
    }

    @Override
    protected void init() {
        bridge.updateTitle(l10n("parser.savegame"));
    }
}
