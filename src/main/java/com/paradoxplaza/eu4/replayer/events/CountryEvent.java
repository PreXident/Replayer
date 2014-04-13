package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Common ancestor of events related to countries.
 */
public class CountryEvent extends Event {

    /** Country tag. */
    public final Ref<String> tag;

    /**
     * Only constructor. Uses Ref to String because of tagchanges.
     * @param tag country tag
     */
    public CountryEvent(final Ref<String> tag) {
        this.tag = tag;
    }
}
