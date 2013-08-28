package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.SaveGame;

/**
 * Processes compound values in format xxx = { COMPOUND }.
 */
abstract class CompoundState extends State {

    /** What tokens can be expected. */
    enum Expecting {

        /** First =. */
        EQUALS {
            @Override
            public char toChar() {
                return '=';
            }

            @Override
            public String toString() {
                return "=";
            }
        },

        /** Opening {. */
        OPENING {
            @Override
            public char toChar() {
                return '{';
            }

            @Override
            public String toString() {
                return "{";
            }
        },
        
        /** Final } or inner word. */
        CLOSING {
            @Override
            public char toChar() {
                return '}';
            }

            @Override
            public String toString() {
                return "}";
            }
        };

        /**
         * Returns char representing expected token.
         * @return char representing expected token
         */
        public abstract char toChar();
    }

    /** What token is expected. */
    Expecting expecting;

    /**
     * Only constructor.
     * @param start parent state
     */
    public CompoundState(final State start) {
        super(start);
    }

    /**
     * Informs descendant that element is finished.
     * @param saveGame
     */
    protected abstract void endCompound(final SaveGame saveGame);

    @Override
    public State processChar(final SaveGame saveGame, final char token) {
        if (token != expecting.toChar()) {
            throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, expecting));
        }
        switch (expecting) {
            case OPENING:
                expecting = Expecting.CLOSING;
                return this;
            case EQUALS:
                expecting = Expecting.OPENING;
                return this;
            case CLOSING:
                endCompound(saveGame);
                reset();
                return start;
            default:
                assert false : "Expecting unknown token";
                return this;
        }
    }

    @Override
    protected void reset() {
        expecting = Expecting.EQUALS;
    }
}
