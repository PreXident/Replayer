package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.ITaskBridge;
import com.paradoxplaza.eu4.replayer.SaveGame;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import static com.paradoxplaza.eu4.replayer.parser.savegame.Utils.chooseStream;
import com.paradoxplaza.eu4.replayer.utils.Pair;
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

    /**
     * Only public constructor.
     * @param saveGame save game
     * @param size size of parsed file
     * @param input input stream to parse
     * @param bridge listening bridge
     * @throws IOException when any IO error occurs
     */
    public SaveGameParser(final SaveGame saveGame, final long size,
            final InputStream input, final ITaskBridge<SaveGame> bridge)
            throws IOException {
        this(saveGame, size, chooseStream(input), bridge);
    }

    /**
     * Private contructor to properly extract input stream size from
     * chooseStream result.
     * @param saveGame save game
     * @param size original input stream size
     * @param pair result of chooseStream, choosen stream and its size
     * @param bridge listening bridge
     * @throws IOException
     */
    private SaveGameParser(final SaveGame saveGame, final long size,
            final Pair<InputStream, Long> pair,
            final ITaskBridge<SaveGame> bridge)
            throws IOException {
        super(saveGame, new Start(), pair.getSecond() < 0 ? size : pair.getSecond(), pair.getFirst(), bridge);
    }

    @Override
    protected void init() {
        bridge.updateTitle(l10n("parser.savegame"));
    }
}
