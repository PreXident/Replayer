package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler.getToken;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Handles countries token.
 * Does not support nesting!
 */
public class CountriesHandler extends DefaultHandler {

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        super.handleToken(context);
        final Token token = getToken(context, 1); //token before =
        //reset flags
        final EnumSet<Flag> flags = context.getCurrentToken().flags;
        flags.remove(Flag.STRING);
        flags.remove(Flag.QUOTED_STRING);
        flags.remove(Flag.PRETTY_LIST);
        //ini flags
        if (token.index == 0x0000) { //EU4txt
            flags.add(Flag.STRING);
            flags.add(Flag.PRETTY_LIST);
        } else {
            flags.add(Flag.QUOTED_STRING);
        }
    }
}
