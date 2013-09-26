package com.paradoxplaza.eu4.replayer.parser.culture;

import com.paradoxplaza.eu4.replayer.CountryInfo;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import com.paradoxplaza.eu4.replayer.utils.Ref;
import java.util.Map;

/**
 * Parses CULTURE_NAME={...}.
 */
class Culture extends CompoundState<Pair<Map<String, CountryInfo>, Map<String, Integer>>> {

    /** Primary country tag of the culture. */
    final Ref<String> primary = new Ref<>();

    /** Parses primary=TAG. */
    final StringState<Pair<Map<String, CountryInfo>, Map<String, Integer>>> primaryState = new StringState<>(this);

    /** State ignoring everything till matching }. */
    final Ignore<Pair<Map<String, CountryInfo>, Map<String, Integer>>> ignore = new Ignore<>(this);

    /** Culture name. */
    String name;

    /**
     * Only constructor
     * @param parent parent state
     */
    public Culture(final State<Pair<Map<String, CountryInfo>, Map<String, Integer>>> parent) {
        super(parent);
    }

    /**
     * Sets name of the culture.
     * @param name new name
     * @return this
     */
    public Culture withName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    protected void compoundReset() {
        if (primary != null) {
            primary.val = null;
        }
    }

    @Override
    protected void endCompound(final Pair<Map<String, CountryInfo>, Map<String, Integer>> context) {
        if (primary.val == null) {
            context.getSecond().put(name, name.hashCode() | 0xFF000000);
        } else {
            context.getSecond().put(name, context.getFirst().get(primary.val).color);
        }
    }

    @Override
    public State<Pair<Map<String, CountryInfo>, Map<String, Integer>>> processWord(final Pair<Map<String, CountryInfo>, Map<String, Integer>> saveGame, final String word) {
        switch (word) {
            case "primary":
                return primaryState.withOutput(primary);
            default:
                return ignore;
        }
    }
}
