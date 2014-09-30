package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * New base tax of a province.
 */
public class Tax extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param tax new base tax
     */
    public Tax(final String id, final Ref<String> name, final String tax) {
        super(id, name, "Tax", tax);
    }
}
