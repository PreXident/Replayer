package com.paradoxplaza.eu4.replayer.parser.religion;

import com.paradoxplaza.eu4.replayer.parser.TextParser;
import java.io.InputStream;
import java.util.Map;

/**
 * Parses common/religions/*.
 */
public class ReligionsParser extends TextParser<Map<String,Integer>> {

    /**
     * Only constructor.
     * @param context seas to fill
     * @param size input size
     * @param input input stream to parse
     */
    public ReligionsParser(final Map<String,Integer> context, final long size, final InputStream input) {
        super(context, new Start(), size, input);
    }
}
