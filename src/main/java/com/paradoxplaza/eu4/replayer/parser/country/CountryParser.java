package com.paradoxplaza.eu4.replayer.parser.country;

import com.paradoxplaza.eu4.replayer.ITaskBridge;
import com.paradoxplaza.eu4.replayer.parser.TextParser;
import com.paradoxplaza.eu4.replayer.utils.Ref;
import java.io.InputStream;

/**
 * Parses common/countries/*.
 */
public class CountryParser extends TextParser<Ref<Integer>> {

    /**
     * Only constructor.
     * @param context country color output
     * @param size input size
     * @param input input stream to parse
     * @param bridge bridge listening to progress
     */
    public CountryParser(final Ref<Integer> context, final long size,
            final InputStream input, final ITaskBridge<Ref<Integer>> bridge) {
        super(context, new Start(), size, input, bridge);
    }
}
