package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * Processed TAG={...}.
 */
class Country extends CompoundState<SaveGame> {

    /** Country tag. */
    String tag;

    /** State processing country history. */
    CountryHistory history = new CountryHistory(this);

    /** State ignoring uninteresting data. */
    Ignore<SaveGame> ignore = new Ignore<>(this);

    /**
     * Only constructor
     * @param start parent state
     */
    public Country(final State<SaveGame> start) {
        super(start);
    }

    /**
     * Sets country tag.
     * @param tag new tag
     * @return this
     */
    public Country withTag(final String tag) {
        this.tag = tag;
        return this;
    }

    @Override
    protected void compoundReset() {
        tag = null;
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "history":
                return history.withTag(tag);
            default:
                return ignore;
        }
    }
}
