package com.paradoxplaza.eu4.replayer.events;

/**
 * New religion was enabled.
 */
public class EnableReligion extends Event {

    /** Name of the religion. */
    final String name;

    /**
     * Only construtor.
     * @param name name of the religion.
     */
    public EnableReligion(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Religion %1$s enabled", name);
    }
}
