package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * New capital of a province.
 */
public class Capital extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param capital new capital
     */
    public Capital(final String id, final Ref<String> name, final String capital) {
        super(id, name, "Capital", capital);
    }
}
