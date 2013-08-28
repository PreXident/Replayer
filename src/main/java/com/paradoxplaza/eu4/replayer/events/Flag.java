package com.paradoxplaza.eu4.replayer.events;

/**
 * Represents setting a flag.
 */
public class Flag extends Event {

    /** Name of the flag. */
    final String name;

    /**
     * Only constructor.
     * @param name identifier of the flag
     */
    public Flag(final String name) {
        this.name = name;
    }
}
