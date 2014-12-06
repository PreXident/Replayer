package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import java.io.IOException;
import java.util.Deque;

/**
 * Handles closing braces.
 */
public class CloseBracesHandler extends DefaultHandler {

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        context.decreaseIndent();
        if (!context.getContext().peek().flags.contains(Flag.EMPTY_LIST)) {
            super.handleToken(context);
        }
        final Deque<Token> c = context.getContext();
        c.pop().handler.handled(); //matching brace
        if (c.peek().index != 0x0300) { //cannot pop two braces!
            context.getContext().pop().handler.handled();
        }
    }
}
