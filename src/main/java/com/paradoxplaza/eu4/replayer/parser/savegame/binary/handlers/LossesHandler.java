package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler.getToken;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Handles losses token.
 * Does not support nesting!
 */
public class LossesHandler extends DefaultHandler {

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        super.handleToken(context);
        final Token token = getToken(context, 3); //token before attacker/defender
        //reset flags
        final EnumSet<Flag> flags = context.getCurrentToken().flags;
        flags.remove(Flag.FLOAT);
        flags.remove(Flag.INTEGER);
        //ini flags
        if (token.index == 0x1D29) { //land_combat
            flags.add(Flag.FLOAT);
        } else {
            flags.add(Flag.INTEGER);
        }
    }
}
