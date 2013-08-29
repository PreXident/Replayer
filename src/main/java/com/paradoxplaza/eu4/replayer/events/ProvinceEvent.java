package com.paradoxplaza.eu4.replayer.events;

/**
 * Event associated with province.
 */
public abstract class ProvinceEvent extends Event {

    /** Province id. */
    final String id;

    /** Province name. */
    final String name;

    /**
     * Only constructor.
     * @param id province id
     * @param name province name
     */
    public ProvinceEvent(final String id, final String name) {
        this.id = id;
        this.name = name;
    }
}
