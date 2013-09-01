package com.paradoxplaza.eu4.replayer.parser;

import javafx.beans.value.WritableValue;

/**
 * Processes string values in format xxx=STRING.
 */
public class StringState<Context> extends ValueState<Context, String> {

    /**
     * Only constructor.
     * @param parent parent state
     */
    public StringState(final State<Context> parent) {
        super(parent);
    }

    @Override
    public StringState<Context> withOutput(final WritableValue<String> output) {
        return (StringState<Context>) super.withOutput(output);
    }

    @Override
    protected String createOutput(final String word) {
        return word;
    }
}
