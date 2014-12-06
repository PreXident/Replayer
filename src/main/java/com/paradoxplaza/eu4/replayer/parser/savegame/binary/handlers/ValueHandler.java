package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler.getToken;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Handles value token.
 * Does not support nesting!
 */
public class ValueHandler extends DefaultHandler {

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        super.handleToken(context);
        final Token token = getToken(context, 1); //token before =
        //reset flags
        final EnumSet<Flag> flags = context.getCurrentToken().flags;
        flags.remove(Flag.FLOAT);
        flags.remove(Flag.INTEGER);
        //ini flags
        if (token.index == 0xD928 //advisor
                || token.index == 0xDD28 //alliance
                || token.index == 0xE128 //vassal
                || token.index == 0x3729 //military_access
                || token.index == 0xEB2A //trade_prov
                || token.index == 0xE92C //colonize_prov
                || token.index == 0xEA2C //conquer_prov
                || token.index == 0xEC2C //building_prov
                || token.index == 0xEE2C //threat
                || token.index == 0xEF2C //protect
                || token.index == 0xF02C //antagonize
                || token.index == 0xF12C //befriend
                || token.index == 0xF72C //rival
                || token.index == 0x1A2F) { //protectorate
            flags.add(Flag.INTEGER);
        } else {
            flags.add(Flag.FLOAT);
        }
    }
}
