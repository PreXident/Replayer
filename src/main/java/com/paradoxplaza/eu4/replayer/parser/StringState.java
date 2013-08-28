package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Processes string values in format xxx=STRING.
 */
class StringState extends ValueState<String> {

    /**
     * Only constructor.
     * @param start parent state
     */
    public StringState(final State start) {
        super(start);
    }

    @Override
    public StringState withOutput(final Ref<String> output) {
        return (StringState) super.withOutput(output);
    }

    @Override
    protected String createOutput(final String word) {
        return word;
    }
}
