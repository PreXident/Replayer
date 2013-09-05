package com.paradoxplaza.eu4.replayer.parser.defaultmap;

import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Processes seas_start={...}.
 */
class Seas extends CompoundState<Pair<Set<Integer>, Map<String, ProvinceInfo>>> {

    static final Pattern NUMBER = Pattern.compile("\\d+");

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Seas(final State<Pair<Set<Integer>, Map<String, ProvinceInfo>>> parent) {
        super(parent);
    }

    @Override
    public State<Pair<Set<Integer>, Map<String, ProvinceInfo>>> processWord(final Pair<Set<Integer>, Map<String, ProvinceInfo>> context, final String word) {
        if (!NUMBER.matcher(word).matches()) {
            throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, word, "NUMBER"));
        }
        context.getFirst().add(context.getSecond().get(word).color);
        return this;
    }
}
