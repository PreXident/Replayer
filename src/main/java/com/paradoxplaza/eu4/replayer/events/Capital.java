package com.paradoxplaza.eu4.replayer.events;

/**
 * New capital of a province.
 */
public class Capital extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param tag new capital
     */
    public Capital(final String id, final String name, final String capital) {
        super(id, name, "Capital", capital);
    }
}
