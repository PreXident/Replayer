package com.paradoxplaza.eu4.replayer.parser.culture;

import com.paradoxplaza.eu4.replayer.CountryInfo;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.io.InputStream;
import java.util.Map;

/**
 * Parses common/cultures/*.
 */
public class CulturesParser extends TextParser<Pair<Map<String, CountryInfo>, Map<String, Integer>>> {

    /**
     * Only constructor.
     * @param context seas to fill
     * @param size input size
     * @param input input stream to parse
     */
    public CulturesParser(final Pair<Map<String, CountryInfo>, Map<String, Integer>> context, final long size, final InputStream input) {
        super(context, new Start(), size, input);
    }
}
