package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * New manpower of a province.
 */
public class Manpower extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param manpower new manpower
     */
    public Manpower(final String id, final Ref<String> name, final String manpower) {
        super(id, name, "Manpower", manpower);
    }
}
