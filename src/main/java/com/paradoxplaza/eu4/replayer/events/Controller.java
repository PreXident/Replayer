package com.paradoxplaza.eu4.replayer.events;

import java.util.Formatter;
import static java.util.FormattableFlags.*;

/**
 * New controller of a province.
 */
public class Controller extends ProvinceEvent {

    /** New controller tag. */
    final public String tag;

    /** Kind of rebels. */
    final public String rebel;

    /**
     * Only constructor.
     * @param id province id
     * @param name province name
     * @param tag new controller tag
     */
    public Controller(final String id, final String name, final String tag, final String rebel) {
        super(id, name);
        this.tag = tag;
        this.rebel = rebel;
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        if ((flags & ALTERNATE) != ALTERNATE) {
            formatter.format(toString());
        } else {
            formatter.format("Controller of province <a href=\"#\" onclick=\"return java.prov(this.textContent)\">%1$s</a> (%2$s) changed to %3$s", id, name, tag);
        }
    }

    @Override
    public String toString() {
        return String.format("Controller of province %1$s (%2$s) changed to %3$s", id, name, tag);
    }
}
