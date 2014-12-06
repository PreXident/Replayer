package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.NEW_LINE;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.SPACE;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.charset;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler.getToken;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;

/**
 * Handles string value.
 */
public class StringHandler extends SingleValueHandler {

    /** Bytes representing three dashes. */
    static protected final byte[] DASHES = "---".getBytes(charset);

    /** Store flags here to restore flags for nested strings. */
    protected Deque<EnumSet<Flag>> flagStack = new ArrayDeque<>(16);

    /** Store brace tokens here to restore flags for nested strings. */
    protected Deque<Token> stringStack = new ArrayDeque<>(16); //probably not necessary, just to be sure

    /** Here the string size will be read. */
    final byte[] lengthBytes = new byte[2];

    byte[] string = new byte[32];

    @Override
    public void handled() {
        final Token token = stringStack.pop();
        token.flags.clear();
        token.flags.addAll(flagStack.pop());
    }

    @Override
    protected void handleValue(final IParserContext context) throws IOException {
        readBytes(context.getInputStream(), lengthBytes);
        int number = toNumber(lengthBytes);
        if (number > string.length) {
            string = new byte[number];
        }
        context.getInputStream().read(string, 0, number);
        //hack: ignore last character if newline
        if (number > 0 && string[number-1] == 0x0A) {
            --number;
        }
        //
        final OutputStream output = context.getOutputStream();
        final EnumSet<Flag> flags = context.getContext().peek().flags;
        if (flags.contains(Flag.QUOTED_STRING)) {
            output.write(QUOTE);
        }
        if (!flags.contains(Flag.STRING)
                && !flags.contains(Flag.QUOTED_STRING)) {
            Token token = context.getContext().peek();
            if (token.index == 0x0300) { // {
                token = getToken(context, 1);
            }
            System.err.printf("No string flag specified for %s (0x%04X)\n", token.text, token.index);
        }
        if (flags.contains(Flag.EMPTY_STRING_DASH) && number == 0) {
            output.write(DASHES);
        } else {
            output.write(string, 0, number);
        }
        if (flags.contains(Flag.QUOTED_STRING)) {
            output.write(QUOTE);
        }
        //end token
        if (context.getPrettyPrint()
                && context.getContext().peek().index == 0x0300 // {
                && flags.contains(Flag.QUOTED_STRING)) {
            context.getOutputStream().write(NEW_LINE);
            context.getOutputStream().write(SPACE);
        }
        //
        //copy flags, needed from string=value
        //store flags into flagStack, store token into flagStack
        final Token token = context.getCurrentToken();
        flagStack.push(EnumSet.copyOf(token.flags));
        token.flags.clear();
        stringStack.push(token);
        //copy flags from determining token
        token.flags.addAll(flags);
    }

    @Override
    public void printIndent(final IParserContext context) throws IOException {
        if (context.getPrettyPrint()
                && context.getContext().peek().index == 0x0300 // {
                && context.getContext().peek().flags.contains(Flag.QUOTED_STRING)) {
            if (context.getLastToken().index == 0x0300) { // { -> first in list
                super.printIndent(context);
            }
        } else {
            super.printIndent(context);
        }
    }
}
