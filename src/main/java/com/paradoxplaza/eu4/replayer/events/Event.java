package com.paradoxplaza.eu4.replayer.events;

import java.util.Formattable;
import java.util.Formatter;

/**
 * Ancestor of all displayable events.
 */
public abstract class Event implements Formattable {

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        formatter.format(toString());
    }
}
