package com.paradoxplaza.eu4.replayer.parser;

import javafx.beans.value.WritableValue;

/**
 * Processes string values in format xxx=STRING.
 */
public class NumberState<Context> extends ValueState<Context, Double> {

    /**
     * Only constructor.
     * @param parent parent state
     */
    public NumberState(final State<Context> parent) {
        super(parent);
    }

    @Override
    public NumberState<Context> withOutput(final WritableValue<Double> output) {
        return (NumberState<Context>) super.withOutput(output);
    }

    @Override
    protected Double createOutput(final String word) {
        return Double.parseDouble(word);
    }
}
