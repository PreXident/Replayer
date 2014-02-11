package com.paradoxplaza.eu4.replayer.parser.colregion;

import com.paradoxplaza.eu4.replayer.ColRegionInfo;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import java.io.InputStream;
import java.util.Map;

/**
 * Parses common/colonial_regions/*.
 */
public class ColRegionParser extends TextParser<Map<String, ColRegionInfo>> {

    /**
     * Only constructor.
     * @param context input provinces to update, output colonial regions info
     * @param size input size
     * @param input input stream to parse
     */
    public ColRegionParser(final Map<String, ColRegionInfo> context, final long size, final InputStream input) {
        super(context, new Start(), size, input);
    }
}
