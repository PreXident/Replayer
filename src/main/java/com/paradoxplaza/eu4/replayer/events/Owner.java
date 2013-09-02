package com.paradoxplaza.eu4.replayer.events;

import static java.util.FormattableFlags.ALTERNATE;
import java.util.Formatter;

/**
 * New owner of a province.
 */
public class Owner extends ProvinceEvent {

    /** New owner of the province. */
    final public String tag;

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param tag new owner tag
     */
    public Owner(final String id, final String name, final String tag) {
        super(id, name);
        this.tag = tag;
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        if ((flags & ALTERNATE) != ALTERNATE) {
            formatter.format(toString());
        } else {
            formatter.format("Province <a href=\"#\" onclick=\"return java.prov(this.textContent)\">%1$s</a> (%2$s) got new owner %3$s", id, name, tag);
        }
    }

    @Override
    public String toString() {
        return String.format("Province %1$s (%2$s) got new owner %3$s", id, name, tag);
    }
}
