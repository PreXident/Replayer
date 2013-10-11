package com.paradoxplaza.eu4.replayer.parser.mod;

import com.paradoxplaza.eu4.replayer.ModInfo;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import java.io.InputStream;
import java.util.List;

/**
 * Parses mod desriptors.
 */
public class ModParser extends TextParser<List<ModInfo>> {

    /**
     * Only constructor.
     * @param context seas to fill
     * @param size input size
     * @param input input stream to parse
     */
    public ModParser(final List<ModInfo> context, final long size, final InputStream input) {
        super(context, new Start(), size, input);
    }
}
