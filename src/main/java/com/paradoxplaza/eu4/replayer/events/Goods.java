package com.paradoxplaza.eu4.replayer.events;

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
    public Goods(final String id, final String name, final String goods) {
        super(id, name, "TradeGoods", goods);
    }
}
