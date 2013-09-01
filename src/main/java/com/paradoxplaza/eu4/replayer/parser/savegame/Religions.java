package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * Parses religions={...}.
 */
class Religions extends CompoundState<SaveGame> {

    /** State processing individual religion. */
    final Religion religion = new Religion(this);

    /**
     * Only constructor.
     * @param start parent state
     */
    public Religions(final State<SaveGame> start) {
        super(start);
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        return religion.withName(word);
    }
}
