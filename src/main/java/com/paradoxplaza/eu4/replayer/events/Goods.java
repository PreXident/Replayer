package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * New trade goods of a province.
 */
public class Goods extends SimpleProvinceEvent {

    /** Owner event generated as part of goods2owner fix. */
    public Owner owner;

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param goods new trade goods
     */
    public Goods(final String id, final Ref<String> name, final String goods) {
        super(id, name, "TradeGoods", goods);
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
