package com.paradoxplaza.eu4.replayer;

/**
 * Represents setting a flag.
 */
public class FlagSet extends Event {

    /** Name of the flag. */
    final String name;

    /**
     * Only constructor.
     * @param name identifier of the flag
     */
    public FlagSet(final String name) {
        this.name = name;
    }
}
