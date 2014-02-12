package com.paradoxplaza.eu4.replayer.events;

/**
 * Country becomes a vassal.
 */
public class Vassal extends Subject {

    /**
     * Only construtor.
     * @param tag country tag
     * @param newOverlord new overloard tag
     */
    public Vassal(final String tag, final String newOverlord) {
        super(tag, newOverlord);
    }

    @Override
    public String toString() {
        return String.format("Country %1$s becomes vassal of %2$s", tag, newOverlord);
    }
}
