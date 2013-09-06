package com.paradoxplaza.eu4.replayer.parser.defaultmap;

import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Parses map/default.map.
 */
public class DefaultMapParser extends TextParser<Pair<Set<Integer>, Map<String, ProvinceInfo>>> {

    /**
     * Only constructor.
     * @param context seas to fill
     * @param size input size
     * @param input input stream to parse
     */
    public DefaultMapParser(final Pair<Set<Integer>, Map<String, ProvinceInfo>> context, final long size, final InputStream input) {
        super(context, new Start(), size, input);
    }
}
