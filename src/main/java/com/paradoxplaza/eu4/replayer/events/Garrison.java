package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.utils.Ref;

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
    public Garrison(final String id, final Ref<String> name, final String garrison) {
        super(id, name, "Garrison", garrison);
    }
}
