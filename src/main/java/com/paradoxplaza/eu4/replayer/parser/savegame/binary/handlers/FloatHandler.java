package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.SPACE;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.charset;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

/**
 * Handles real numbers tokens.
 */
public class FloatHandler extends SingleValueHandler {

    /** Here the float bytes will be read. */
    final byte[] bytes = new byte[4];

    @Override
    protected void handleValue(final IParserContext context) throws IOException {
        readBytes(context.getInputStream(), bytes);
        final ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN); //reverse order
        final float f = bb.getFloat();
        final String string = String.format(Locale.ENGLISH, "%.3f", f);
        final byte[] out  = string.getBytes(charset);
        context.getOutputStream().write(out);
    }

    @Override
    public void printIndent(final IParserContext context) throws IOException {
        if (context.getPrettyPrint()
                && context.getContext().peek().index == 0x0300) { // {
            if (context.getLastToken().index == 0x0300) { // { -> first in list
                super.printIndent(context);
            } else {
                context.getOutputStream().write(SPACE);
            }
        } else {
            super.printIndent(context);
        }
    }
}
