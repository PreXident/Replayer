package com.paradoxplaza.eu4.replayer.events;

/**
 * New name of a province.
 */
public class Name extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param newName new name
     */
    public Name(final String id, final String name, final String newName) {
        super(id, name, "Name", newName);
    }
}
