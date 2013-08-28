package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Processes dates in format xxx=Y.M.D.
 */
class DateState extends ValueState<Date> {

    /**
     * Only constructor.
     * @param start parent state
     */
    public DateState(final State start) {
        super(start);
    }

    @Override
    public DateState withOutput(final Ref<Date> output) {
        return (DateState) super.withOutput(output);
    }

    @Override
    protected Date createOutput(final String word) {
        return new Date(word);
    }
}
