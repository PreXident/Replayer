package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;

/**
 * Personal union is formed.
 */
public class Union extends Subject {

    /**
     * Only construtor.
     * @param tag country tag
     * @param newOverlord new overloard tag
     */
    public Union(final String tag, final String newOverlord) {
        super(tag, newOverlord);
    }

    @Override
    public String toString() {
        return String.format(l10n("event.union"), tag, newOverlord);
    }
}
