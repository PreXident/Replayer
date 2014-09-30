package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Event associated with province.
 */
public abstract class ProvinceEvent extends Event {

    /** Province id. */
    final public String id;

    /** Province name. */
    final public Ref<String> name;

    /**
     * Only constructor.
     * @param id province id
     * @param name province name
     */
    public ProvinceEvent(final String id, final Ref<String> name) {
        this.id = id;
        this.name = name;
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
