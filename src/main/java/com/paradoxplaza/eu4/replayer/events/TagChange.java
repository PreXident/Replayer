package com.paradoxplaza.eu4.replayer.events;

import java.util.Date;

import com.paradoxplaza.eu4.replayer.EventProcessor;

/**
 * Represents country tag change.
 */
public class TagChange extends Event {

    /** Target tag. */
    public final String toTag;

    /** Source tag. */
    public final String fromTag;

    /**
     * Only construtor.
     * @param toTag target tag
     * @param fromTag what tag changes
     */
    public TagChange(final String toTag, final String fromTag) {
        this.toTag = toTag;
        this.fromTag = fromTag;
    }

    @Override
    public boolean accept(final Date date, final EventProcessor processor) {
        return processor.process(date, this);
    }

    @Override
    public String toString() {
        return String.format("Country %2$s changed tag to %1$s", toTag, fromTag);
    }
}
