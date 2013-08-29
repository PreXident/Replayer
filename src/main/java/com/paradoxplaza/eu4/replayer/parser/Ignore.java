package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.SaveGame;

/**
 * Ignores value, both ={...} and =VALUE.
 */
class Ignore extends State {

    enum Expecting { EQUALS, SWITCH, CLOSING }

    Expecting expecting;

    /** Number of opened "{". */
    int rightCount;

    /**
     * Only constructor.
     * @param start parent state
     */
    public Ignore(final State start) {
        super(start);
    }

    @Override
    public State processChar(final SaveGame saveGame, final char token) {
        switch (expecting) {
            case EQUALS:
                if (token != '=') {
                    throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "="));
                }
                expecting = Expecting.SWITCH;
                return this;
            case SWITCH:
                if (token != '{') {
                    throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "{"));
                }
                expecting = Expecting.CLOSING;
                return this;
            case CLOSING:
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
            default:
                assert false : "Expecting unknown token.";
                return this;
        }
    }

    @Override
    public State processWord(final SaveGame saveGame, final String word) {
        switch (expecting) {
            case EQUALS:
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, '='));
            case SWITCH:
                reset();
                return start;
            case CLOSING:
                return this;
            default:
                assert false : "Expecting unknown token.";
                return this;
        }
    }

    @Override
    protected void reset() {
        rightCount = 1;
        expecting = Expecting.EQUALS;
    }
}
