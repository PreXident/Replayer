package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.SaveGame;

/**
 * Parses religions={...}.
 */
class Religions extends CompoundState {

    /** State processing individual religion. */
    final Religion religion = new Religion(this);

    /**
     * Only constructor.
     * @param start parent state
     */
    public Religions(final Start start) {
        super(start);
    }

    @Override
    public State processWord(final SaveGame saveGame, final String word) {
        return religion.withName(word);
    }
}
