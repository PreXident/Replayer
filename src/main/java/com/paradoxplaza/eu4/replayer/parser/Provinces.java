package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.SaveGame;

/**
 * Processes provinces={...}.
 */
class Provinces extends CompoundState {

    /** State processing individual provinces. */
    final Province province = new Province(this);

    /**
     * Only constructor
     * @param start parent state
     */
    public Provinces(final State start) {
        super(start);
    }

    @Override
    public State processWord(final SaveGame saveGame, final String word) {
        if (!word.matches("-[0-9]+")) {
            throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, word, "-NUMBER"));
        }
        return province.withID(word.substring(1));
    }
}
