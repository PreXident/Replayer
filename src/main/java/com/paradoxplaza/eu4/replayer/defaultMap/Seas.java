package com.paradoxplaza.eu4.replayer.defaultmap;

import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javafx.scene.paint.Color;

/**
 * Processes seas_start={...}.
 */
class Seas extends CompoundState<Pair<Set<Color>, Map<String, Color>>> {

    static final Pattern NUMBER = Pattern.compile("\\d+");

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Seas(final State<Pair<Set<Color>, Map<String, Color>>> parent) {
        super(parent);
    }

    @Override
    public State<Pair<Set<Color>, Map<String, Color>>> processWord(final Pair<Set<Color>, Map<String, Color>> context, final String word) {
        if (!NUMBER.matcher(word).matches()) {
            throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, word, "NUMBER"));
        }
        context.getFirst().add(context.getSecond().get(word));
        return this;
    }
}
