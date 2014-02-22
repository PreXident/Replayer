package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;

/**
 * Country becomes a colonial nation.
 */
public class Colonial extends Subject {

    /**
     * Only construtor.
     * @param tag country tag
     * @param newOverlord new overloard tag
     */
    public Colonial(final String tag, final String newOverlord) {
        super(tag, newOverlord);
    }

    @Override
    public String toString() {
        return String.format(l10n("event.colonial"), tag, newOverlord);
    }
}
