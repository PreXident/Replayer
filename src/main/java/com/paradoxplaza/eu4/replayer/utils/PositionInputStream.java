package com.paradoxplaza.eu4.replayer.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream that is aware of cursor position in stream.
 * Take from <a href="http://stackoverflow.com/questions/240294/given-a-java-inputstream-how-can-i-determine-the-current-offset-in-the-stream">here</a>.
 * Synchronization removed.
 */
public final class PositionInputStream extends FilterInputStream {

    /** Postion in stream. */
    private long pos = 0;

    /** Mark position. */
    private long mark = 0;

    /**
     * Only constructor.
     * @param in decorated stream
     */
    public PositionInputStream(final InputStream in) {
        super(in);
    }

    /**
     * <p>Get the stream position.</p>
     *
     * <p>Eventually, the position will roll over to a negative number.
     * Reading 1 Tb per second, this would occur after approximately three
     * months. Applications should account for this possibility in their
     * design.</p>
     *
     * @return the current stream position.
     */
    public long getPosition() {
        return pos;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b >= 0) {
            pos += 1;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0) {
            pos += n;
        }
        return n;
    }

    @Override
    public long skip(long skip) throws IOException {
        long n = super.skip(skip);
        if (n > 0) {
          pos += n;
        }
        return n;
    }

    @Override
    public void mark(int readlimit) {
        super.mark(readlimit);
        mark = pos;
    }

    @Override
    public void reset() throws IOException {
        /* A call to reset can still succeed if mark is not supported, but the
         * resulting stream position is undefined, so it's not allowed here. */
        if (!markSupported()) {
            throw new IOException("Mark not supported.");
        }
        super.reset();
        pos = mark;
    }
}
