package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.SaveGame;

/**
 * Ignores everything till matching "}".
 */
class Ignore extends State {

    /** Number of opened "{". */
    int rightCount;

    /**
     * Only constructor.
     * @param start parent state
     */
    public Ignore(final Start start) {
        super(start);
    }

    @Override
    public State processChar(final SaveGame saveGame, final char token) {
        switch (token) {
            case '{':
                ++rightCount;
                return this;
            case '}':
                if (--rightCount == 0) {
                    reset();
                    return start;
                } else {
                    return this;
                }
            default:
                return this;
        }
    }

    @Override
    protected void reset() {
        rightCount = 1;
    }
}
