package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import static java.util.FormattableFlags.ALTERNATE;
import java.util.Formatter;

/**
 * Building added/removed from province.
 */
public class Building extends ProvinceEvent {

    /** Possible types of building event. */
    enum Type {
        BUILT {
            @Override
            public String toString() {
                return l10n("building.built");
            }
        },
        DESTROYED {
            @Override
            public String toString() {
                return l10n("building.destroyed");
            }
        };
        static Type fromString(final String string) {
            switch (string) {
                case "yes":
                    return BUILT;
                case "no":
                    return DESTROYED;
                default:
                    throw new IllegalArgumentException(String.format(l10n("building.unknown"), string));
            }
        }
    }

    /** Building name. */
    final String building;

    /** Building type. */
    final Type type;

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param building building name
     * @param type building event type
     */
    public Building(final String id, final String name, final String building, final String type) {
        super(id, name);
        this.building = building;
        this.type = Type.fromString(type);
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        if ((flags & ALTERNATE) != ALTERNATE) {
            formatter.format(toString());
        } else {
            formatter.format(l10n("building.log"), building, type, "<a href='#' onclick='return java.prov(this.textContent)'>" + id + "</a>", name);
        }
    }

    @Override
    public String toString() {
        return String.format(l10n("building.log"), building, type, id, name);
    }
}
