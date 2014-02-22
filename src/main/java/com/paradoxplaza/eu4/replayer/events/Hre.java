package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
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
                    formatter.format(l10n("event.province.Hre.added"));
                }
            }
        }, REMOVED {
            @Override
            public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
                if ((flags & ALTERNATE) != ALTERNATE) {
                    formatter.format(toString());
                } else {
                    formatter.format(l10n("event.province.Hre.removed"));
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
                    throw new IllegalArgumentException(String.format(l10n("event.province.Hre.unknown"), string));
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
            formatter.format(
                    l10n("event.province.Hre"),
                    "<a href='#' onclick='return java.prov(this.textContent)'>" + id + "</a>", name, type);
        }
    }

    @Override
    public String toString() {
        return String.format(l10n("event.province.Hre"), id, name, type);
    }
}
