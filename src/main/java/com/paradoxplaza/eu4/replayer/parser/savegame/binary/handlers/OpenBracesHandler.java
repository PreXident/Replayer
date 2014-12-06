package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.NEW_LINE;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.SPACE;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler.TAB;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;

/**
 * Handles opening braces.
 */
public class OpenBracesHandler extends DefaultHandler {

    /** Here the next token will be read. */
    final byte[] bytes = new byte[2];

    /** Store flags here to restore flags for nested braces. */
    protected Deque<EnumSet<Flag>> flagStack = new ArrayDeque<>(16);

    /** Store brace tokens here to restore flags for nested braces. */
    protected Deque<Token> braceStack = new ArrayDeque<>(16); //probably not necessary, just to be sure

    @Override
    public void handled() {
        final Token brace = braceStack.pop();
        brace.flags.clear();
        brace.flags.addAll(flagStack.pop());
    }

    @Override
    protected void printIndent(final IParserContext context) throws IOException {
        if (context.getPrettyPrint()) {
            if (context.getContext().peek().index == 0x0300) { //nested lists empty line
                context.getOutputStream().write(NEW_LINE);
            }
            context.getOutputStream().write(NEW_LINE);
            final int indent = context.getIndent();
            for (int i = 0; i < indent; ++i) {
                context.getOutputStream().write(TAB);
            }
        } else {
            context.getOutputStream().write(SPACE);
        }
    }

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        //store flags into flagStack, store token into flagStack
        final Token brace = context.getCurrentToken();
        flagStack.push(EnumSet.copyOf(brace.flags));
        brace.flags.clear();
        braceStack.push(brace);
        //copy flags from determining token
        final Token list = context.getContext().peek(); //token before =
        brace.flags.addAll(list.flags);
        //do not print completely empty lists
        //what follows?
        readBytes(context.getInputStream(), bytes);
        final short nextToken = (short) ((bytes[0] << 8) + bytes[1]);
        context.getInputStream().unread(bytes);
        boolean emptyList =
                context.getLastToken().index != 0x0100 // =
                && nextToken == 0x0400; // }
        if (!emptyList) {
            printIndent(context);
        } else {
            brace.flags.add(Flag.EMPTY_LIST);
        }
        //update context
        context.getContext().push(brace);
        context.increaseIndent();
        //print
        if (!emptyList) {
            printText(context);
        }
    }
}
