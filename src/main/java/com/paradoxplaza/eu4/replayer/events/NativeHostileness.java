package com.paradoxplaza.eu4.replayer.events;

/**
 * New native hostileness of a province.
 */
public class NativeHostileness extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param hostileness new hostileness
     */
    public NativeHostileness(final String id, final String name, final String hostileness) {
        super(id, name, "Native hostileness", hostileness);
    }
}
