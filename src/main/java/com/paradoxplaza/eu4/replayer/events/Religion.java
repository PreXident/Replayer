package com.paradoxplaza.eu4.replayer.events;

/**
 * New culture of a province.
 */
public class Religion extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param culture new culture
     */
    public Religion(final String id, final String name, final String religion) {
        super(id, name, "Religion", religion);
    }
}
