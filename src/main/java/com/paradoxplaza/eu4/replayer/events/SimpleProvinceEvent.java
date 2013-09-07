package com.paradoxplaza.eu4.replayer.events;

import java.util.Formatter;
import static java.util.FormattableFlags.*;

/**
 * Ancestor of simple province event.
 */
public abstract class SimpleProvinceEvent extends ProvinceEvent {

    /** Type of event type. */
    final public String type;

    /** New value. */
    final public String value;

    /**
     * Only constructor.
     * @param id province id
     * @param name province name
     * @param type type of event
     * @param value new value
     */
    public SimpleProvinceEvent(final String id, final String name, final String type, final String value) {
        super(id, name);
        this.type = type;
        this.value = value;
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        if ((flags & ALTERNATE) != ALTERNATE) {
            formatter.format(toString());
        } else {
            formatter.format("%1$s of province <a href=\"#\" onclick=\"return java.prov(this.textContent)\">%2$s</a> (%3$s) changed to %4$s", type, id, name, value);
        }
    }

    @Override
    public String toString() {
        return String.format("%1$s of province %2$s (%3$s) changed to %4$s", type, id, name, value);
    }
}
