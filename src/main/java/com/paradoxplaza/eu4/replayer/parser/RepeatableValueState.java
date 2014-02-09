package com.paradoxplaza.eu4.replayer.parser;

import java.util.HashSet;
import java.util.Set;
import javafx.beans.value.WritableValue;

/**
 * Stores repeated strings provided in format xxx=VALUE.
 * @param <Context> context of the parser
 */
public class RepeatableValueState<Context> extends ValueState<Context, String> implements WritableValue<String> {

    /** Set of the resulting values. */
    final Set<String> values = new HashSet<>();

    /**
     * Only constructor.
     * @param parent parent state
     */
    public RepeatableValueState(State<Context> parent) {
        super(parent);
        super.withOutput(this);
    }

    @Override
    @Deprecated
    public RepeatableValueState<Context> withOutput(final WritableValue<String> output) {
        return (RepeatableValueState<Context>) super.withOutput(output);
    }

    @Override
    protected String createOutput(String word) {
        return word;
    }

    @Override
    public String getValue() {
        return null;
    }

    /**
     * Returns processed values.
     * @return processed values
     */
    public Set<String> getValues() {
        return values;
    }

    @Override
    public void setValue(String t) {
        values.add(t);
    }
}
