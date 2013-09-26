package com.paradoxplaza.eu4.replayer.parser.religion;

import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.Map;

/**
 * Parses RELIGION_NAME={...}.
 */
class Religion extends CompoundState<Map<String,Integer>> {

    /** Parses color={...}. */
    final Color color = new Color(this);

    /** State ignoring everything till matching }. */
    final Ignore<Map<String,Integer>> ignore = new Ignore<>(this);

    /** Religion name. */
    String name;

    /**
     * Only constructor
     * @param parent parent state
     */
    public Religion(final State<Map<String,Integer>> parent) {
        super(parent);
    }

    /**
     * Sets name of the religion.
     * @param name new name
     * @return this
     */
    public Religion withName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    public State<Map<String,Integer>> processWord(final Map<String,Integer> saveGame, final String word) {
        switch (word) {
            case "color":
                return color.withReligion(name);
            default:
                return ignore;
        }
    }
}
