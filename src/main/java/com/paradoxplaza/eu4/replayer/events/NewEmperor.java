package com.paradoxplaza.eu4.replayer.events;

/**
 * Represents new emperor in Holy Roman Empire.
 */
public class NewEmperor extends Event {

    final String id;
    final String tag;

    public NewEmperor(final String id, final String tag) {
        this.id = id;
        this.tag = tag;
    }
}
