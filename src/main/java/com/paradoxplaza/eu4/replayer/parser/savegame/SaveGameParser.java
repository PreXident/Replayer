package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import java.io.IOException;
import java.io.InputStream;

/**
 * Parser of save games
 */
public class SaveGameParser extends TextParser<SaveGame> {

    /**
     * Only constructor.
     * @param saveGame SaveGame to fill
     */
    public SaveGameParser(final SaveGame saveGame) {
        super(saveGame);
    }

    /**
     * Parses the input stream.
     * @param input stream to parse
     * @throws IOException if any error occurs
     */
    public void parse(final InputStream input) throws IOException {
        super.parse(new Start(), input);
    }
}
