package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;

/**
 * New Defender of the Faith was proclaimed.
 */
public class Defender extends Event {

    /** Religion name. */
    final String religion;

    /** Defender tag. */
    final String tag;

    /**
     * The only constructor.
     * @param religion religion's name
     * @param tag defender's tag
     */
    public Defender(final String religion, final String tag) {
        this.religion = religion;
        this.tag = tag;
    }

    @Override
    public String toString() {
        return String.format(l10n("event.defender"), religion, tag);
    }
}
