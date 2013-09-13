package com.paradoxplaza.eu4.replayer.events;

import java.util.Formattable;
import static java.util.FormattableFlags.ALTERNATE;
import java.util.Formatter;

/**
 * Core added to a province.
 */
public class Claim extends ProvinceEvent {

    /** Possible types of core event. */
    public enum Type implements Formattable {
        ADDED {
            @Override
            public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
                if ((flags & ALTERNATE) != ALTERNATE) {
                    formatter.format(toString());
                } else {
                    formatter.format("added to");
                }
            }
        }, REMOVED {
            @Override
            public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
                if ((flags & ALTERNATE) != ALTERNATE) {
                    formatter.format(toString());
                } else {
                    formatter.format("removed from");
                }
            }
        };
    }

    /** Makes access to ADDED type easier. */
    static public Type ADDED = Type.ADDED;

    /** Makes access to REMOVED type easier. */
    static public Type REMOVED = Type.REMOVED;

    /** New core owner. */
    final String tag;

    /** Type of core event. */
    final Type type;

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param tag country tag
     * @param type core event type
     */
    public Claim(final String id, final String name, final String tag, final Type type) {
        super(id, name);
        this.tag = tag;
        this.type = type;
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        if ((flags & ALTERNATE) != ALTERNATE) {
            formatter.format(toString());
        } else {
            formatter.format("Claim to province <a href=\"#\" onclick=\"return java.prov(this.textContent)\">%2$s</a> (%3$s) %4$#s country %1$s", tag, id, name, type);
        }
    }

    @Override
    public String toString() {
        return String.format("Claim to province %2$s (%3$s) %4$#s country %1$s", tag, id, name, type);
    }
}