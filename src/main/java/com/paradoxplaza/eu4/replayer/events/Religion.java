package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;

/**
 * New culture of a province.
 */
public class Religion extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param religion new religion
     */
    public Religion(final String id, final String name, final String religion) {
        super(id, name, "Religion", religion);
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
