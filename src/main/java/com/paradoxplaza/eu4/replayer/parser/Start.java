package com.paradoxplaza.eu4.replayer.parser;

import java.io.InputStream;
import java.io.StreamTokenizer;

/**
 * Represents starting state of TextParser.
 */
public abstract class Start<Context> extends State<Context> {

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Start(final State<Context> parent) {
        super(parent);
    }

    /**
     * Creates tokenizer used by TextParser. Quote character is always '"'.
     * @param input InputStream to parse
     * @return new StreamTokenizer
     */
    public final StreamTokenizer createTokenizer(final InputStream input) {
        final StreamTokenizer t = _createTokenizer(input);
        t.quoteChar('"');
        return t;
    }

    /**
     * Creates tokenizer used by TextParser. Quote character is always '"'.
     * @param input InputStream to parse
     * @return new StreamTokenizer
     */
    protected abstract StreamTokenizer _createTokenizer(final InputStream input);
}
