package com.paradoxplaza.eu4.replayer.events;

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
    public RevoltRisk(final String id, final String name, final String revoltRisk) {
        super(id, name, "Revolt risk", revoltRisk);
    }
}
