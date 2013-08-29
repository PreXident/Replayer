package com.paradoxplaza.eu4.replayer.events;

/**
 * New owner of a province.
 */
public class Owner extends ProvinceEvent {

    /** New owner of the province. */
    final String tag;

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param tag new owner tag
     */
    public Owner(final String id, final String name, final String tag) {
        super(id, name);
        this.tag = tag;
    }

    @Override
    public String toString() {
        return String.format("Province %1$s (%2$s) got new owner %3$s", id, name, tag);
    }
}
