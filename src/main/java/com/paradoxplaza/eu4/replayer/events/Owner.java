package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;

/**
 * New owner of a province.
 */
public class Owner extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param tag new owner tag
     */
    public Owner(final String id, final String name, final String tag) {
        super(id, name, "Owner", tag);
    }

    @Override
    public boolean accept(final Date date, final EventProcessor processor) {
        return processor.process(date, this);
    }
}
