package com.paradoxplaza.eu4.replayer.parser.religion;

import com.paradoxplaza.eu4.replayer.ITaskBridge;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import java.io.InputStream;
import java.util.Map;

/**
 * Parses common/religions/*.
 */
public class ReligionsParser extends TextParser<Map<String,Integer>> {

    /**
     * Only constructor.
     * @param context religions to fill
     * @param size input size
     * @param input input stream to parse
     * @param bridge bridge listening to progress
     */
    public ReligionsParser(final Map<String,Integer> context, final long size,
            final InputStream input, final ITaskBridge<Map<String,Integer>> bridge) {
        super(context, new Start(), size, input, bridge);
    }
}
