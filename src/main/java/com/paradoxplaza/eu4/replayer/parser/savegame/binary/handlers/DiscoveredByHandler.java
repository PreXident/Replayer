package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Handles discovered_by token.
 * Does not support nesting!
 */
public class DiscoveredByHandler extends DefaultHandler {

    /** Here the next two tokens will be read. */
    final byte[] bytes = new byte[4];

    /** Flag indicating whether pretty print flag has been changed. */
    boolean changedPrettyPrint = false;

    /** ParserContext with changed pretty print. */
    IParserContext cachedContext = null;

    @Override
    public void handled() {
        if (changedPrettyPrint) {
            cachedContext.setPrettyPrint(true);
        }
    }

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        super.handleToken(context);
        //reset flags
        final EnumSet<Flag> flags = context.getCurrentToken().flags;
        flags.remove(Flag.STRING);
        flags.remove(Flag.QUOTED_STRING);
        //what does follow?
        readBytes(context.getInputStream(), bytes);
        final short token1 = (short) ((bytes[0] << 8) + bytes[1]);
        final short token2 = (short) ((bytes[2] << 8) + bytes[3]);
        changedPrettyPrint = false;
        //does the list follow?
        if (token1 == (short) 0x0100 /*=*/
                && token2 == (short) 0x0300 /*{*/) {
            flags.add(Flag.STRING);
            if (context.getPrettyPrint()) {
                context.setPrettyPrint(false);
                changedPrettyPrint = true;
                cachedContext = context;
            }
        } else {
            flags.add(Flag.QUOTED_STRING);
        }
        context.getInputStream().unread(bytes);
    }
}
