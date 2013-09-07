package com.paradoxplaza.eu4.replayer.events;

/**
 * New culture of a province.
 */
public class Culture extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param culture new culture
     */
    public Culture(final String id, final String name, final String culture) {
        super(id, name, "Culture", culture);
    }
}
