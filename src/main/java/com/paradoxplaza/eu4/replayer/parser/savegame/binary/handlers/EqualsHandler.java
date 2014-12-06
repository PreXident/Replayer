package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import java.io.IOException;

/**
 * Handles equals token.
 */
public class EqualsHandler extends DefaultHandler {

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        printText(context);
        context.getContext().push(context.getLastToken());
    }
}
