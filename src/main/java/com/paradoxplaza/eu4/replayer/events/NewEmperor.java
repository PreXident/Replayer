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

    @Override
    public String toString() {
        return String.format("HRE got new emeperor %1$s (%2$s)", id, tag);
    }
}
