package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.utils.Ref;

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
    public NativeHostileness(final String id, final Ref<String> name, final String hostileness) {
        super(id, name, "NativeHostileness", hostileness);
    }
}
