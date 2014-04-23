package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.ITaskBridge;
import com.paradoxplaza.eu4.replayer.SaveGame;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

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
     * Wraps is into IronmanStream if needed.
     * @param stream save game stream
     * @return stream wrapped to IronmanStream if needed
     * @throws IOException if IO error occurs
     */
    static private InputStream chooseStream(final InputStream stream)
            throws IOException {
        final PushbackInputStream push = new PushbackInputStream(stream, 6);
        final byte[] bytes = new byte[6];
        push.read(bytes);
        push.unread(bytes);
        if (new String(bytes).equals("EU4bin")) {
            return new IronmanStream(push); //it's ok, this stream's buffer is 6
        } else {
            return push;
        }
    }

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
