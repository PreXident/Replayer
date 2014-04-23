package com.paradoxplaza.eu4.replayer.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple wrapper to prevent accidantal closing of System.out.
 */
public class UnclosableOutputStream extends OutputStream {

    /** Decorated stream. */
    private final OutputStream stream;

    /**
     * Only constructor.
     * @param stream InputStream to decorate
     */
    public UnclosableOutputStream(final OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }
}
