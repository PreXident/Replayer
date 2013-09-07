package com.paradoxplaza.eu4.replayer.events;

/**
 * New manpower of a province.
 */
public class Manpower extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param tag new manpower
     */
    public Manpower(final String id, final String name, final String manpower) {
        super(id, name, "Manpower", manpower);
    }
}
