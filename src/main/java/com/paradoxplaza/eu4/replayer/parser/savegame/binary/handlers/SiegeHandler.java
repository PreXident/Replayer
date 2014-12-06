package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler.getToken;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Handles siege token.
 * Does not support nesting!
 */
public class SiegeHandler extends DefaultHandler {

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        super.handleToken(context);
        final Token token = getToken(context, 1); //token before =
        //reset flags
        final EnumSet<Flag> flags = context.getCurrentToken().flags;
        flags.remove(Flag.FLOAT);
        flags.remove(Flag.INTEGER);
        //ini flags
        if (token.index == 0x0C00 || token.index == 0x1400) { //integer - province id
            flags.add(Flag.FLOAT);
        } else {
            flags.add(Flag.INTEGER);
        }
    }
}
