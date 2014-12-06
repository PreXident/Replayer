package com.paradoxplaza.eu4.replayer.parser.savegame.binary;

/**
 * Simple extension of standard ByteArrayOutputStream exposing buffer.
 */
public class ByteArrayOutputStream extends java.io.ByteArrayOutputStream {

    /**
     * Only constructor.
     * @param size initial size
     */
    public ByteArrayOutputStream(int size) {
        super(size);
    }

    /**
     * Returns byte on given index. No checking is done!
     * @param index index of wanted byte
     * @return byte on given index
     */
    public byte getByte(final int index) {
        return buf[index];
    }
}
