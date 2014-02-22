package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;

/**
 * Represents new emperor in Holy Roman Empire.
 */
public class NewEmperor extends Event {

    final String id;
    final String tag;

    public NewEmperor(final String id, final String tag) {
        this.id = id;
        this.tag = tag;
    }

    @Override
    public String toString() {
        return String.format(l10n("event.newemperor"), id, tag);
    }
}
