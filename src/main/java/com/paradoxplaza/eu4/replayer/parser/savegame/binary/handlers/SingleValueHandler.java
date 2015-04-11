package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.charset;
import java.io.IOException;

/**
 * Common ancestor for single value tokens.
 */
public abstract class SingleValueHandler extends DefaultHandler {

    /** Bytes representing quote. */
    static final byte[] QUOTE = "\"".getBytes(charset);

    /**
     * Converts byte array to integer it represents.
     * @param bytes convert these bytes
     * @return converted integer
     */
    static public int toNumber(final byte[] bytes) {
        return toNumber(bytes, 0, bytes.length);
    }

    /**
     * Converts byte array to integer it represents.
     * @param bytes convert these bytes
     * @param offset start from this index
     * @param length use this number of bytes
     * @return converted integer
     */
    static public int toNumber(final byte[] bytes, final int offset, final int length) {
        int number = 0;
        for(int i = length + offset - 1; i >= offset; --i) {
            number <<= 8;
            number += bytes[i] & 0xff;
        }
        return number;
    }

    /**
     * Handles single value in binary save context.
     * @param context binary save context
     * @throws java.io.IOException if any IO error occurs
     */
    protected abstract void handleValue(final IParserContext context) throws IOException;

    @Override
    protected void printText(final IParserContext context) throws IOException {
        handleValue(context);
    }
}
