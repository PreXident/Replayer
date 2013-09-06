package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import java.io.InputStream;

/**
 * Parser of save games
 */
public class SaveGameParser extends TextParser<SaveGame> {

    /**
     * Only constructor.
     * @param saveGame SaveGame to fill
     * @param size size of parsed file
     * @param input input to parse
     */
    public SaveGameParser(final SaveGame saveGame, final long size, final InputStream input) {
        super(saveGame, new Start(), size, input);
        updateTitle("Parsing save game...");
    }
}
