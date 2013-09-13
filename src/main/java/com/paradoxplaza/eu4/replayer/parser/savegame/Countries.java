package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * Processes countries={...}.
 */
class Countries extends CompoundState<SaveGame> {

    /** State processing individual provinces. */
    final Country country = new Country(this);

    /**
     * Only constructor
     * @param parent parent state
     */
    public Countries(final State<SaveGame> parent) {
        super(parent);
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        return country.withTag(word);
    }
}
