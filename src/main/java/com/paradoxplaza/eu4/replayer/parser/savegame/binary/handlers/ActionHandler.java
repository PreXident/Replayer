package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Handles action token.
 * Does not support nesting!
 */
public class ActionHandler extends DefaultHandler {

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        super.handleToken(context);
        final Token token = getToken(context, 1); //token before =
        //reset flags
        final EnumSet<Flag> flags = context.getCurrentToken().flags;
        flags.remove(Flag.QUOTED_STRING);
        flags.remove(Flag.QUOTED_DATE);
        flags.remove(Flag.INTEGER);
        //ini flags
        if (token.index == 0x8201) { //diplomacy_construction
            flags.add(Flag.QUOTED_STRING);
        } else if (token.index == 0xD62A) { //envoy
            flags.add(Flag.INTEGER);
        } else {
            flags.add(Flag.QUOTED_DATE);
        }
    }
}
