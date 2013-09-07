package com.paradoxplaza.eu4.replayer.events;

import java.util.Formattable;
import static java.util.FormattableFlags.ALTERNATE;
import java.util.Formatter;

/**
 * Building added/removed from province.
 */
public class Hre extends ProvinceEvent {

    /** Possible types of hre event. */
    enum Type implements Formattable {
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

        static Type fromString(final String string) {
            switch (string) {
                case "yes":
                    return ADDED;
                case "no":
                    return REMOVED;
                default:
                    throw new IllegalArgumentException(String.format("Unknown hre type %1$s", string));
            }
        }
    }

    /** Hre type. */
    final Type type;

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param type hre event type
     */
    public Hre(final String id, final String name, final String type) {
        super(id, name);
        this.type = Type.fromString(type);
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        if ((flags & ALTERNATE) != ALTERNATE) {
            formatter.format(toString());
        } else {
            formatter.format("Province <a href=\"#\" onclick=\"return java.prov(this.textContent)\">%1$s</a> (%2$s) %3$#s HRE", id, name, type);
        }
    }

    @Override
    public String toString() {
        return String.format("Province %1$s (%2$s) %3$#s HRE", id, name, type);
    }
}
