package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.charset;
import java.io.IOException;

/**
 * Handles boolean values.
 */
public class BooleanHandler extends SingleValueHandler {

    /** Bytes representing yes. */
    static protected final byte[] YES = "yes".getBytes(charset);

    /** Bytes representing yes. */
    static protected final byte[] NO = "no".getBytes(charset);

    @Override
    protected void handleValue(final IParserContext context) throws IOException {
        final int flag = context.getInputStream().read();
        if (flag == -1) {
            throw new IOException(l10n("parser.eof.unexpected"));
        }
        context.getOutputStream().write(flag > 0 ? YES : NO);
    }
}
