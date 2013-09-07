package com.paradoxplaza.eu4.replayer.events;

/**
 * New garrison of a province.
 */
public class Garrison extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param garrison new base garrison
     */
    public Garrison(final String id, final String name, final String garrison) {
        super(id, name, "Garrison", garrison);
    }
}
