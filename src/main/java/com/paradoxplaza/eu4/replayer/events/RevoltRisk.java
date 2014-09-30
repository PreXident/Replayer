package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * New revolt risk of a province.
 */
public class RevoltRisk extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param revoltRisk new revolt risk
     */
    public RevoltRisk(final String id, final Ref<String> name, final String revoltRisk) {
        super(id, name, "RevoltRisk", revoltRisk);
    }
}
