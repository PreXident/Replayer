package com.paradoxplaza.eu4.replayer.events;

/**
 * New native size of a province.
 */
public class NativeSize extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param size new size
     */
    public NativeSize(final String id, final String name, final String size) {
        super(id, name, "NativeSize", size);
    }
}
