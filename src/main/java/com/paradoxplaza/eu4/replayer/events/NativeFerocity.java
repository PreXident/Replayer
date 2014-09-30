package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * New native ferocity of a province.
 */
public class NativeFerocity extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param ferocity new ferocity
     */
    public NativeFerocity(final String id, final Ref<String> name, final String ferocity) {
        super(id, name, "NativeFerocity", ferocity);
    }
}
