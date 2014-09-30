package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.Ref;
import java.util.Formattable;
import static java.util.FormattableFlags.ALTERNATE;
import java.util.Formatter;

/**
 * Core added to a province.
 */
public class Core extends ProvinceEvent {

    /** Possible types of core event. */
    public enum Type implements Formattable {
        ADDED {
            @Override
            public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
                if ((flags & ALTERNATE) != ALTERNATE) {
                    formatter.format(toString());
                } else {
                    formatter.format(l10n("event.province.Core.added"));
                }
            }
        }, REMOVED {
            @Override
            public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
                if ((flags & ALTERNATE) != ALTERNATE) {
                    formatter.format(toString());
                } else {
                    formatter.format(l10n("event.province.Core.removed"));
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
    public Core(final String id, final Ref<String> name, final String tag, final Type type) {
        super(id, name);
        this.tag = tag;
        this.type = type;
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        if ((flags & ALTERNATE) != ALTERNATE) {
            formatter.format(toString());
        } else {
            formatter.format(
                    l10n("event.province.Core"),
                    tag, "<a href='#' onclick='return java.prov(this.textContent)'>" + id + "</a>", name.val, type);
        }
    }

    @Override
    public String toString() {
        return String.format(l10n("event.province.Claim"), tag, id, name.val, type);
    }
}
