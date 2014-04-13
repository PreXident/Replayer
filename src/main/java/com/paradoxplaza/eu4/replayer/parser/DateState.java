package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.utils.WritableValue;

/**
 * Processes dates in format xxx=Y.M.D.
 */
public class DateState<Context> extends ValueState<Context, Date> {

    /**
     * Only constructor.
     * @param parent parent state
     */
    public DateState(final State<Context> parent) {
        super(parent);
    }

    @Override
    public DateState<Context> withOutput(final WritableValue<Date> output) {
        return (DateState<Context>) super.withOutput(output);
    }

    @Override
    protected Date createOutput(final String word) {
        return new Date(word);
    }
}
