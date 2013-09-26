package com.paradoxplaza.eu4.replayer.parser.culture;

import com.paradoxplaza.eu4.replayer.CountryInfo;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.util.Map;

/**
 * Parses CULTURE_GROUP_NAME={...}.
 */
class CultureGroup extends CompoundState<Pair<Map<String, CountryInfo>, Map<String, Integer>>> {

    /** Culture group name. */
    String name;

    /** Parses cultures. */
    Culture culture = new Culture(this);

    /** Ignores graphical_culture and dynasty_names. */
    Ignore<Pair<Map<String, CountryInfo>, Map<String, Integer>>> ignore = new Ignore<>(this);

    /**
     * Only constructor.
     * @param parent parent state
     */
    public CultureGroup(final State<Pair<Map<String, CountryInfo>, Map<String, Integer>>> parent) {
        super(parent);
    }

    /**
     * Sets name of the culture group.
     * @param name new name
     * @return this
     */
    public CultureGroup withName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    public State<Pair<Map<String, CountryInfo>, Map<String, Integer>>> processWord(final Pair<Map<String, CountryInfo>, Map<String, Integer>> saveGame, final String word) {
        switch (word) {
            case "dynasty_names":
            case "graphical_culture":
                return ignore;
            default:
                return culture.withName(word);
        }
    }
}
