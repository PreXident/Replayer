package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler.getToken;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Handles type token.
 * Does not support nesting!
 */
public class TypeHandler extends DefaultHandler {

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        super.handleToken(context);
        final Token token = getToken(context, 1); //token before =
        //reset flags
        final EnumSet<Flag> flags = context.getCurrentToken().flags;
        flags.remove(Flag.STRING);
        flags.remove(Flag.QUOTED_STRING);
        //ini flags
        if (token.index == 0xD928 //advisor
                || token.index == 0x802A) { //faction
            flags.add(Flag.STRING);
        } else {
            flags.add(Flag.QUOTED_STRING);
        }
    }
}
