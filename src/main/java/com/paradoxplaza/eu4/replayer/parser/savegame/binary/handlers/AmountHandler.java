package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler.getToken;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Handles amount token.
 * Does not support nesting!
 */
public class AmountHandler extends DefaultHandler {

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        super.handleToken(context);
        final Token token = getToken(context, 1); //token before =
        //reset flags
        final EnumSet<Flag> flags = context.getCurrentToken().flags;
        flags.remove(Flag.FLOAT);
        flags.remove(Flag.INTEGER);
        //ini flags
        if (token.index == 0x4028 //monarch
                || token.index == 0xEF2F //foreign_heir
                || token.index == 0x2A2A //subsidies
                || token.index == 0x092E) { //transfer_trade_power
            flags.add(Flag.FLOAT);
        } else {
            flags.add(Flag.INTEGER);
        }
    }
}
