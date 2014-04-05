package com.paradoxplaza.eu4.replayer.parser.climate;

import com.paradoxplaza.eu4.replayer.ITaskBridge;
import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import java.io.InputStream;
import java.util.Map;

/**
 * Parses map/default.map.
 */
public class ClimateParser extends TextParser<Map<String, ProvinceInfo>> {

    /**
     * Only constructor.
     * @param context seas to fill
     * @param size input size
     * @param input input stream to parse
     * @param bridge bridge listening to progress
     */
    public ClimateParser(final Map<String, ProvinceInfo> context,
            final long size, final InputStream input,
            final ITaskBridge<Map<String, ProvinceInfo>> bridge) {
        super(context, new Start(), size, input, bridge);
    }
}
