package com.paradoxplaza.eu4.replayer.events;

/**
 * New colony size of a province.
 */
public class ColonySize extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param size new size
     */
    public ColonySize(final String id, final String name, final String size) {
        super(id, name, "ColonySize", size);
    }
}
