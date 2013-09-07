package com.paradoxplaza.eu4.replayer.events;

import static java.util.FormattableFlags.ALTERNATE;
import java.util.Formatter;

/**
 * Colony changed to city.
 */
public class City extends ProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param value ignored as it seems the save is buggy and yes and no have same meaning
     */
    public City(final String id, final String name, final String value) {
        super(id, name);
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        if ((flags & ALTERNATE) != ALTERNATE) {
            formatter.format(toString());
        } else {
            formatter.format("Colony in province <a href=\"#\" onclick=\"return java.prov(this.textContent)\">%1$s</a> (%2$s) grew to a city", id, name);
        }
    }

    @Override
    public String toString() {
        return String.format("Colony in province %1$s (%2$s) grew to a city", id, name);
    }
}
