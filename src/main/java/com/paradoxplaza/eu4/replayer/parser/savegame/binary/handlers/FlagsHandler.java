package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler.getToken;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Handles flags token.
 * Does not support nesting!
 */
public class FlagsHandler extends DefaultHandler {

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        super.handleToken(context);
        //reset flags
        final EnumSet<Flag> flags = context.getCurrentToken().flags;
        flags.remove(Flag.DATE);
        flags.remove(Flag.INTEGER);
        if (context.getContext().size() < 4) {
            flags.add(Flag.DATE);
        } else {
            final Token token = getToken(context, 3); //token country tag
            //ini flags
            if (token.index == 0x612E) { //active_relations
                flags.add(Flag.INTEGER);
            } else {
                flags.add(Flag.DATE);
            }
        }
    }
}
