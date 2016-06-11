package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Handles strange token from multiplayer saves by ignoring everything to
 * PLAYERS, used_colonial_names or speed tokens.
 */
public class RubbishHandler extends SingleValueHandler {

    /**
     * Reads byte from the stream or throws IOException.
     * @param stream stream to read from
     * @return byte read
     * @throws IOException if stream is eof
     */
    private byte readByte(final InputStream stream) throws IOException {
        int buf = stream.read();
        if (buf == -1) {
            throw new IOException(l10n("parser.eof.unexpected"));
        }
        return (byte) buf;
    }
    
    @Override
    protected void handleValue(final IParserContext context) throws IOException {
        final PushbackInputStream stream = context.getInputStream();
        byte byte1 = readByte(stream);
        byte byte2 = readByte(stream);
        while (true) {
            if ((byte1 == 0x11 && byte2 == 0x2F) //used_colonial_names
                    || (byte1 == 0x3F && byte2 == 0x2F) //PLAYERS
                    || (byte1 == 0x6E && byte2 == 0x00)) { //speed
                stream.unread(byte2);
                stream.unread(byte1);
                break;
            }
            byte1 = byte2;
            byte2 = readByte(stream);
        }
    }
}
