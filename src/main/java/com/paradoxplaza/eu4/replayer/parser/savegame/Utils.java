package com.paradoxplaza.eu4.replayer.parser.savegame;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Set of static methods useful for manupulation with save games.
 */
public class Utils {

    /** Prefix of the zip files. */
    static private final byte[] ZIP_PREFIX = { 0x50, 0x4B }; //PK

    /** Prefix of the eu4bin files. */
    static private final byte[] EU4BIN_PREFIX = { 0x45, 0x55, 0x34, 0x62, 0x69, 0x6E }; //EU4bin

    /** Prefix of the eu4txt files. */
    static private final byte[] EU4TXT_PREFIX = { 0x45, 0x55, 0x34, 0x74, 0x78, 0x74 }; //EU4txt

    /**
     * Checks whether array starts with given prefix.
     * @param array array to check
     * @param prefix prefix to search for
     * @return true if array starts with prefix, false otherwise
     */
    static private boolean startsWith(final byte[] array, final byte[] prefix) {
        final int length = Math.min(array.length, prefix.length);
        for (int i = 0; i < length; ++i) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Wraps stream into IronmanStream/ZipStream if needed.
     * Long part of returned pair is the size of the zip stream, or -1.
     * @param stream save game stream
     * @return stream wrapped to IronmanStream if needed, size of zip stream
     * @throws IOException if IO error occurs
     */
    static public Pair<InputStream, Long> chooseStream(final InputStream stream)
            throws IOException {
        long size = -1;
        final BufferedInputStream buff = new BufferedInputStream(stream);
        PushbackInputStream push = new PushbackInputStream(buff, 6);
        final byte[] bytes = new byte[6];
        push.read(bytes);
        push.unread(bytes);
        if (startsWith(bytes, ZIP_PREFIX)) {
            final ZipInputStream zip = new ZipInputStream(push);
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                if (!"meta".equals(entry.getName())) {
                    size = entry.getSize();
                    push = new PushbackInputStream(zip, 6);
                    push.read(bytes);
                    push.unread(bytes);
                    break;
                }
                entry = zip.getNextEntry();
            }
        }
        if (startsWith(bytes, EU4BIN_PREFIX)) {
            return new Pair<InputStream, Long>(new IronmanStream(push), size); //it's ok, this stream's buffer is 6
        } else if (startsWith(bytes, EU4TXT_PREFIX)) {
            return new Pair<InputStream, Long>(push, size);
        } else {
            System.err.println(l10n("parser.savegame.unknown"));
            return new Pair<InputStream, Long>(push, size);
        }
    }

    /**
     * Utility classes need no constructor.
     */
    private Utils() {
        //nothing
    }
}
