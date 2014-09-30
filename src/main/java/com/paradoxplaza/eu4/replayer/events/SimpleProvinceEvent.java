package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.Ref;
import static java.util.FormattableFlags.ALTERNATE;
import java.util.Formatter;

/**
 * Ancestor of simple province event.
 */
public abstract class SimpleProvinceEvent extends ProvinceEvent {

    /** Type of event type. */
    final public String type;

    /** New value. */
    final public String value;

    /**
     * Previous value.
     * Set during processing. Used during unprocessing.
     */
    public String previousValue;

    /**
     * Only constructor.
     * @param id province id
     * @param name province name
     * @param type type of event
     * @param value new value
     */
    public SimpleProvinceEvent(final String id, final Ref<String> name, final String type, final String value) {
        super(id, name);
        this.type = type;
        this.value = value;
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        if ((flags & ALTERNATE) != ALTERNATE) {
            formatter.format(toString());
        } else {
            formatter.format(
                    l10n("event.province." + type),
                    "<a href='#' onclick='return java.prov(this.textContent)'>" + id + "</a>", name.val, value);
        }
    }

    @Override
    public String toString() {
        return String.format(l10n("event.province." + type), id, name.val, value);
    }
}
