package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;

/**
 * Country becomes a protectorate.
 */
public class FreeSubject extends Subject {

    /**
     * Only construtor.
     * @param tag country tag
     */
    public FreeSubject(final String tag) {
        super(tag, null);
    }

    @Override
    public String toString() {
        return String.format(l10n("event.freesubject"), tag, oldOverlord);
    }
}
