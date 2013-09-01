package com.paradoxplaza.eu4.replayer.parser;

import javafx.beans.value.WritableValue;

/**
 * Parent of states processing xxx=VALUE.
 * @param <Context> context of the parser
 * @param <T> final value type
 */
public abstract class ValueState<Context, T> extends State<Context> {

    /** What tokens can be expected. */
    enum Expecting {
        EQUALS, VALUE
    }
    /** What token is expected now. */
    Expecting expecting;

    /** Where to set the value. */
     WritableValue<T> output;

    /**
     * Only constructor.
     * @param parent parent state
     */
    public ValueState(final State<Context> start) {
        super(start);
    }

    /**
     * Sets output to given reference.
     * @param output where to store output value
     * @return this
     */
    public ValueState<Context, T> withOutput(final  WritableValue<T> output) {
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
    public final State<Context> processChar(final Context context, final char token) {
        switch (expecting) {
            case EQUALS:
                if (token == '=') {
                    expecting = Expecting.VALUE;
                    return this;
                } else {
                    throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "="));
                }
            case VALUE:
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, token, "VALUE"));
            default:
                assert false : "Expecting unknown token";
                return this;
        }
    }

    @Override
    public final State<Context> processWord(final Context context, final String word) {
        switch (expecting) {
            case EQUALS:
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, "="));
            case VALUE:
                output.setValue(createOutput(word));
                reset();
                return parent;
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
