package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.utils.Ref;

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
    public ColonySize(final String id, final Ref<String> name, final String size) {
        super(id, name, "ColonySize", size);
    }
}
