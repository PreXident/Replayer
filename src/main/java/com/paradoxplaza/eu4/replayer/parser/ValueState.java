package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Parent of states processing xxx=VALUE.
 * @param <T> final value type
 */
abstract class ValueState<T> extends State {

    /** What tokens can be expected. */
    enum Expecting {
        EQUALS, VALUE
    }
    /** What token is expected now. */
    Expecting expecting;

    /** Where to set the value. */
    Ref<T> output;

    /**
     * Only constructor.
     * @param start parent state
     */
    public ValueState(final State start) {
        super(start);
    }

    /**
     * Sets output to given reference.
     * @param output where to store output value
     * @return this
     */
    public ValueState<T> withOutput(final Ref<T> output) {
        this.output = output;
        return this;
    }

    /**
     * Creates final output from string value.
     * @param word extracted string
     * @return final output value
     */
    protected abstract T createOutput(final String word);

    @Override
    public final State processChar(final SaveGame saveGame, final char token) {
        switch (expecting) {
            case EQUALS:
                if (token == '=') {
                    expecting = Expecting.VALUE;
                    return this;
                } else {
                    throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "="));
                }
            case VALUE:
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, token, "date"));
            default:
                assert false : "Expecting unknown token";
                return this;
        }
    }

    @Override
    public final State processWord(final SaveGame saveGame, final String word) {
        switch (expecting) {
            case EQUALS:
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, "="));
            case VALUE:
                output.val = createOutput(word);
                reset();
                return start;
            default:
                assert false : "Expecting unknown token";
                return this;
        }
    }

    @Override
    protected final void reset() {
        expecting = Expecting.EQUALS;
    }
}
