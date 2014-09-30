package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * New owner of a province.
 */
public class Owner extends SimpleProvinceEvent {

    /** Previous controller of the province. */
    public String previousController = null;

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param tag new owner tag
     */
    public Owner(final String id, final Ref<String> name, final String tag) {
        super(id, name, "Owner", tag);
    }

    @Override
    public boolean beProcessed(final Date date, final EventProcessor processor) {
        return processor.process(date, this);
    }

    @Override
    public boolean beUnprocessed(final Date date, final EventProcessor processor) {
        return processor.unprocess(date, this);
    }
}
