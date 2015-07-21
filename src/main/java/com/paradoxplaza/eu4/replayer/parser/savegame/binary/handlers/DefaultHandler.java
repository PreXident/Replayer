package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.savegame.Utils;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.NEW_LINE;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.SPACE;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Default handler of tokens from binary save stream.
 */
public class DefaultHandler implements IHandler {

    /** Convenient instance of the DefaultHandler. */
    static public DefaultHandler INSTANCE = new DefaultHandler();

    /** Bytes representing tabulators. */
    static protected final byte[] TAB = "\t".getBytes(IronmanStream.charset);

    /**
     * Reads count bytes from is.
     * @param is input stream to read from
     * @param count read this number of bytes
     * @return read bytes
     * @throws IOException when IOException occurs during reading
     * or not enough bytes are read
     */
    static protected byte[] readBytes(final InputStream is, final int count)
            throws IOException {
        final byte[] bytes = new byte[count];
        readBytes(is, bytes);
        return bytes;
    }

    /**
     * Reads count bytes from is.
     * @param is input stream to read from
     * @param out array to store read bytes
     * @throws IOException when IOException occurs during reading
     * or not enough bytes are read
     */
    static protected void readBytes(final InputStream is, final byte[] out)
            throws IOException {
        Utils.ensureRead(is, out);
    }

    /**
     * Returns index-th token in binary save context.
     * Performs no checking!
     * @param context binary save context
     * @param index token index
     * @return tokne on given index
     */
    static protected Token getToken(final IParserContext context, final int index) {
        Iterator<Token> it = context.getContext().iterator();
        for (int i = 0; i < index; ++i) {
            it.next();
        }
        return it.next();
    }

    @Override
    public void handled() {
        //nothing
    }

    @Override
    public void handleToken(final IParserContext context) throws IOException {
        printIndent(context);
        printText(context);
        popContext(context);
    }

    /**
     * Pops context if needed.
     * @param context binary save context
     */
    protected void popContext(final IParserContext context) {
        if (context.getLastToken().index == 0x0100) { // =
            context.getContext().pop().handler.handled();
        }
    }

    /**
     * Prints indent into context's output stream.
     * @param context binary save context
     * @throws IOException if any IO error occurs
     */
    protected void printIndent(final IParserContext context) throws IOException {
        if (!context.getPrettyPrint()) {
            context.getOutputStream().write(SPACE);
            return;
        }
        final Token token = context.getLastToken();
        if (token.index != 0x0100) { // =
            context.getOutputStream().write(NEW_LINE);
            final int indent = context.getIndent();
            for (int i = 0; i < indent; ++i) {
                context.getOutputStream().write(TAB);
            }
        }
    }

    /**
     * Prints indent into context's output stream.
     * @param context binary save context
     * @throws IOException if any IO error occurs
     */
    protected void printText(final IParserContext context) throws IOException {
        context.getOutputStream().write(context.getCurrentToken().bytes);
    }
}
