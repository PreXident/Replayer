package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;
import java.util.Formattable;
import java.util.Formatter;

/**
 * Ancestor of all displayable events.
 */
public abstract class Event implements Formattable {

    /**
     * Accepts processor and let it process this event.
     * This method is intended to be overriden in descendants that should be
     * processed in a unique way.
     * @param date date of the event
     * @param processor EventProcessor to process the event
     * @return true if event should be logged, false otherwise
     */
    public boolean beProcessed(final Date date, final EventProcessor processor) {
        return processor.process(date, this);
    }

    /**
     * Accepts processor and let it unprocess this event.
     * This method is intended to be overriden in descendants that should be
     * unprocessed in a unique way.
     * @param date date of the event
     * @param processor EventProcessor to unprocess the event
     * @return true if event should be logged, false otherwise
     */
    public boolean beUnprocessed(final Date date, final EventProcessor processor) {
        return processor.unprocess(date, this);
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        formatter.format(toString());
    }
}
