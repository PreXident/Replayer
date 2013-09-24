package com.paradoxplaza.eu4.replayer.parser.religion;

import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.Map;

/**
 * Parses RELIGION_GROUP_NAME={...}.
 */
class ReligionGroup extends CompoundState<Map<String, Integer>> {

    /** Religion group name. */
    String name;

    /** Parses religions. */
    Religion religion = new Religion(this);

    /** Ignores defender_of_faith and crusade_name. */
    Ignore<Map<String, Integer>> ignore = new Ignore<>(this);

    /**
     * Only constructor.
     * @param parent parent state
     */
    public ReligionGroup(final State<Map<String, Integer>> parent) {
        super(parent);
    }

    /**
     * Sets name of the religion group.
     * @param name new name
     * @return this
     */
    public ReligionGroup withName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    public State<Map<String, Integer>> processWord(final Map<String, Integer> saveGame, final String word) {
        switch (word) {
            case "crusade_name":
            case "defender_of_faith":
                return ignore;
            default:
                return religion.withName(word);
        }
    }
}
