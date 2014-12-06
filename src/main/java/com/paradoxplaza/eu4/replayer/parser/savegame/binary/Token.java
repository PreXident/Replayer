package com.paradoxplaza.eu4.replayer.parser.savegame.binary;

import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.charset;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.IHandler;
import java.util.EnumSet;

/**
 * Information how the token should be processed.
 */
public class Token {

    /** Token itself. */
    public final int index;

    /** Output text. Is not null. */
    public final String text;

    /** Byte representation of the text. */
    public final byte[] bytes;

    /** Flags. */
    public final EnumSet<Flag> flags;

    /** Token handler. */
    public final IHandler handler;

    /**
     * Constructor for unknown tokens.
     * @param index token index
     * @param text token text
     */
    public Token(final int index, final String text) {
        this(index, text, EnumSet.of(Flag.UNKNOWN), DefaultHandler.INSTANCE);
    }

    /**
     * Default constructor.
     * @param index token index
     * @param text token text
     * @param flags token flags
     * @param handler token handler
     */
    public Token(final int index, final String text,
            final EnumSet<Flag> flags, final IHandler handler) {
        assert text != null : "text cannot be null, use empty string instead!";
        this.index = index;
        this.text = text;
        this.bytes = text.getBytes(charset);
        this.flags = flags;
        this.handler = handler;
    }

    @Override
    public String toString() {
        return String.format("%s (0x%04X)", text, index);
    }
}