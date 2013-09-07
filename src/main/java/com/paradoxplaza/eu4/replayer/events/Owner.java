package com.paradoxplaza.eu4.replayer.events;

/**
 * New owner of a province.
 */
public class Owner extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param tag new owner tag
     */
    public Owner(final String id, final String name, final String tag) {
        super(id, name, "Owner", tag);
    }
}
