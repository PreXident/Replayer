package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * New trade goods of a province.
 */
public class Goods extends SimpleProvinceEvent {

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param goods new trade goods
     */
    public Goods(final String id, final Ref<String> name, final String goods) {
        super(id, name, "TradeGoods", goods);
    }
}
