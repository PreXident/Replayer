package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;

/**
 * New controller of a province.
 */
public class Controller extends SimpleProvinceEvent {

    /** Kind of rebels. */
    final public String rebel;

    /** New controller tag. */
    final public String tag;

    /**
     * Only constructor.
     * @param id province id
     * @param name province name
     * @param tag new controller tag
     */
    public Controller(final String id, final String name, final String tag, final String rebel) {
        super(id, name, "Controller", "REB".equals(tag) ? tag + " (" + rebel + ")" : tag);
        this.tag = tag;
        this.rebel = rebel;
    }

    @Override
    public boolean accept(final Date date, final EventProcessor processor) {
        return processor.process(date, this);
    }
}
