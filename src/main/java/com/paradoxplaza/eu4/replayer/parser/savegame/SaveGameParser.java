package com.paradoxplaza.eu4.replayer.parser.savegame;

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
     */
    public SaveGameParser(final SaveGame saveGame, final long size, final InputStream input) {
        super(saveGame, new Start(), size, input);
    }

    @Override
    protected void init() {
        updateTitle(l10n("parser.savegame"));
    }
}
