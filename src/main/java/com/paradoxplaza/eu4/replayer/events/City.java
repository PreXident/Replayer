package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.Ref;
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
    public City(final String id, final Ref<String> name, final String value) {
        super(id, name);
    }

    @Override
    public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        if ((flags & ALTERNATE) != ALTERNATE) {
            formatter.format(toString());
        } else {
            formatter.format(l10n("event.province.City"), "<a href='#' onclick='return java.prov(this.textContent)'>" + id + "</a>", name.val);
        }
    }

    @Override
    public String toString() {
        return String.format(l10n("event.province.City"), id, name.val);
    }
}
