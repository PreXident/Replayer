package com.paradoxplaza.eu4.replayer.defaultMap;

import com.paradoxplaza.eu4.replayer.parser.TextParser;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import javafx.scene.paint.Color;

/**
 * Parses map/default.map.
 */
public class DefaultMapParser extends TextParser<Pair<Set<Color>, Map<String, Color>>> {

    /**
     * Only constructor.
     * @param context seas to fill
     */
    public DefaultMapParser(final Pair<Set<Color>, Map<String, Color>> context) {
        super(context);
    }

    /**
     * Parses the input stream.
     * @param input stream to parse
     * @throws IOException if any error occurs
     */
    public void parse(final InputStream input) throws IOException{
        super.parse(new Start(), input);
    }
}
