package com.paradoxplaza.eu4.replayer.events;

/**
 * New controller of a province.
 */
public class Controller extends ProvinceEvent {

    /** New controller tag. */
    final String tag;

    /** Kind of rebels. */
    final String rebel;

    /**
     * Only constructor.
     * @param id province id
     * @param name province name
     * @param tag new controller tag
     */
    public Controller(final String id, final String name, final String tag, final String rebel) {
        super(id, name);
        this.tag = tag;
        this.rebel = rebel;
    }

    @Override
    public String toString() {
        return String.format("Controller of province %1$s (%2$s) changed to %3$s", id, name, tag);
    }
}
